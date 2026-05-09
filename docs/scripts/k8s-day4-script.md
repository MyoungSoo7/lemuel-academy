# 쿠버네티스 4일차 — 영상 스크립트 (무중단 배포)

분량 75분 / 7개 레슨.

---

## 레슨 1 — RollingUpdate 의 흐름 (10분)

**훅 (1m)** — "deploy 한 번에 502 가 나는 이유?"
**기본 동작 (3m)** — [SLIDE] strategy.type: RollingUpdate. 옛 RS 는 점차 줄고, 새 RS 가 점차 늘어남. 10개 → 점진 교체 애니메이션.
**[TERM] 시연 (5m)** — `kubectl set image deploy/web app=v2` 후 `kubectl rollout status` 실시간 watch.
**마무리 (1m)** — "기본값이지만 두 옵션을 만지면 속도가 달라집니다."

## 레슨 2 — maxSurge / maxUnavailable (10분)

**훅 (1m)** — "배포 속도 X 안전성 — 두 다이얼."
**개념 (3m)** — [SLIDE] 25% / 25% 기본 → 100%/0% 빠름 위험 → 0%/25% 느림 안전. 표.
**[TERM] 실습 (5m)** — yaml 수정 후 다시 deploy, `kubectl describe rs` 로 surge 동작 확인.
**마무리 (1m)** — "트래픽 큰 서비스는 보수적으로. CI 비번 서비스는 공격적으로."

## 레슨 3 — readinessProbe 가 핵심 (10분)

**훅 (1m)** — "readinessProbe 없으면 RollingUpdate 가 무중단이 아니라 중단입니다."
**원리 (3m)** — [SLIDE] kube-proxy 가 트래픽 받을지 결정 = readiness 통과 여부. 시작 중인 Pod 도 ready 되기 전엔 트래픽 X.
**[TERM] 시연 (5m)** — readinessProbe 없는 yaml 로 deploy → 502 발생. probe 추가 → 502 사라짐.
**마무리 (1m)** — "모든 Pod 에 readinessProbe — 운영 시작 전 필수."

## 레슨 4 — Canary 배포 (12분)

**훅 (1m)** — "신버전 5% 사용자한테만 보낼 수 있나요?"
**A 방법 — replica 비율 (4m)** — [SLIDE] stable 9 + canary 1 = 90:10. 같은 라벨 → Service 가 둘 다 라우팅. 거칠지만 yaml 만으로 가능.
**B 방법 — Service Mesh (5m)** — [SLIDE] Istio VirtualService weight 5/95. 정확한 % 제어. flagger 같은 자동 분석 + 점증.
**언제 뭘 (1m)** — "MVP 는 A. 트래픽 큰 서비스는 B."
**마무리 (1m)** — "다음은 두 환경 통째로 띄우는 Blue-Green."

## 레슨 5 — Blue-Green 배포 (10분)

**훅 (1m)** — "롤백 즉시? 그럼 두 환경 운영해야."
**개념 (3m)** — [SLIDE] blue (v1, 100% 트래픽) + green (v2, 0%) 동시 가동. Service selector 한 줄 변경 = 전환.
**[TERM] 시연 (5m)** — `kubectl patch service web -p '{"spec":{"selector":{"color":"green"}}}'`. v2 → v1 즉시 복원.
**비용 (1m)** — "인프라 2배. 결제·금융처럼 복원이 초 단위로 중요한 곳."

## 레슨 6 — Rollback 한 줄 + revision (8분)

**[TERM] (5m)** — `kubectl rollout history deploy/web` → 리비전 목록. `kubectl rollout undo deploy/web --to-revision=3`.
**revisionHistoryLimit (2m)** — [SLIDE] 기본 10. 너무 많이 두면 etcd 부하, 너무 적으면 롤백 불가.
**마무리 (1m)** — "롤백 못 하면 무중단도 의미 없습니다."

## 레슨 7 — Graceful Shutdown 패턴 (15분)

**훅 (1m)** — "RollingUpdate 잘 굴렸는데 502 가 가끔 난다면?"
**원인 (4m)** — [SLIDE] Pod 종료 흐름: SIGTERM → endpoint 제거(약간 지연) → 컨테이너 종료. 그 사이 in-flight 요청이 끊김.
**해결 (5m)** — [SLIDE] preStop sleep 10s + readinessProbe 503 패턴. terminationGracePeriodSeconds: 30.
**[TERM] yaml (4m)** — lifecycle.preStop + 앱 코드 SIGTERM 핸들러.
**다음회 (1m)** — "5일차 — 배포가 잘됐는지 어떻게 알죠? 모니터링 + 로깅."
