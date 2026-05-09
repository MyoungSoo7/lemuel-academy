# 쿠버네티스 6일차 — 영상 스크립트 (보안)

분량 75분 / 7개 레슨.

---

## 레슨 1 — RBAC 4총사 (12분)

**훅 (1m)** — "그 클러스터 전권 토큰이 git 에 있었습니다."
**개념 (4m)** — [SLIDE] Role / ClusterRole / RoleBinding / ClusterRoleBinding. Subject = User/Group/ServiceAccount.
**[TERM] 예제 (5m)** — `pod-reader` Role + `dev-can-read` Binding. `kubectl auth can-i get pods --as dev-1@lemuel.co.kr -n prod`.
**Least Privilege (1m)** — [CAM] "필요한 만큼만, 더는 안 준다."
**마무리 (1m)** — "다음은 앱의 신원, ServiceAccount."

## 레슨 2 — ServiceAccount 패턴 (10분)

**훅 (1m)** — "default ServiceAccount 의 함정?"
**문제 (2m)** — [SLIDE] 모든 Pod 가 default 사용 → 권한 누수.
**[TERM] 패턴 (5m)** — Pod 마다 전용 SA + 최소 권한 Role.
**resourceNames 조건 (1m)** — 특정 Secret 만 읽게 좁히기.
**마무리 (1m)** — "다음은 네트워크 격리."

## 레슨 3 — NetworkPolicy + CNI 함정 (12분)

**훅 (1m)** — "기본은 모든 Pod 가 모든 Pod 와 통신 가능. 위험."
**[TERM] yaml (4m)** — frontend → backend 만 허용하는 ingress 룰.
**default-deny 패턴 (3m)** — [SLIDE] empty selector + ingress: [] = 전부 차단 후 화이트리스트로 풀기.
**CNI 함정 (3m)** — [SLIDE] Flannel ❌ / Calico ✅ / Cilium ✅✅. yaml 만 적용해도 CNI 미지원이면 무용지물.
**마무리 (1m)** — "다음은 Secret 보안 강화."

## 레슨 4 — Secret 보안 (12분)

**훅 (1m)** — "base64 가 암호화가 아니라는 얘기, 또 한번."
**3 옵션 비교 (3m)** — [SLIDE] etcd 암호화 / SOPS+age / Sealed Secrets.
**SOPS 데모 (5m)** — [TERM] age 키 생성 → secret.yaml 평문 → `sops -e` → 암호화 → git 커밋 → `sops -d | kubectl apply -f -`.
**ExternalSecrets 한 발 (2m)** — [SLIDE] AWS Secrets Manager / Vault 와 연동하는 ExternalSecret CRD.
**마무리 (1m)** — "다음은 컨테이너 자체 권한."

## 레슨 5 — Pod Security Admission + securityContext (10분)

**훅 (1m)** — "컨테이너 안에서 root 면 어떻게 됩니까?"
**PSA 3 등급 (3m)** — [SLIDE] privileged / baseline / restricted 표.
**[TERM] 적용 (3m)** — Namespace 라벨 한 줄로 enforce.
**securityContext (2m)** — [SLIDE] runAsNonRoot, readOnlyRootFilesystem, capabilities drop.
**마무리 (1m)** — "이 둘이 컨테이너 탈출 공격 99% 차단."

## 레슨 6 — Audit + 감사 로그 (8분)

**개념 (3m)** — [SLIDE] kube-apiserver audit policy. 누가 언제 무엇을 호출했는지 기록.
**[TERM] policy yaml (3m)** — RequestResponse / Metadata 레벨, 리소스별 분기.
**확인 (1m)** — `kubectl logs -n kube-system kube-apiserver-* | grep audit`.
**마무리 (1m)** — "사고 후 추적의 기반."

## 레슨 7 — 운영 체크리스트 7가지 (11분)

**[CAM] 한 번에 (8m)** — 7가지 체크리스트 (블로그 6일차 글 그대로):
1. kubeconfig 사용자 분리
2. cluster-admin 1~2명만
3. Pod 마다 전용 SA + 최소 권한
4. CNI 가 NetworkPolicy 지원 + default-deny
5. SOPS / Sealed Secrets / etcd 암호화 중 1
6. Namespace PSA `restricted` 이상
7. runAsNonRoot + drop capabilities ALL

**[CAM] 마무리 (3m)** — "이 7개를 통과시킨 클러스터만 production 에 올리세요. 다음은 종합 프로젝트로 마무리."
