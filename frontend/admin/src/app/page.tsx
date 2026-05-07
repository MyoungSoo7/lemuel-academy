"use client";
import useSWR from "swr";

const fetcher = (url: string) => fetch(url).then(r => r.json());

export default function AdminDashboard() {
  const { data: pending } = useSWR("/api/catalog/courses?status=REVIEW", fetcher);
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">대시보드</h1>
      <div className="grid grid-cols-3 gap-4">
        <div className="bg-slate-800 p-4 rounded">
          <p className="text-sm text-slate-400">검수 대기</p>
          <p className="text-3xl font-bold">{pending?.total ?? "—"}</p>
        </div>
        <div className="bg-slate-800 p-4 rounded">
          <p className="text-sm text-slate-400">총 강의</p>
          <p className="text-3xl font-bold">—</p>
        </div>
        <div className="bg-slate-800 p-4 rounded">
          <p className="text-sm text-slate-400">활성 사용자 (24h)</p>
          <p className="text-3xl font-bold">—</p>
        </div>
      </div>
    </div>
  );
}
