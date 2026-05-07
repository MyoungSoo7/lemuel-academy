"use client";
import { useEffect } from "react";

export default function AutoLogin({ role = "CREATOR" }: { role?: string }) {
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
          window.location.reload();
        }
      })
      .catch(() => {});
  }, [role]);
  return null;
}
