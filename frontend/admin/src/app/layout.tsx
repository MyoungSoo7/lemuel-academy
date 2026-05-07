import "./globals.css";
import AutoLogin from "@/components/AutoLogin";

export default function RootLayout({
  children,
}: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body className="bg-slate-900 text-slate-100 min-h-screen">
        <AutoLogin role="ADMIN" />
        <header className="bg-red-700">
          <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
            <span className="font-bold">⚠️ admin — lemuel academy</span>
            <span className="text-xs text-red-100">데모 관리자 자동 로그인</span>
          </div>
        </header>
        <nav className="bg-slate-800">
          <div className="max-w-7xl mx-auto px-4 py-2 text-sm space-x-4">
            <a href="/" className="hover:underline">대시보드</a>
            <a href="/review" className="hover:underline">검수 큐</a>
            <a href="/users" className="hover:underline">사용자</a>
          </div>
        </nav>
        <main className="max-w-7xl mx-auto px-4 py-8">{children}</main>
      </body>
    </html>
  );
}
