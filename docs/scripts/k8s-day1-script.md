# 쿠버네티스 1일차 — 영상 스크립트

> 목표: 입문자가 클러스터 아키텍처를 직관으로 잡게 하는 75분 분량 7개 레슨.
> 톤: 친근한 반말 X, 부드러운 존대말. 화면 옆에 화이트보드/슬라이드 + 터미널 데모.
> 표기: `[CAM]` 카메라, `[SLIDE]` 슬라이드, `[TERM]` 터미널 데모, `[B-ROLL]` 보조영상.
> 분량 합계: 약 75분. 각 레슨은 [편집 후] 7~12분 권장.

---

## 레슨 1 — 왜 도커만으론 부족한가 (8분, preview)

### 0:00 – 0:30  훅 (Hook)
[CAM]
> "여러분, 도커는 잘 쓰고 계신가요? 그런데 만약, 도커 컨테이너가 100개라면? 그 중 한두 개가 새벽 3시에 갑자기 죽으면 누가 살릴까요? 오늘은 그 질문의 답을 함께 찾아갑니다."

### 0:30 – 2:00  도커가 만든 혁신
[SLIDE: "Docker — 어디서든 똑같이 돌아간다"]
> "도커 덕분에 우리는 '내 컴퓨터에선 되는데...' 라는 농담을 거의 졸업했습니다. 컨테이너가 의존성을 통째로 패키징하니까요. 그런데 운영 규모로 가면 새 문제 5개가 한꺼번에 터집니다."

### 2:00 – 5:30  5가지 운영 시나리오
[SLIDE: 5x2 비교표 — Docker vs Kubernetes]

각 행을 **현장 사례** 와 함께 설명:

1. "앱이 갑자기 죽음" → "수동 재시작 vs 자동 복구"
   - 사례: "새벽 3시에 OOM 으로 컨테이너가 죽는다고 상상해보세요. 도커만으론 누가 깨워줘야..."
2. "트래픽 폭주" → "수동 복사 vs 명령 한 번"
3. "배포 중 다운타임" → "재시작 시 중단 vs 무중단 롤링"
4. "서버가 여러 대" → "사람이 결정 vs 자동 스케줄"
5. "설정 관리" → "서버마다 수정 vs 중앙 배포"

### 5:30 – 7:00  k8s 라는 이름의 유래
[SLIDE: k → 8 → s]
> "쿠버네티스는 그리스어로 '조타수' 입니다. 컨테이너라는 큰 배의 키를 쥐고 방향을 잡는 사람이죠. 줄여서 k8s 라고 부르는데, k 와 s 사이에 글자가 8개라서 그렇습니다. i18n, l10n 같은 IT 업계 별명 작명법이에요."

### 7:00 – 8:00  요약 + 다음 영상 예고
[CAM]
> "정리하면 컨테이너 오케스트레이션 = 컨테이너 100개를 사람 손 안 타고 굴리는 시스템입니다. 다음 영상에서는 쿠버네티스가 풀어주는 6가지 큰 문제를 보겠습니다."

---

## 레슨 2 — k8s 등장 배경 + 6대 해결 영역 (9분)

### 0:00 – 1:30  Google Borg → Kubernetes
[SLIDE: 2014 / Google / Borg → k8s]
> "쿠버네티스는 2014년 구글이 오픈소스로 공개한 프로젝트입니다. 구글 내부에서 10년 넘게 굴리던 'Borg' 라는 시스템의 노하우를 외부에 공개한 셈이죠. 그래서 처음부터 '대규모를 견디는 설계' 가 깔려있어요."

### 1:30 – 3:00  쿠버네티스를 부른 3가지 환경 변화
[SLIDE: 컨테이너 부상 / 마이크로서비스 / 클라우드 보편화]
> "이 3가지가 만나면서 '여러 클라우드, 여러 서버, 여러 마이크로서비스' 라는 복잡도가 폭발했습니다. 누가 정리해줄 도구가 절실했죠."

### 3:00 – 8:00  6대 해결 영역 (각 50초씩)
[SLIDE: 6개 카드]
1. **자동 롤아웃/롤백** — "버튼 한 번에 v2, 문제 생기면 한 번에 v1 복귀"
2. **로드 밸런싱 + 디스커버리** — "내부 DNS 자동, 트래픽 자동 분산"
3. **스토리지 오케스트레이션** — "Pod 가 옮겨가도 디스크가 따라간다"
4. **빈 패킹** — "CPU/메모리 보고 가장 적합한 노드에 배치"
5. **자동 복구** — "컨테이너 죽으면 재시작, 노드 죽으면 다른 노드로"
6. **시크릿/구성 관리** — "비밀번호를 코드에 박지 않고 안전하게"

