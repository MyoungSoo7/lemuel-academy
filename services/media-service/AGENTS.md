# media-service AGENTS.md

영상 업로드/트랜스코딩/HLS 서빙. 가장 stateful 하고 R2/Redis/PG 셋 다 쓰는 서비스.

## 핵심 흐름 5단계

1. 클라가 `/upload-url` 호출 → media-service 가 R2 presigned PUT URL 리턴 (15분 유효)
2. 클라가 R2 에 PUT 으로 직접 업로드 (백엔드 트래픽 0)
3. 클라가 `/finalize` 호출 → DB status=UPLOADED, Redis Streams `media:transcode` enqueue
4. ffmpeg-worker (Python, 별도 컨테이너) 가 dequeue → R2 다운 → ffmpeg HLS → R2 upload → DB status=READY
5. 학생이 `/{id}/manifest` 호출 → R2 public m3u8 URL 응답 → hls.js 가 ABR 재생

## 자주 만지는 파일

- `scripts/transcode.sh` — ffmpeg 옵션 (비트레이트 / preset / segment 길이)
- `scripts/worker.py` — Redis xreadgroup / R2 boto3 / DB UPDATE
- `Dockerfile.worker` — Python + ffmpeg 이미지 (~1GB)
- `R2Client.kt` — S3Presigner / S3Client Bean
- `VideoController.kt` — REST 엔드포인트

## 절대 만지지 말 것 (운영 영향)

- 이미 R2 에 올라간 hls/<video_id>/ 경로 schema (master.m3u8 / 1080p.m3u8 / *.ts / thumb.jpg) — 클라(hls.js) 와 합의된 규약
- VideoStatus enum 값 (`UPLOADING/UPLOADED/TRANSCODING/READY/FAILED`) — DB CHECK 제약 + 클라이언트 분기

## 새 트랜스코딩 옵션 추가하는 법

```
1. transcode.sh 에 새 ffmpeg 명령 추가
2. master.m3u8 EXT-X-STREAM-INF 라인 추가
3. 워커 재빌드: docker compose build ffmpeg-worker
4. 기존 영상 재처리 필요시: redis-cli XADD media:transcode '*' video_id <id>
```

## 운영 모니터링

- Redis Streams 길이: `XLEN media:transcode` (백로그 확인)
- 워커 컨슈머: `XINFO CONSUMERS media:transcode transcoders`
- 실패 영상: `SELECT id, error_message FROM media.videos WHERE status='FAILED'`
- R2 사용량: Cloudflare 대시보드 (Class A operations, storage)
