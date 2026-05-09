# 르무엘 SSH 22 복구 가이드

> 증상: 다른 노드(Mac, David)에서 `ssh iamipro@192.168.219.101` 가 timeout. ping 은 정상.
> 원인 후보: ufw 규칙 / sshd down / fail2ban 차단 / 포트 충돌

## 1. 콘솔 (모니터+키보드 직접 접속) 에서 진단

```bash
# 1) sshd 상태
sudo systemctl status ssh
# Active: active (running) 이어야 함. 아니면:
sudo systemctl restart ssh
sudo systemctl enable ssh
sudo journalctl -u ssh -n 50 --no-pager       # 에러 로그

# 2) 포트 listening 확인
sudo ss -tlnp | grep :22
# tcp LISTEN 0 128 *:22 ... users:(("sshd",...))  이어야 함
# 안 보이면 sshd 가 죽었거나 다른 포트에 바인딩됨

# 3) ufw 룰 확인 + 22 허용
sudo ufw status verbose
# 22/tcp 가 ALLOW 가 아니면:
sudo ufw allow 22/tcp
sudo ufw reload

# 4) fail2ban 차단 확인 (있다면)
sudo fail2ban-client status sshd 2>/dev/null
sudo fail2ban-client unban 192.168.219.103 2>/dev/null   # 내 Mac IP
sudo fail2ban-client unban 192.168.219.107 2>/dev/null   # David

# 5) iptables 직접 점검 (ufw 에 안 잡히는 룰)
sudo iptables -L INPUT -n -v | head -40
# REJECT/DROP 룰이 있으면 위치 확인
```

## 2. 외부에서 재시도

복구 후 Mac 에서:
```bash
ssh -v iamipro@192.168.219.101 hostname
```

`-v` 출력이 `Connecting to 192.168.219.101 port 22...` 후 멈추면 여전히 막힘.

## 3. 추가 의심 — 네트워크 카드

```bash
# 콘솔에서:
ip a
# 192.168.219.101 가 일치하는 인터페이스 (보통 eth0/enp3s0) 의 RX/TX 패킷이 늘고 있는지
# 패킷이 0 이면 NIC 또는 케이블 문제

# 라우팅
ip route
# default via 192.168.219.1 (공유기) 이어야 함

# 공유기 ARP 캐시 의심 시 NIC 재가동
sudo ip link set eth0 down && sleep 2 && sudo ip link set eth0 up
sudo systemctl restart systemd-networkd  # 또는 NetworkManager
```

## 4. 복구 후 후속 작업 (자동화)

복구되면 Mac 에서 다음 두 명령으로 academy DB Flyway 마이그레이션 + crypto 터널 라우트 자동 적용:

```bash
# 4a) academy V2/V3/V4 적용 — academy 컨테이너 재시작 (Flyway 자동 실행)
ssh iamipro@192.168.219.101 'cd /opt/lqc/lemuel-academy && \
  git pull && \
  docker compose -f infra/docker-compose.full.yml restart catalog-service'

# 4b) crypto.lemuel.co.kr → David:8090 터널 라우트
ssh iamipro@192.168.219.101 'cat /etc/cloudflared/config.yml | grep -A2 crypto || \
  echo "라우트 추가 필요 — config.yml hostnames 섹션에:" && \
  echo "  - hostname: crypto.lemuel.co.kr" && \
  echo "    service: http://192.168.219.107:8090"'
```

## 5. 임시 우회 — David Cloudflare Tunnel 직접 운영

복구가 어려우면 David 에 별도 cloudflared 인스턴스를 깔아 crypto.lemuel.co.kr 만 우회:

```bash
ssh david@192.168.219.107
# cloudflared 설치
wget -q https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb
sudo dpkg -i cloudflared-linux-amd64.deb

# 사용자 토큰으로 로그인 (브라우저 열림)
cloudflared tunnel login

# 터널 생성
cloudflared tunnel create lemuel-david
cloudflared tunnel route dns lemuel-david crypto.lemuel.co.kr

# config.yml
cat > ~/.cloudflared/config.yml <<EOF
tunnel: lemuel-david
credentials-file: /home/david/.cloudflared/<UUID>.json
ingress:
  - hostname: crypto.lemuel.co.kr
    service: http://localhost:8090
  - service: http_status:404
EOF

sudo cloudflared service install
sudo systemctl start cloudflared
```

## 6. 원인 기록 (복구 후 작성)

| 일시 | 증상 | 원인 | 조치 | 재발 방지 |
|---|---|---|---|---|
| 2026-05-09 ~07:30 | SSH 22 timeout | (조사 중) | (복구 후 기재) | (예: ufw allow rule 영구화) |
