import "./globals.css";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "lemuel academy",
  description: "동영상 강의 플랫폼",
};

export default function RootLayout({
  children,
}: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body className="bg-gray-50 text-gray-900 min-h-screen">
        <header className="border-b bg-white">
          <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
            <a href="/" className="text-xl font-bold">lemuel academy</a>
            <nav className="space-x-4 text-sm">
              <a href="/" className="hover:underline">강의 둘러보기</a>
              <a href="/login" className="hover:underline">로그인</a>
            </nav>
          </div>
        </header>
        <main className="max-w-7xl mx-auto px-4 py-8">{children}</main>
        <footer className="border-t bg-white mt-16">
          <div className="max-w-7xl mx-auto px-4 py-6 text-sm text-gray-500">
            © {new Date().getFullYear()} lemuel academy
          </div>
        </footer>
      </body>
    </html>
  );
}
