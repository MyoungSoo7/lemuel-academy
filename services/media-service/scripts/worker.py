#!/usr/bin/env python3
"""ffmpeg 워커 — Redis Streams 'media:transcode' 구독 → R2 다운로드 → HLS 변환 → R2 업로드 → DB 상태 업데이트.

호출: python3 worker.py
"""
from __future__ import annotations

import os
import shutil
import subprocess
import tempfile
import time
from pathlib import Path

import boto3
import psycopg
import redis

REDIS_URL    = os.environ.get("REDIS_URL", "redis://127.0.0.1:6389")
QUEUE        = os.environ.get("LQA_QUEUE", "media:transcode")
GROUP        = os.environ.get("LQA_GROUP", "transcoders")
CONSUMER     = os.environ.get("LQA_CONSUMER", "worker-1")
DB_DSN       = os.environ.get("DATABASE_URL_PG",
                               "postgres://academy:academy@127.0.0.1:5439/academy")

R2_ENDPOINT  = os.environ["R2_ENDPOINT"]
R2_BUCKET    = os.environ.get("R2_BUCKET", "lemuel-academy")
R2_ACCESS    = os.environ["R2_ACCESS_KEY"]
R2_SECRET    = os.environ["R2_SECRET_KEY"]

THIS_DIR    = Path(__file__).resolve().parent
TRANSCODE   = THIS_DIR / "transcode.sh"


def s3():
    return boto3.client(
        "s3",
        endpoint_url=R2_ENDPOINT,
        aws_access_key_id=R2_ACCESS,
        aws_secret_access_key=R2_SECRET,
        region_name="auto",
    )


def claim_or_create_group(r: redis.Redis):
    try:
        r.xgroup_create(QUEUE, GROUP, id="0", mkstream=True)
    except redis.ResponseError as e:
        if "BUSYGROUP" not in str(e):
            raise


def update_status(video_id: str, status: str, **fields):
    sets = ["status = %s"]
    params = [status]
    for k, v in fields.items():
        sets.append(f"{k} = %s")
        params.append(v)
    if status == "READY":
        sets.append("ready_at = now()")
    params.append(video_id)
    sql = f"UPDATE media.videos SET {', '.join(sets)} WHERE id = %s"
    with psycopg.connect(DB_DSN) as conn:
        conn.execute(sql, params)
        conn.commit()


def fetch_original_key(video_id: str) -> str | None:
    with psycopg.connect(DB_DSN) as conn:
        row = conn.execute(
            "SELECT original_url FROM media.videos WHERE id = %s",
            (video_id,)
        ).fetchone()
    return row[0] if row else None


def process(video_id: str):
    print(f"[worker] processing {video_id}")
    update_status(video_id, "TRANSCODING")
    key = fetch_original_key(video_id)
    if not key:
        update_status(video_id, "FAILED", error_message="original_url missing")
        return

    s3c = s3()
    with tempfile.TemporaryDirectory(prefix=f"transcode-{video_id}-") as tmp:
        in_path = Path(tmp) / "input"
        out_dir = Path(tmp) / "out"
        s3c.download_file(R2_BUCKET, key, str(in_path))
        try:
            subprocess.run([str(TRANSCODE), str(in_path), str(out_dir)],
                            check=True)
        except subprocess.CalledProcessError as e:
            update_status(video_id, "FAILED", error_message=f"ffmpeg rc={e.returncode}")
            return

        # 업로드: hls/<video_id>/{master,1080p,720p,480p}.m3u8 + 모든 .ts + thumb.jpg
        hls_prefix = f"hls/{video_id}/"
        for f in out_dir.iterdir():
            if f.name == "duration.txt":
                continue
            content_type = "application/vnd.apple.mpegurl" if f.suffix == ".m3u8" \
                else "video/mp2t" if f.suffix == ".ts" \
                else "image/jpeg" if f.suffix == ".jpg" \
                else "application/octet-stream"
            s3c.upload_file(
                str(f), R2_BUCKET, hls_prefix + f.name,
                ExtraArgs={"ContentType": content_type, "ACL": "public-read"},
            )

        duration = int((out_dir / "duration.txt").read_text().strip())
        master_url = f"{R2_ENDPOINT.rstrip('/')}/{R2_BUCKET}/{hls_prefix}master.m3u8"
        thumb_url  = f"{R2_ENDPOINT.rstrip('/')}/{R2_BUCKET}/{hls_prefix}thumb.jpg"
        update_status(video_id, "READY",
                       hls_master_url=master_url,
                       thumbnail_url=thumb_url,
                       duration_sec=duration)
    print(f"[worker] done {video_id}")


def main():
    r = redis.from_url(REDIS_URL, decode_responses=True)
    claim_or_create_group(r)
    print(f"[worker] consuming {QUEUE} as {CONSUMER}@{GROUP}")
    while True:
        try:
            msgs = r.xreadgroup(GROUP, CONSUMER, {QUEUE: ">"},
                                 count=1, block=10_000)
        except redis.ConnectionError:
            time.sleep(2); continue
        if not msgs:
            continue
        for _, entries in msgs:
            for msg_id, fields in entries:
                vid = fields.get("video_id")
                try:
                    process(vid)
                except Exception as e:
                    print(f"[worker] error: {e}")
                    if vid:
                        update_status(vid, "FAILED",
                                       error_message=str(e)[:500])
                finally:
                    r.xack(QUEUE, GROUP, msg_id)


if __name__ == "__main__":
    main()
