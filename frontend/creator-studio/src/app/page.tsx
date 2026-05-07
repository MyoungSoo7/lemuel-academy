"use client";
import useSWR from "swr";

const fetcher = (url: string) => {
  const token = typeof window !== "undefined"
    ? localStorage.getItem("lqa_token") : null;
  return fetch(url, {
    headers: token ? { authorization: `Bearer ${token}` } : {},
  }).then(r => r.json());
};

export default function MyCourses() {
  const { data, isLoading } = useSWR("/api/catalog/courses?creator=me", fetcher);
  if (isLoading) return <p>로딩 중...</p>;
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">내 강의</h1>
      <a href="/courses/new"
         className="inline-block px-4 py-2 bg-blue-600 text-white rounded">
        + 새 강의 만들기
      </a>
      <div className="mt-6 space-y-2">
        {data?.items?.length ? data.items.map((c: any) => (
          <div key={c.id} className="bg-white p-4 rounded shadow-sm flex justify-between">
            <span>{c.title}</span>
            <span className="text-sm text-gray-500">{c.status}</span>
          </div>
        )) : <p className="text-gray-500">아직 만든 강의가 없어요.</p>}
      </div>
    </div>
  );
}
