import VideoPlayer from "@/components/VideoPlayer";
import { notFound } from "next/navigation";

interface Lesson {
  id: string; title: string; videoId: string | null;
  durationSec: number | null; displayOrder: number; isPreview: boolean;
}
interface Chapter {
  id: string; title: string; displayOrder: number; lessons: Lesson[];
}
interface CourseDetail {
  course: { id: string; title: string; description: string | null;
            ratingAvg: number | null; ratingCount: number; };
  chapters: Chapter[];
}

async function fetchCourse(id: string): Promise<CourseDetail | null> {
  const base = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080";
  const res = await fetch(`${base}/api/catalog/courses/${id}`,
    { next: { revalidate: 30 } });
  if (!res.ok) return null;
  return res.json();
}

async function fetchVideo(id: string) {
  const base = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080";
  const res = await fetch(`${base}/api/media/videos/${id}`,
    { next: { revalidate: 0 } });
  if (!res.ok) return null;
  return res.json();
}

export default async function CoursePage(
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;
  const detail = await fetchCourse(id);
  if (!detail) notFound();

  const firstLesson = detail.chapters[0]?.lessons[0];
  const videoMeta = firstLesson?.videoId
    ? await fetchVideo(firstLesson.videoId)
    : null;

  return (
    <div className="grid lg:grid-cols-3 gap-8">
      <div className="lg:col-span-2 space-y-4">
        <h1 className="text-2xl font-bold">{detail.course.title}</h1>
        {detail.course.description && (
          <p className="text-gray-600">{detail.course.description}</p>
        )}
        {videoMeta?.hlsMasterUrl ? (
          <VideoPlayer src={videoMeta.hlsMasterUrl} lessonId={firstLesson!.id} />
        ) : (
          <div className="aspect-video bg-gray-100 rounded flex items-center justify-center text-gray-500">
            영상이 아직 준비되지 않았습니다
          </div>
        )}
      </div>
      <aside className="space-y-4">
        <h2 className="font-bold text-lg">커리큘럼</h2>
        {detail.chapters.map(ch => (
          <div key={ch.id} className="bg-white rounded p-3 shadow-sm">
            <h3 className="font-semibold mb-2">{ch.title}</h3>
            <ul className="space-y-1 text-sm">
              {ch.lessons.map(l => (
                <li key={l.id} className="flex justify-between">
                  <span>{l.title}{l.isPreview && " 🎬"}</span>
                  {l.durationSec && (
                    <span className="text-gray-500">
                      {Math.floor(l.durationSec / 60)}분
                    </span>
                  )}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </aside>
    </div>
  );
}
