"use client";
import Hls from "hls.js";
import { useEffect, useRef } from "react";

export default function VideoPlayer(
  { src, lessonId }: { src: string; lessonId: string }
) {
  const videoRef = useRef<HTMLVideoElement>(null);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;

    if (video.canPlayType("application/vnd.apple.mpegurl")) {
      video.src = src; // Safari 네이티브 HLS
    } else if (Hls.isSupported()) {
      const hls = new Hls({ enableWorker: true });
      hls.loadSource(src);
      hls.attachMedia(video);
      return () => hls.destroy();
    }
  }, [src]);

  // 5초마다 진도 보고
  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;
    const id = setInterval(() => {
      if (video.paused || video.ended) return;
      const watched = Math.floor(video.currentTime);
      const completed = video.duration > 0 &&
        video.currentTime / video.duration > 0.95;
      const token = typeof window !== "undefined"
        ? localStorage.getItem("lqa_token") : null;
      if (!token) return;
      fetch("/api/users/progress", {
        method: "POST",
        headers: {
          "content-type": "application/json",
          "authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({
          lessonId, watchedSeconds: watched, completed,
        }),
      }).catch(() => {});
    }, 5000);
    return () => clearInterval(id);
  }, [lessonId]);

  return (
    <video ref={videoRef} controls
      className="w-full aspect-video bg-black rounded" />
  );
}
