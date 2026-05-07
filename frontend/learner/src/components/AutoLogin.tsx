"use client";
import { useEffect } from "react";

/**
 * Phase 1 MVP — 첫 진입시 자동으로 dev 유저로 로그인.
 * 운영 전환 시 이 컴포넌트 제거하고 진짜 OAuth/login 화면 사용.
 */
export default function AutoLogin({ role = "STUDENT" }: { role?: string }) {
  useEffect(() => {
    if (typeof window === "undefined") return;
    if (localStorage.getItem("lqa_token")) return;
    fetch(`/api/users/dev/auto-login?role=${role}`, { method: "POST" })
      .then(r => r.json())
      .then(d => {
        if (d.token) {
          localStorage.setItem("lqa_token", d.token);
          localStorage.setItem("lqa_user", JSON.stringify({
            id: d.userId, role: d.role, displayName: d.displayName,
          }));
          // 다른 컴포넌트가 토큰 변경 즉시 반영하도록 1회 리로드
          window.location.reload();
        }
      })
      .catch(() => {});
  }, [role]);
  return null;
}
