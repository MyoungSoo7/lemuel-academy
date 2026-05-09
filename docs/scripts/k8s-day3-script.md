# 쿠버네티스 3일차 — 영상 스크립트 (ConfigMap / Secret / Volume / PVC)

분량 75분 / 7개 레슨. 표기: `[CAM]` / `[SLIDE]` / `[TERM]` / `[B-ROLL]`.

---

## 레슨 1 — ConfigMap (10분)

**훅 (1m)** — [CAM] "도커 이미지에 DATABASE_URL 박은 적 있죠? 환경별로 이미지 새로 빌드해 본 분, 손." → 안티패턴 짚기.
**개념 (3m)** — [SLIDE] envFrom vs 파일 마운트 비교. 두 방법의 갱신 동작 차이.
**실습 (5m)** — [TERM] `kubectl create configmap app-config --from-literal=LOG_LEVEL=INFO`. yaml 변환 후 `envFrom` 적용. 컨테이너 안에서 `env | grep LOG_LEVEL`.
**마무리 (1m)** — "환경변수 vs 파일 — 갱신 시 재시작 필요 vs ~1분 자동 반영. 다음은 비밀번호용 형제 Secret."

## 레슨 2 — Secret + base64 함정 (12분)

**훅 (1m)** — "Secret 이 안전하다고 들었죠? 거짓말이에요. base64 는 암호화가 아닙니다."
**개념 (3m)** — [SLIDE] ConfigMap vs Secret 표 (저장 형식, RBAC, tmpfs).
**실습 (4m)** — [TERM] `kubectl create secret generic db-cred --from-literal=password=...`. yaml 으로 변환 → base64 디코딩 시연 ("누구나 풀 수 있다").
**보안 강화 (3m)** — [SLIDE] etcd 암호화 / SOPS+age / Sealed Secrets 비교. 르무엘은 SOPS+age 사용.
**마무리 (1m)** — "Secret yaml 을 git 에 그냥 올리지 말 것. 다음은 Pod 가 죽어도 살아남는 데이터."

## 레슨 3 — Volume 종류 4가지 (10분)

**훅 (1m)** — "Pod 가 죽으면 컨테이너 안 디스크는 다 사라져요. DB 는 어떻게 살죠?"
**4 종 비교 (5m)** — [SLIDE] emptyDir / hostPath / configMap-secret / **PVC** 표. 수명/용도/예시.
**emptyDir 실습 (3m)** — [TERM] sidecar 로그 수집 패턴. 두 컨테이너가 `/var/log/app` 공유.
**마무리 (1m)** — "운영 90% 는 PVC. 다음 레슨에서 자세히."

## 레슨 4 — PV / PVC + Dynamic Provisioning (12분)

**훅 (1m)** — "EBS, GCE PD, Azure Disk… 다 다른데 한 번에 다루려면?"
**PV/PVC 분리 (3m)** — [SLIDE] 공급(PV) vs 요구(PVC) 비유 — 부동산 매물 vs 임차 신청.
**실습 (5m)** — [TERM] 20Gi PVC 만들어 `Bound` 까지. `kubectl get pv,pvc -o wide`.
**Dynamic Provisioning (2m)** — [SLIDE] StorageClass 가 자동 PV 생성하는 흐름도. 클라우드별 default class 차이.
**마무리 (1m)** — "PVC 한 줄로 클라우드 디스크 자동 할당. 다음은 동시 접근 모드."

## 레슨 5 — AccessMode + StatefulSet 한 발 (8분)

**훅 (1m)** — "DB 를 Deployment 로 띄우면 안 되는 이유?"
**AccessMode 3종 (3m)** — [SLIDE] RWO / ROX / RWX 표 + 운영 케이스.
**StatefulSet 살짝 (3m)** — [SLIDE] Deployment vs StatefulSet (이름 안정성, ordered restart, headless service).
**마무리 (1m)** — "Postgres 같은 stateful 은 StatefulSet + RWO PVC. 다음 레슨에서 통합."

## 레슨 6 — 통합 yaml 한 통 (15분)

**훅 (1m)** — "오늘 배운 4종을 한 yaml 에 다 담아봅시다."
**작성 (8m)** — [TERM] postgres-stack.yaml 작성:
1. ConfigMap (POSTGRES_DB)
2. Secret (POSTGRES_USER, POSTGRES_PASSWORD, base64)
3. PVC 5Gi
4. Deployment + envFrom + volumeMounts
**적용/검증 (5m)** — [TERM] `kubectl apply` → `pvc Bound` → Pod Running → `kubectl exec -it postgres -- psql -U lemuel`.
**Pod 죽이기 시연 (1m)** — `kubectl delete pod -l app=postgres` → 새 Pod 가 같은 PVC 마운트 → 데이터 살아있음.

## 레슨 7 — 정리 + 4일차 예고 (8분)

**복습 (3m)** — [SLIDE] 4종 카드.
**현장 함정 (4m)** — [CAM] "ConfigMap 갱신 시 envFrom 재시작 / Secret base64 ≠ 암호화 / RWX 안 쓰면 NFS 깔지 마라 / RWO 인데 replicas:2 면 두 번째 Pod 가 영원히 Pending".
**다음회 예고 (1m)** — "데이터까지 영속시켰습니다. 4일차는 새 버전을 끊김 없이 배포하는 무중단 배포 4 패턴."
