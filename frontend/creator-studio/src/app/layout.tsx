import "./globals.css";
import type { Metadata } from "next";
import AutoLogin from "@/components/AutoLogin";

export const metadata: Metadata = {
  title: "lemuel academy — Creator Studio",
};

export default function RootLayout({
  children,
}: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body className="bg-gray-100 min-h-screen">
        <AutoLogin role="CREATOR" />
        <header className="bg-gray-900 text-white">
          <div className="max-w-7xl mx-auto px-4 py-3 flex items-center gap-6">
            <a href="/" className="font-bold">Creator Studio</a>
            <nav className="text-sm space-x-4">
              <a href="/" className="hover:underline">내 강의</a>
              <a href="/upload" className="hover:underline">영상 업로드</a>
              <a href="/courses/new" className="hover:underline">강의 등록</a>
            </nav>
            <span className="ml-auto text-xs text-gray-400">데모 크리에이터 자동 로그인</span>
          </div>
        </header>
        <main className="max-w-5xl mx-auto px-4 py-8">{children}</main>
      </body>
    </html>
  );
}
