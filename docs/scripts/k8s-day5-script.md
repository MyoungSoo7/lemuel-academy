# 쿠버네티스 5일차 — 영상 스크립트 (Observability)

분량 75분 / 7개 레슨.

---

## 레슨 1 — Observability 3 기둥 (8분)

**훅 (1m)** — "장애가 났는지 모르는 게 가장 무섭다."
**개념 (4m)** — [SLIDE] metrics / logs / traces 표 + 도구 매핑. 각 기둥의 질문이 다름 ("얼마나" / "무엇을" / "어디서").
**오늘 다룰 범위 (2m)** — "1, 2일차는 metrics+logs. traces 는 마이크로서비스 정착 후."
**마무리 (1m)** — "다음은 metrics 의 표준, Prometheus."

## 레슨 2 — Prometheus Pull 방식 (12분)

**훅 (1m)** — "왜 Push 가 아니라 Pull?"
**개념 (4m)** — [SLIDE] Pull 의 장점: target health 자동 감지, 인증 단순, 대량 push 폭주 방지. 단점: 외부망 노출 어려움.
**[TERM] 메트릭 노출 (5m)** — Spring Boot actuator + micrometer-registry-prometheus, `/actuator/prometheus` 출력 확인.
**스크랩 설정 (1m)** — [SLIDE] static config vs ServiceMonitor.
**마무리 (1m)** — "다음 레슨 ServiceMonitor 로 자동화."

## 레슨 3 — ServiceMonitor (10분)

**훅 (1m)** — "Pod 가 자동 스케일되는데 Prometheus 도 자동 발견해야."
**개념 (3m)** — [SLIDE] Prometheus Operator + ServiceMonitor CRD 흐름.
**[TERM] yaml (5m)** — selector 라벨로 Service 매칭, endpoints 의 path/port/interval 설정.
**확인 (1m)** — Prometheus targets 페이지에서 발견됨.

## 레슨 4 — PromQL 기초 (12분)

**훅 (1m)** — "메트릭 보면 그래프가 너무 많아요. 어떻게 압축?"
**3가지 핵심 함수 (5m)** — [SLIDE] rate / sum / histogram_quantile. 각 1줄 예시.
**[TERM] 실습 (5m)** — Grafana Explore 에서:
```
rate(http_requests_total[5m])
sum(rate(http_requests_total{status=~"5.."}[5m])) / sum(rate(http_requests_total[5m]))
histogram_quantile(0.95, sum by (le) (rate(http_request_duration_seconds_bucket[5m])))
```
**마무리 (1m)** — "이 3개로 운영 80% 가 됩니다."

## 레슨 5 — Grafana 대시보드 + 알람 (10분)

**훅 (1m)** — "공식 추천 대시보드 ID 4개만 알아두세요."
**대시보드 import (3m)** — [TERM] 15760 / 15757 / 15759 / 12740 import.
**알람 룰 (4m)** — [SLIDE] PrometheusRule yaml. for: 5m, severity, annotations.
**알람 채널 (1m)** — [SLIDE] Slack / Telegram / 이메일. 르무엘은 텔레그램 봇.
**마무리 (1m)** — "다음은 metrics 의 형제, 로그."

## 레슨 6 — Loki + LogQL (10분)

**훅 (1m)** — "ELK 너무 무겁다. 가벼운 대안 Loki."
**왜 가볍냐 (3m)** — [SLIDE] 본문 인덱싱 X, 라벨만. 디스크 1/10 수준.
**LogQL (3m)** — [SLIDE] `{app="web"}`, `|=`, `|!`, regex 매칭, rate() 시계열 변환.
**[TERM] 실습 (2m)** — Grafana 에서 같은 화면에 metrics 와 logs 동시 표시.
**마무리 (1m)** — "마지막 레슨, 한 번에 다 깔자."

## 레슨 7 — kube-prometheus-stack + 4가지 필수 알람 (13분)

**[TERM] Helm 설치 (4m)**:
```bash
helm install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace
helm install loki grafana/loki-stack -n monitoring --set promtail.enabled=true
```
**Grafana 접근 (2m)** — `kubectl port-forward`. 첫 로그인.
**4가지 필수 알람 yaml (5m)** — [TERM]
1. Pod CrashLoopBackOff
2. Node 메모리/디스크 90%
3. 5xx 에러율 5%
4. 배포 후 30분 내 ErrorRate 급증
**마무리 (2m)** — "이 4개만 자동화해도 새벽 호출 절반은 줄어듭니다. 다음은 보안."
