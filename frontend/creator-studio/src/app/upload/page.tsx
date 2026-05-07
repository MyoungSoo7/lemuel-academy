"use client";
import { useState } from "react";

export default function UploadPage() {
  const [file, setFile] = useState<File | null>(null);
  const [progress, setProgress] = useState(0);
  const [videoId, setVideoId] = useState<string | null>(null);
  const [status, setStatus] = useState<string>("");

  async function handleUpload() {
    if (!file) return;
    const token = localStorage.getItem("lqa_token") ?? "";
    setStatus("upload-url 요청 중...");
    const r1 = await fetch("/api/media/videos/upload-url", {
      method: "POST",
      headers: {
        "content-type": "application/json",
        "authorization": `Bearer ${token}`,
      },
      body: JSON.stringify({ filename: file.name, contentType: file.type }),
    });
    const { videoId, uploadUrl } = await r1.json();
    setVideoId(videoId);

    setStatus("R2 에 업로드 중...");
    const xhr = new XMLHttpRequest();
    xhr.open("PUT", uploadUrl);
    xhr.setRequestHeader("content-type", file.type);
    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) setProgress(Math.round(e.loaded / e.total * 100));
    };
    xhr.onload = async () => {
      if (xhr.status === 200 || xhr.status === 204) {
        setStatus("트랜스코딩 큐에 등록 중...");
        await fetch(`/api/media/videos/${videoId}/finalize`,
          { method: "POST", headers: { "authorization": `Bearer ${token}` } });
        setStatus("✅ 업로드 완료. 변환 중 (수 분 소요)");
      } else {
        setStatus(`❌ 업로드 실패 (${xhr.status})`);
      }
    };
    xhr.send(file);
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">영상 업로드</h1>
      <input type="file" accept="video/*"
             onChange={e => setFile(e.target.files?.[0] ?? null)}
             className="block mb-4" />
      <button onClick={handleUpload} disabled={!file}
              className="px-4 py-2 bg-blue-600 text-white rounded disabled:bg-gray-400">
        업로드
      </button>
      {progress > 0 && (
        <div className="mt-4">
          <div className="bg-gray-200 rounded h-2">
            <div className="bg-blue-600 h-2 rounded" style={{ width: `${progress}%` }} />
          </div>
          <p className="text-sm mt-1">{progress}%</p>
        </div>
      )}
      {status && <p className="mt-4 text-sm">{status}</p>}
      {videoId && <p className="mt-2 text-xs text-gray-500">video_id: {videoId}</p>}
    </div>
  );
}