### 8:00 – 9:00  마무리
[CAM]
> "이 6개를 한 줄로 줄이면 '사람이 할 일을 시스템이 대신' 입니다. 다음 영상에서는 그 시스템 안에 누가 사는지 들여다봅니다."

---

## 레슨 3 — Master/Worker 노드 구조 한눈에 (10분)

### 0:00 – 1:00  비유로 시작
[CAM]
> "쿠버네티스 클러스터를 회사로 비유하면, **Control Plane 은 본사 (마스터 노드)** 이고, **Worker Node 는 지사 (실행 부대)** 입니다. 본사는 결정을 내리고, 지사는 실제 일을 합니다."

### 1:00 – 3:30  Master 4총사 [SLIDE 다이어그램 위에 한 명씩 등장]
- API server: "정문 경비. 모든 명령은 여기를 통과"
- etcd: "회사의 모든 기록이 저장된 금고"
- Scheduler: "어느 지사로 일을 보낼지 정하는 배치 담당자"
- Controller Manager: "실제 상태와 약속한 상태가 같은지 계속 감시"

### 3:30 – 6:30  Worker 3총사
- kubelet: "지사장. 본사 명령을 받아 실제 컨테이너 띄움"
- kube-proxy: "지사 내부 네트워크 담당. 트래픽 라우팅"
- container runtime: "도커/containerd — 실제 컨테이너를 돌리는 엔진"

### 6:30 – 9:00  명령 한 줄이 흐르는 길 [TERM 데모: kubectl apply]
[B-ROLL: 마우스로 yaml 파일 적용 → 화면에 단계별 흐름]
> "kubectl apply 한 줄을 치면 → API server 에 도착 → etcd 저장 → Scheduler 가 노드 결정 → 그 노드의 kubelet 이 명령 받음 → runtime 이 컨테이너 실행. 그 사이 controller manager 는 백그라운드에서 계속 '약속한 상태와 일치하는가?' 를 감시합니다."

### 9:00 – 10:00  요약
[CAM]
> "이 7명이 모든 일을 해줍니다. 다음 두 영상은 본사(Control Plane)와 지사(Worker)를 각각 자세히 보겠습니다."

---

## 레슨 4 — Control Plane 4 컴포넌트 자세히 (12분)

### 0:00 – 0:45  요약 슬라이드 복습
[SLIDE: API server / etcd / Scheduler / Controller Manager 4박스]

### 0:45 – 4:00  API Server
- 모든 통신의 정문, RESTful API
- 인증 → 권한 → 검증 → 저장 (4단계)
- [TERM] `kubectl get pods --v=8` 으로 실제 API 호출 보기
- "API server 가 죽으면? → 클러스터가 멈춥니다. 그래서 마스터 HA 가 중요"

### 4:00 – 7:00  etcd
- 키-값 저장소, 모든 클러스터 상태가 여기 들어있음
- [SLIDE: "이 클러스터의 진실은 etcd 에 있다"]
- raft 합의 알고리즘, 그래서 홀수 권장 (다음 레슨 6에서 자세히)
- [TERM] `etcdctl get /registry/pods/default/` (운영 환경에서만)

### 7:00 – 9:30  Scheduler
- 새 Pod 가 생길 때 어느 노드?
- 결정 기준: CPU/Memory 요구량 / Node Selector / Taint & Toleration / Affinity
- [SLIDE: 5개 필터로 노드 후보 좁히는 과정]

### 9:30 – 11:30  Controller Manager
- 여러 컨트롤러를 묶음: Deployment, ReplicaSet, Node, Service Account, …
- 핵심 패턴: "현재 상태 vs 원하는 상태 = reconcile loop"
- [B-ROLL: 무한루프 애니메이션]

### 11:30 – 12:00  마무리
> "본사 4명이 끝없이 reconcile 을 돌려서 클러스터를 약속한 상태로 유지합니다. 다음은 지사 차례."

---

## 레슨 5 — Worker Node 3총사 (9분)

### 0:00 – 1:00  복습
[SLIDE: kubelet / kube-proxy / runtime]

### 1:00 – 4:00  kubelet
- 본사로부터 Pod spec(yaml/json) 을 받아 실행
- 컨테이너 헬스체크, 라이프사이클 (start/stop/restart/delete)
- [TERM] `journalctl -u kubelet -f` 로 실제 로그 보기
- 에러 시 본사에 보고

