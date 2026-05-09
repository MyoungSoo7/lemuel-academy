# 쿠버네티스 2일차 — 영상 스크립트

> 목표: Pod / Deployment / Service / Ingress 4종 세트를 직접 만들어보면서 익히기.
> 분량 합계: 약 75분. 7개 레슨, 각 7~12분.
> 표기: `[CAM]` 카메라 / `[SLIDE]` 슬라이드 / `[TERM]` 터미널 / `[B-ROLL]` 보조영상.

---

## 레슨 1 — Pod 가 뭔가요? (8분, preview)

### 0:00 – 0:30 훅
[CAM] "도커 컨테이너 잘 띄우는데, 쿠버네티스는 왜 컨테이너를 직접 안 다루고 'Pod' 라는 한 겹을 거쳐갈까요? 거기에 쿠버네티스의 핵심 철학이 들어있습니다."

### 0:30 – 3:00 공동 운명, 공동 네트워크
[SLIDE: Pod 안에 컨테이너 1+사이드카 2 그림]
> "Pod 안 컨테이너들은 같은 노드에 같이 뜨고 같이 죽어요. localhost 로 서로 부릅니다. IP 도 공유합니다."

[B-ROLL: nginx + log-shipper 사이드카 yaml]

### 3:00 – 5:30 대부분 1 Pod = 1 컨테이너
> "사실 99% 의 경우 Pod 1개 = 컨테이너 1개입니다. 사이드카 패턴은 로그 수집 / istio sidecar / TLS 종료 같은 특수한 경우입니다."

### 5:30 – 8:00 그래도 직접 만들 일은 거의 없다
[CAM] "오늘부터 우리는 Pod 를 yaml 로 만들 수 있게 됩니다. 그런데 운영에선 거의 안 만들어요. 왜냐? 다음 레슨에서 Deployment 가 등장하기 때문입니다."

---

## 레슨 2 — 첫 Pod yaml 한 통 (9분)

### 0:00 – 1:30 명령어 vs yaml
> "kubectl run 으로 한 줄에 띄울 수 있지만, 실무에선 yaml 로 관리합니다. 이유는 git 추적, 재현성, code review."

### 1:30 – 5:00 [TERM] pod-nginx.yaml 작성
```yaml
apiVersion: v1
kind: Pod
metadata: { name: hello-nginx, labels: { app: hello } }
spec:
  containers:
    - name: web
      image: nginx:1.27
      ports: [ { containerPort: 80 } ]
```
- 각 필드 설명 (apiVersion, kind, metadata, spec)
- labels 가 왜 중요한지 (Service 와 연결고리)

### 5:00 – 8:00 [TERM] 적용 + 검증
```bash
kubectl apply -f pod-nginx.yaml
kubectl get pods -o wide
kubectl describe pod hello-nginx
kubectl logs hello-nginx
kubectl port-forward pod/hello-nginx 8080:80
# 브라우저 http://localhost:8080
```

### 8:00 – 9:00 마무리
> "직접 띄워봤어요. 그런데 죽이면? 자동 복원이 없습니다. 다음 레슨에서 해결합니다."

[TERM] `kubectl delete pod hello-nginx` → 끝. 안 살아남.

---

## 레슨 3 — Deployment 가 진짜 운영을 가능하게 (12분)

### 0:00 – 1:00 선언적 vs 명령적
[CAM] "쿠버네티스 핵심 단어 하나만 기억하라면: '선언적'. '뭘 해라' 가 아니라 '이렇게 있어줘' 라고 말합니다. Deployment 가 가장 잘 보여줍니다."

### 1:00 – 4:30 [TERM] deploy-nginx.yaml 작성
```yaml
apiVersion: apps/v1
kind: Deployment
metadata: { name: hello-nginx }
spec:
  replicas: 3
  selector:
    matchLabels: { app: hello }
  template:
    metadata: { labels: { app: hello } }
    spec:
      containers:
        - name: web
          image: nginx:1.27
          ports: [ { containerPort: 80 } ]
```
- replicas / selector / template 3대 핵심
- "selector 의 라벨이 template 의 라벨과 일치해야" 함정 강조

### 4:30 – 7:30 [TERM] 자동 복구 시연
```bash
kubectl apply -f deploy-nginx.yaml
kubectl get deploy,pods
kubectl delete pod -l app=hello   # 1개 죽이기
kubectl get pods -w               # 즉시 새로 생김 (watch)
```

### 7:30 – 10:00 [TERM] scale + set image
```bash
kubectl scale deploy/hello-nginx --replicas=5
kubectl set image deploy/hello-nginx web=nginx:1.28
kubectl rollout status deploy/hello-nginx
```

### 10:00 – 11:30 ReplicaSet 한 겹 더 있다
[SLIDE: Deployment → ReplicaSet → Pod 3겹 구조]
> "사실 Deployment 안에 ReplicaSet 이 있고, 무중단 배포 때 새 RS 와 옛 RS 사이에서 점진 전환합니다. 입문자는 그림만 알아두면 충분."

### 11:30 – 12:00 마무리
> "Pod 가 자동으로 N개 굴러가요. 그런데 이걸 외부에 어떻게 노출하죠? 다음 레슨, Service."

