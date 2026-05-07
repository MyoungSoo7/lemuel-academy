#!/usr/bin/env bash
# ffmpeg HLS multi-bitrate 변환 스크립트.
#   usage: transcode.sh <input.mp4> <output_dir>
# 산출:
#   master.m3u8
#   1080p.m3u8 + 1080p_*.ts
#   720p.m3u8  + 720p_*.ts
#   480p.m3u8  + 480p_*.ts
#   thumb.jpg
set -euo pipefail
IN="$1"
OUT="$2"
mkdir -p "$OUT"

# 6초 세그먼트, AAC 128k, H.264 + AAC HLS
common=( -hide_banner -y -i "$IN" \
    -preset veryfast -profile:v main -sc_threshold 0 \
    -g 48 -keyint_min 48 \
    -hls_time 6 -hls_playlist_type vod \
    -hls_flags independent_segments \
    -movflags +faststart -c:a aac -b:a 128k -ac 2 )

ffmpeg "${common[@]}" \
    -map 0:v:0 -map 0:a:0? -c:v libx264 -b:v 5000k -maxrate 5350k -bufsize 7500k \
    -vf "scale=w=1920:h=1080:force_original_aspect_ratio=decrease,pad=ceil(iw/2)*2:ceil(ih/2)*2" \
    -hls_segment_filename "$OUT/1080p_%03d.ts" "$OUT/1080p.m3u8"

ffmpeg "${common[@]}" \
    -map 0:v:0 -map 0:a:0? -c:v libx264 -b:v 2500k -maxrate 2675k -bufsize 3750k \
    -vf "scale=w=1280:h=720:force_original_aspect_ratio=decrease,pad=ceil(iw/2)*2:ceil(ih/2)*2" \
    -hls_segment_filename "$OUT/720p_%03d.ts" "$OUT/720p.m3u8"

ffmpeg "${common[@]}" \
    -map 0:v:0 -map 0:a:0? -c:v libx264 -b:v 1000k -maxrate 1070k -bufsize 1500k \
    -vf "scale=w=854:h=480:force_original_aspect_ratio=decrease,pad=ceil(iw/2)*2:ceil(ih/2)*2" \
    -hls_segment_filename "$OUT/480p_%03d.ts" "$OUT/480p.m3u8"

# 썸네일 (5초 지점)
ffmpeg -hide_banner -y -ss 5 -i "$IN" -frames:v 1 -q:v 3 "$OUT/thumb.jpg" || true

# master playlist
cat > "$OUT/master.m3u8" <<'EOF'
#EXTM3U
#EXT-X-VERSION:3
#EXT-X-STREAM-INF:BANDWIDTH=5350000,RESOLUTION=1920x1080
1080p.m3u8
#EXT-X-STREAM-INF:BANDWIDTH=2675000,RESOLUTION=1280x720
720p.m3u8
#EXT-X-STREAM-INF:BANDWIDTH=1070000,RESOLUTION=854x480
480p.m3u8
EOF

# duration 추출
duration=$(ffprobe -v error -show_entries format=duration -of csv=p=0 "$IN" \
    | awk '{printf "%d", $1}')
echo "$duration" > "$OUT/duration.txt"

echo "[transcode] done: $OUT"