### 4:00 – 6:30  kube-proxy
- iptables / IPVS 로 네트워크 규칙 관리
- 서비스 IP → 백엔드 Pod IP 로 라우팅
- 로드밸런싱은 여기서 일어남
- [SLIDE: 트래픽 흐름도]

### 6:30 – 8:30  Container Runtime
- Docker (deprecated since v1.20) → containerd / CRI-O
- CRI 표준 인터페이스로 kubelet 과 대화
- 이미지 풀, 컨테이너 라이프사이클 실행
- [B-ROLL: containerd 가 실제로 컨테이너 띄우는 모습]

### 8:30 – 9:00  마무리
> "지사 3명이 본사 명령을 받아 실제로 컨테이너를 굴립니다. 다음은 본사를 여러 개 둬서 살아남는 법, HA 구성을 봅니다."

---

## 레슨 6 — 마스터 HA + 짝수의 함정 (10분)

### 0:00 – 1:00  왜 마스터 HA?
[CAM]
> "본사 1명이 죽으면 회사가 멈춥니다. 본사도 여러 명을 둬야겠죠. 그런데 여러 명이 똑같은 결정을 내리면 충돌이 납니다. 어떻게 풀까요?"

### 1:00 – 3:30  Active-Active vs Leader Election
[SLIDE: 두 패턴 비교]
- API server: 모두 Active. 앞에 HAProxy 로드밸런서.
- Scheduler / Controller-Manager: Leader Election. 한 명만 일하고 나머지 standby.

### 3:30 – 6:00  Keepalived + HAProxy + VIP
[SLIDE: VIP 192.168.0.100 그림]
- 가상 IP 한 개를 마스터끼리 공유
- 마스터 1번 죽으면 → Keepalived 감지 → VIP 를 마스터 2번에 넘김
- 워커 노드는 IP 변화 모름, 끊김 없음

### 6:00 – 9:00  짝수의 함정 (etcd quorum)
[SLIDE: 1/3/4/5/7 견딜 수 있는 장애 표]
- 4대 vs 3대: **둘 다 1대만 죽어도 OK**
- 4대 중 2대 죽으면 → 50%, 과반수 X → etcd 가 자체 정지
- "짝수는 비용만 + 1, 안전성은 그대로 = 함정"
- 실무: 3대 표준, 5대(2대 허용), 7대(3대 허용)

### 9:00 – 10:00  요약
[CAM]
> "마스터는 항상 홀수. 3대가 표준. 다음은 드디어 손으로 클러스터 띄워보는 시간입니다."

---

## 레슨 7 — 실습: minikube 1줄 설치 (12분)

### 0:00 – 1:30  실습 환경 안내
- macOS / Linux / Windows 모두 가능
- 도커 데스크톱 설치 가정
- 메모리 4GB+ 권장

### 1:30 – 4:30  설치
[TERM]
```bash
brew install minikube kubectl       # macOS
# 또는 Linux: curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
#             sudo install minikube-linux-amd64 /usr/local/bin/minikube
minikube start --driver=docker
```

### 4:30 – 7:30  첫 명령
[TERM]
```bash
kubectl get nodes
kubectl get pods -A
kubectl cluster-info
```
- 각 출력 의미 설명
- "kube-system 네임스페이스 = 본사 + 지사 전원이 여기 사는 동네"

### 7:30 – 11:00  부수 명령 익히기
```bash
kubectl run hello --image=nginx --restart=Never
kubectl get pods
kubectl describe pod hello
kubectl logs hello
kubectl delete pod hello
```

### 11:00 – 12:00  내일 예고
[CAM]
> "축하합니다, 1일차 끝! 클러스터 한 대가 여러분 노트북에 살아있습니다. 내일은 이 위에 진짜 앱을 올리는 4종 세트 — Pod / Deployment / Service / Ingress 를 만나봅니다."

---

## 촬영/편집 메모

- **자막**: 모든 SLIDE 와 TERM 부분에 한국어 자막 필수 (입문자 청취 부담 줄이기).
- **Pace**: 분당 200~220 자 한국어 발화. 천천히, 또박또박.
- **Visual rhythm**: 매 90초 안쪽으로 화면 전환 (CAM ↔ SLIDE ↔ TERM).
- **Outro**: 7개 레슨 모두 같은 끝 지퍼 (5초 — 다음 영상 카드 + "구독" 호출).
- **썸네일 톤**: 짙은 남색 배경 + 흰색 큰 한글 + 작은 영문 키워드. 7개 레슨 동일 컨셉으로 시리즈 통일감.
