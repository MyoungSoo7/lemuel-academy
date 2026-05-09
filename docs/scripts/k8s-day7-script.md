# 쿠버네티스 7일차 — 영상 스크립트 (종합 프로젝트)

분량 75분 / 7개 레슨.

---

## 레슨 1 — Lemuel-Todo 프로젝트 개요 (8분)

**[CAM] 환영 (1m)** — "1~6일차의 모든 개념을 한 프로젝트로 묶습니다."
**[SLIDE] 아키텍처 (4m)** — 프론트(Next.js) + 백엔드(Spring Boot) + Postgres. 매핑 표 (1~6일차 → 적용처).
**[CAM] 학습 목표 (2m)** — "끝나면 본인 프로젝트를 그대로 클러스터에 올릴 수 있습니다."
**다음 (1m)** — "yaml 묶음을 어떻게 관리? Helm."

## 레슨 2 — Helm Chart 구조 (12분)

**훅 (1m)** — "yaml 10개 따로 관리하면 금방 깨집니다."
**[SLIDE] 구조 (3m)** — Chart.yaml / values.yaml / templates/.
**[TERM] init (4m)** — `helm create todo-saas`. 기본 구조 둘러보기.
**[CAM] 템플릿 변수 (3m)** — `{{ .Values.x }}` / `{{ .Release.Name }}` 가장 많이 쓰는 둘.
**마무리 (1m)** — "다음은 환경별 분리."

## 레슨 3 — values 분기 + 환경별 설정 (10분)

**[TERM] (5m)** — values-staging.yaml vs values-prod.yaml. replicas / resources / domain 차이.
**[SLIDE] (3m)** — `helm install -f values-prod.yaml` 가 어떻게 머지되는지.
**[CAM] 함정 (1m)** — values-prod 가 git 에 들어갈 때 secret 은 SOPS 로.
**마무리 (1m)** — "다음 레슨 — 1~6일차를 한 chart 에."

## 레슨 4 — 1~6일차 통합 (15분)

**[TERM] templates 작성 (12m)** — 한 파일씩:
1. deployment-frontend.yaml + Service ClusterIP
2. deployment-backend.yaml + readinessProbe (4일차)
3. statefulset-postgres.yaml + PVC RWO (3일차)
4. configmap.yaml + secret.yaml (3일차)
5. ingress.yaml (2일차)
6. networkpolicy.yaml (6일차)
7. servicemonitor.yaml (5일차)
**[TERM] install (2m)** — `helm install todo ./todo-saas -n staging`. 모든 리소스 한 번에.
**검증 (1m)** — `kubectl get all -n staging`.

## 레슨 5 — GitOps with ArgoCD (12분)

**훅 (1m)** — "수동 helm upgrade — 누가 언제 뭘 배포했는지 추적이 안 된다."
**[SLIDE] 개념 (3m)** — git = source of truth. ArgoCD = git → cluster sync 봇.
**[TERM] Application yaml (5m)** — repoURL, path, targetRevision, automated sync.
**[CAM] 효과 (2m)** — PR 머지 = 자동 배포 / git revert = 즉시 롤백 / 모든 변경 git 히스토리.
**마무리 (1m)** — "다음은 자동 동기화 옵션."

## 레슨 6 — automated sync (prune / selfHeal) (8분)

**[SLIDE] (3m)** — prune (git 에서 지우면 클러스터에서도 삭제), selfHeal (수동 변경 무시 + 복원).
**[TERM] (4m)** — Application 에 `automated: { prune: true, selfHeal: true }` 추가, 수동으로 pod scale 변경 → 자동 복원되는 모습 시연.
**경고 (1m)** — selfHeal=true 면 디버깅 시 인내심 필요.

## 레슨 7 — 다음 학습 로드맵 (10분)

**[CAM] 7일이 끝났습니다 (2m)** — 자축 + 정리.
**[SLIDE] 1주일 안 (3m)** — Helm 직접 만들기 / Grafana 4 알람 / NetworkPolicy default-deny / PSA 등급 올리기.
**[SLIDE] 1개월 안 (3m)** — Service Mesh (Istio/Linkerd) / Tracing (OpenTelemetry+Tempo) / HPA / Cluster Autoscaler / Velero 백업.
**[SLIDE] 자격증/책 (2m)** — Kubernetes in Action / CKAD → CKA.
**[CAM] 마무리 (1m)** — "수고하셨습니다. 진짜 학습은 본인 프로젝트를 클러스터에 올리는 순간 시작됩니다. 또 만나요."