---

## 레슨 4 — Service 4종 (10분)

### 0:00 – 1:30 Pod IP 의 한계
> "Pod 가 죽었다 살아나면 IP 가 바뀝니다. 5개 Pod 에 트래픽 어떻게 분산하죠? Service 가 두 문제를 한 번에."

### 1:30 – 4:30 [TERM] ClusterIP
```yaml
apiVersion: v1
kind: Service
metadata: { name: hello }
spec:
  selector: { app: hello }
  ports: [ { port: 80, targetPort: 80 } ]
  type: ClusterIP
```
- selector 라벨이 어떻게 Pod 와 연결되는지 그림으로 설명
- DNS 자동 등록: `http://hello` 로 부를 수 있음 (cluster 내부)

### 4:30 – 7:00 4종 비교표
[SLIDE: ClusterIP / NodePort / LoadBalancer / ExternalName 4박스]
- ClusterIP: 내부만, 마이크로서비스 간
- NodePort: 노드 포트 (30000~32767) — 데모용
- LoadBalancer: 클라우드 LB 자동 생성 — 비싸므로 Ingress 와 함께
- ExternalName: 외부 DB 를 cluster 이름처럼

### 7:00 – 9:00 [TERM] 실습
```bash
kubectl expose deployment hello --type=NodePort --port=80
kubectl get svc
minikube service hello              # 브라우저 자동 열림
```

### 9:00 – 10:00 마무리
> "내부 호출과 외부 노출까지 끝. 그런데 '도메인별/경로별' 라우팅은? 다음 레슨, Ingress."

---

## 레슨 5 — Ingress (10분)

### 0:00 – 1:30 LB 10개의 함정
> "LoadBalancer Service 는 1개당 클라우드 LB 1개입니다. 마이크로서비스 10개면 LB 10개. 비싸요."

### 1:30 – 4:30 Ingress 의 역할
[SLIDE: 1 Ingress → N Service 라우팅 트리]
- HTTP 7계층, 도메인 + 경로별 라우팅
- TLS 인증서 종료 (Let's Encrypt + cert-manager)

### 4:30 – 7:30 [TERM] 실 yaml — lemuel-academy 예시
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: lemuel-routes
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  ingressClassName: nginx
  rules:
    - host: academy.lemuel.co.kr
      http:
        paths:
          - { path: /,        backend: { service: { name: learner,        port: { number: 3001 } } } }
          - { path: /studio,  backend: { service: { name: creator-studio, port: { number: 3002 } } } }
          - { path: /api,     backend: { service: { name: api-gateway,    port: { number: 8080 } } } }
```

### 7:30 – 9:30 Ingress Controller 설치 (minikube)
```bash
minikube addons enable ingress
kubectl get pods -n ingress-nginx
```
> "yaml 은 선언일 뿐, 실제 트래픽은 Controller (nginx, traefik, istio gateway) 가 처리합니다."

### 9:30 – 10:00 마무리
> "도메인 1개 + 경로 N개 + Service N개 = LB 1개. 다음 레슨에서 4종 세트 전체가 어떻게 어울리는지 봅니다."

---

## 레슨 6 — 4종 세트 트래픽 흐름 한 번에 (8분)

### 0:00 – 1:00 큰 그림 시각화
[SLIDE: 외부 → LB → Ingress → Service → Pod (Deployment 관리)]

### 1:00 – 6:00 단계별 흐름
- 외부 요청 도착 → LoadBalancer (클라우드 LB)
- → Ingress Controller (nginx) → 도메인/경로 매칭
- → Service (ClusterIP) → 라벨로 백엔드 Pod 찾음
- → kube-proxy 가 iptables 룰로 트래픽 분산
- → Pod 컨테이너에 도착
- → Deployment 가 백그라운드에서 Pod N개 유지

### 6:00 – 8:00 한 줄 요약
> "Pod 직접 호출 X. 항상 Service 경유. 외부 입구는 Ingress."

---

## 레슨 7 — 실습: 5분 안에 nginx 노출 (12분)

### 0:00 – 2:00 환경 점검
```bash
minikube status
minikube addons enable ingress
```

### 2:00 – 5:00 [TERM] 명령형으로 후딱
```bash
kubectl create deployment hello --image=nginx:1.27 --replicas=3
kubectl expose deployment hello --type=NodePort --port=80
minikube service hello
```

### 5:00 – 9:00 [TERM] 선언형 yaml 로 다시
- pod / deploy / svc / ingress 4 통의 yaml 을 한 폴더에
- `kubectl apply -f .` 한 번
- 브라우저로 검증

### 9:00 – 11:00 죽여보기 + 새 버전 배포
```bash
kubectl delete pod -l app=hello
kubectl set image deploy/hello nginx=nginx:1.28
kubectl rollout status deploy/hello
```

### 11:00 – 12:00 다음 영상 예고
> "축하합니다, 2일차 끝! 이제 앱이 살아있고 외부에 노출됩니다. 그런데 DB 비밀번호는? 데이터는? 3일차에서 ConfigMap / Secret / PVC 만나봅니다."
