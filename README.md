# lemuel-academy

동영상 강의 플랫폼. 크리에이터가 영상 강의를 올리고, 학생이 무료로 시청하는 MSA 기반 시스템. Class101 / 탈잉 스타일.

## 모듈

```
classflow/
├── services/                      Spring Boot 4 + Kotlin
│   ├── user-service/              인증/프로필/진도/즐겨찾기
│   ├── catalog-service/           강의/챕터/레슨/리뷰
│   ├── media-service/             업로드/트랜스코딩/HLS 서빙
│   └── api-gateway/               Spring Cloud Gateway (JWT/CORS/rate-limit)
├── frontend/                      Next.js 15 (App Router)
│   ├── learner/                   학생용 (강의 둘러보기 / 시청)
│   ├── creator-studio/            크리에이터용 (영상 업로드 / 통계)
│   └── admin/                     관리자용 (검수 / 차단 / 분석)
├── infra/
│   └── docker-compose.yml         로컬 dev 환경
└── docs/
    └── architecture.md            아키텍처 + 데이터 플로우
```

## 액터 + 화면

| 액터 | 보는 화면 | 권한 |
|------|----------|------|
| 학생 (User) | learner — 강의 둘러보기 / 시청 / Q&A | 시청, 댓글, 진도 저장 |
| 크리에이터 (Creator) | creator-studio — 영상 업로드 / 강의 관리 / 통계 | 본인 강의만 CRUD |
| 관리자 (Admin) | admin — 검수 / 차단 / 분석 | 전권 |

## 기술 스택

- 백엔드: Spring Boot 4 + Kotlin + JPA + PostgreSQL 17
- 프론트: Next.js 15 (App Router) + TypeScript
- 영상 플레이어: hls.js + video.js
- 트랜스코딩: ffmpeg (1080p/720p/480p HLS multi-bitrate)
- 메시지큐: Redis Streams (큐 / pub-sub)
- 스토리지: Cloudflare R2 (S3 호환, 송신 무료)
- 인증: JWT + NextAuth (Google/Kakao/Naver OAuth)
- 검색: PostgreSQL full-text (MVP) → Elasticsearch (Phase 2)
- 배포: Docker Compose (dev) / k3s (운영, 르무엘)
- 모니터링: Grafana + Prometheus

## 빠른 시작 (dev)

```bash
# 1. 의존 서비스 띄우기 (PostgreSQL, Redis)
cd infra && docker compose up -d postgres redis

# 2. 백엔드 서비스 (각각 별 터미널)
cd services/user-service && ./gradlew bootRun
cd services/catalog-service && ./gradlew bootRun
cd services/media-service && ./gradlew bootRun
cd services/api-gateway && ./gradlew bootRun

# 3. 프론트엔드
cd frontend/learner && pnpm dev
cd frontend/creator-studio && pnpm dev
cd frontend/admin && pnpm dev
```

## 환경변수

```
# 공통
DATABASE_URL=postgres://academy:academy@127.0.0.1:5432/academy
REDIS_URL=redis://127.0.0.1:6379

# media-service (R2)
R2_ENDPOINT=https://<account>.r2.cloudflarestorage.com
R2_BUCKET=lemuel-academy
R2_ACCESS_KEY=...
R2_SECRET_KEY=...

# user-service (JWT)
JWT_SECRET=...
JWT_TTL_HOURS=24
OAUTH_GOOGLE_CLIENT_ID=...
OAUTH_KAKAO_CLIENT_ID=...

# 프론트엔드
NEXT_PUBLIC_API_BASE=https://academy.lemuel.co.kr/api
```

## Phase 1 MVP 범위

- [x] 모노레포 부트스트랩
- [ ] user-service: 회원가입/로그인 + JWT
- [ ] catalog-service: 강의 CRUD + 목록
- [ ] media-service: R2 업로드 → ffmpeg HLS → 게시
- [ ] learner-frontend: 강의 목록 + 시청 (hls.js)
- [ ] creator-studio: 영상 업로드 + 강의 등록
- [ ] admin: 강의 검수 + 통계
- [ ] api-gateway + docker-compose 통합

## 배포 대상

- 르무엘 (192.168.219.101): media-service (트랜스코딩 무거움), 모든 백엔드, 모든 프론트
- DNS: academy.lemuel.co.kr (Cloudflare Tunnel)

## 자동 로그인 (Phase 1 MVP)

회원가입 없이 첫 진입시 역할별 데모 유저로 자동 로그인:
- learner :3001 → STUDENT (`demo-student@academy.local`)
- creator-studio :3002 → CREATOR (`demo-creator@academy.local`)
- admin :3003 → ADMIN (`demo-admin@academy.local`)

## 문서

- [`docs/architecture.md`](docs/architecture.md) — 시스템 컨텍스트, 데이터 플로우, DB 스키마, API 컨트랙트
- [`CLAUDE.md`](CLAUDE.md) — Claude / AI 도구용 가이드 (컨벤션 + 자주 빠지는 함정)
- [`AGENTS.md`](AGENTS.md) — 작업 가이드 (자주 하는 일 + 디버깅 체크리스트)
- [`services/media-service/AGENTS.md`](services/media-service/AGENTS.md) — media-service 전용 가이드

## 관련 도구

- [superpowers](https://github.com/MyoungSoo7/superpowers) — agentic skills framework. Claude Code 플러그인.
- [ouroboros](https://github.com/MyoungSoo7/ouroboros) — Agent OS, spec-driven workflow.
