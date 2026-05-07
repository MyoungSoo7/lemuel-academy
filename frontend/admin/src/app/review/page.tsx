"use client";
import useSWR, { mutate } from "swr";

const fetcher = (url: string) => {
  const t = typeof window !== "undefined"
    ? localStorage.getItem("lqa_token") : null;
  return fetch(url, { headers: t ? { authorization: `Bearer ${t}` } : {} })
    .then(r => r.json());
};

export default function ReviewQueue() {
  const { data } = useSWR("/api/catalog/courses?status=REVIEW", fetcher);
  async function review(id: string, approved: boolean) {
    const t = localStorage.getItem("lqa_token") ?? "";
    await fetch(`/api/catalog/courses/${id}/review?approved=${approved}`, {
      method: "PATCH",
      headers: { authorization: `Bearer ${t}` },
    });
    mutate("/api/catalog/courses?status=REVIEW");
  }
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">검수 큐</h1>
      <table className="w-full bg-slate-800 rounded">
        <thead>
          <tr className="text-left border-b border-slate-700">
            <th className="p-2">제목</th>
            <th className="p-2">크리에이터</th>
            <th className="p-2">제출일</th>
            <th className="p-2">조치</th>
          </tr>
        </thead>
        <tbody>
          {data?.items?.map((c: any) => (
            <tr key={c.id} className="border-b border-slate-700">
              <td className="p-2">{c.title}</td>
              <td className="p-2 text-xs">{c.creatorId.slice(0, 8)}…</td>
              <td className="p-2">{new Date(c.createdAt).toLocaleDateString()}</td>
              <td className="p-2 space-x-2">
                <button onClick={() => review(c.id, true)}
                        className="px-3 py-1 bg-green-600 rounded text-sm">승인</button>
                <button onClick={() => review(c.id, false)}
                        className="px-3 py-1 bg-red-600 rounded text-sm">반려</button>
              </td>
            </tr>
          )) ?? <tr><td colSpan={4} className="p-4 text-center text-slate-500">검수 대기 강의 없음</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
