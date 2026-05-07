import Link from "next/link";

interface Course {
  id: string;
  title: string;
  description?: string;
  thumbnailUrl?: string;
  ratingAvg?: number;
  ratingCount: number;
}

async function fetchCourses(): Promise<Course[]> {
  const base = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080";
  try {
    const res = await fetch(`${base}/api/catalog/courses?size=20`,
      { next: { revalidate: 60 } });
    if (!res.ok) return [];
    const data = await res.json();
    return data.items ?? [];
  } catch { return []; }
}

export default async function Home() {
  const courses = await fetchCourses();
  return (
    <div>
      <h1 className="text-3xl font-bold mb-6">인기 강의</h1>
      {courses.length === 0 ? (
        <p className="text-gray-500">아직 등록된 강의가 없습니다.</p>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {courses.map(c => (
            <Link key={c.id} href={`/courses/${c.id}`}
                  className="block bg-white rounded-lg overflow-hidden shadow-sm hover:shadow-md transition">
              <div className="aspect-video bg-gray-200">
                {c.thumbnailUrl && (
                  <img src={c.thumbnailUrl} alt=""
                       className="w-full h-full object-cover" />
                )}
              </div>
              <div className="p-3">
                <h3 className="font-semibold text-sm line-clamp-2">{c.title}</h3>
                {c.ratingAvg !== undefined && c.ratingCount > 0 && (
                  <p className="text-xs text-gray-500 mt-1">
                    ⭐ {c.ratingAvg.toFixed(1)} ({c.ratingCount})
                  </p>
                )}
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
