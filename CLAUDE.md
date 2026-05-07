# CLAUDE.md — lemuel-academy

이 레포에서 Claude / 다른 AI 도구가 알아야 할 컨벤션과 컨텍스트.

## 한 줄

동영상 강의 플랫폼. 크리에이터가 영상 강의를 올리고, 학생이 무료로 시청. MSA 7개 (Spring Boot Kotlin × 4 + Next.js × 3) + ffmpeg 워커.

## 디렉토리

```
services/
  user-service/        인증/프로필/진도/즐겨찾기 (port 8081)
  catalog-service/     강의/챕터/레슨/리뷰 (port 8082)
  media-service/       업로드/HLS/R2 (port 8083) + ffmpeg-worker (Python)
  api-gateway/         Spring Cloud Gateway (port 8080) — JwtFilter

frontend/
  learner/             학생 :3001 (Next.js 15 App Router)
  creator-studio/      크리에이터 :3002
  admin/               관리자 :3003

infra/
  docker-compose.full.yml   통합 dev/배포
  init.sql                  스키마 부트스트랩 (3 schemas)

docs/architecture.md         시스템 컨텍스트 + 데이터 플로우 + DB
```

## 액터 + 권한

| 액터 | 보는 화면 | 권한 | 자동 로그인 |
|------|----------|------|-------------|
| 학생 (STUDENT) | learner :3001 | 시청, 진도, 댓글 | demo-student@academy.local |
| 크리에이터 (CREATOR) | creator-studio :3002 | 본인 강의 CRUD | demo-creator@academy.local |
| 관리자 (ADMIN) | admin :3003 | 전권 | demo-admin@academy.local |

> Phase 1 MVP 단계라 첫 진입시 자동 로그인. `<AutoLogin role="..." />` 컴포넌트가 layout.tsx 에 마운트.

## 기술 스택

- **백엔드**: Kotlin 2.0 + Spring Boot 3.4 + JPA + Flyway + PostgreSQL 17
- **프론트**: Next.js 15 + React 19 + Tailwind + hls.js + SWR
- **트랜스코딩**: ffmpeg → HLS multi-bitrate (1080p/720p/480p)
- **메시지큐**: Redis Streams (`media:transcode`)
- **스토리지**: Cloudflare R2 (S3 호환) — presigned PUT 업로드, public HLS 서빙
- **인증**: JWT (HS256) — api-gateway JwtFilter 가 Bearer 검증 → X-User-Id, X-User-Role 헤더 주입

## 환경변수

```
# 공통
DATABASE_URL=jdbc:postgresql://localhost:5439/academy
REDIS_URL=redis://localhost:6389
JWT_SECRET=<256bit+ random>

# media-service
R2_ENDPOINT=https://<account>.r2.cloudflarestorage.com
R2_BUCKET=lemuel-academy
R2_ACCESS_KEY=...
R2_SECRET_KEY=...

# 프론트엔드
NEXT_PUBLIC_API_BASE=https://academy.lemuel.co.kr
```

## 주요 흐름

### 회원/인증
```
POST /api/users/dev/auto-login?role=STUDENT|CREATOR|ADMIN  → 데모 유저 + JWT (MVP)
POST /api/users/signup                                       → 회원가입 + JWT
POST /api/users/login                                        → 로그인 + JWT
POST /api/users/oauth/{google|kakao|naver}                   → OAuth 로그인 (스텁)
GET  /api/users/me
PATCH /api/users/me
POST /api/users/progress                                     → 진도 보고
GET  /api/users/favorites
POST /api/users/favorites/{courseId}
```

### 강의 CRUD
```
GET  /api/catalog/courses?categoryId=&q=&page=&size=         → 목록 (PUBLISHED 만)
GET  /api/catalog/courses/{id}                               → 상세 + 챕터/레슨
POST /api/catalog/courses                                    → 생성 (CREATOR)
POST /api/catalog/courses/{id}/submit-review                 → DRAFT → REVIEW (CREATOR)
PATCH /api/catalog/courses/{id}/review?approved=true|false   → ADMIN

POST /api/catalog/courses/{courseId}/chapters
PUT  /api/catalog/chapters/{id}
DEL  /api/catalog/chapters/{id}
POST /api/catalog/chapters/{chapterId}/lessons
PUT  /api/catalog/lessons/{id}
DEL  /api/catalog/lessons/{id}
GET  /api/catalog/courses/{id}/reviews
POST /api/catalog/courses/{id}/reviews                       → 평점 1~5 + 평균 자동 갱신
```

### 영상
```
POST /api/media/videos/upload-url     → presigned PUT URL (15분)
PUT  <presigned URL>                  → 클라이언트가 R2 에 직접 업로드
POST /api/media/videos/{id}/finalize  → Redis Streams enqueue → ffmpeg 워커가 비동기 처리
GET  /api/media/videos/{id}           → status (UPLOADING/UPLOADED/TRANSCODING/READY/FAILED) + manifest URL
GET  /api/media/videos/{id}/manifest  → R2 public m3u8 URL
GET  /api/media/videos/me             → 본인 업로드 목록
DEL  /api/media/videos/{id}
```

## 코드 컨벤션

- Kotlin: `co.lemuel.academy.<service>` 패키지, 데이터 클래스 + JPA Entity 분리
- 엔티티는 mutable var (JPA 호환), DTO 는 immutable val
- Flyway: `services/<svc>/src/main/resources/db/migration/V<N>__init.sql` — schema 별 분리
- API 경로: `/api/{service}/...` (api-gateway 가 prefix 분기)
- 인증: 컨트롤러에서 `@RequestHeader("X-User-Id") userId: UUID` 받음 (gateway 가 주입)
- Spring 정책: 서비스간 직접 통신 금지 (필요시 webhook 또는 메시지큐)

## 자주 빠지는 함정

- **Flyway schema 충돌**: `spring.flyway.schemas` + `default-schema` 둘 다 설정 필요
- **R2 presigned URL CORS**: R2 콘솔에서 CORS 정책 추가 필요 (PUT 허용)
- **ffmpeg-worker**: `Dockerfile.worker` 별도, ffmpeg + Python 같이 들어감 (이미지 크기 ~1GB)
- **Next.js rewrites**: `/api/*` 를 NEXT_PUBLIC_API_BASE 로 라우팅 — dev/prod env 다름
- **JWT secret**: 32 bytes 미만이면 jjwt 가 거부 → 256bit 이상

## 운영 / 배포

- 르무엘 (192.168.219.101): 모든 서비스 + 프론트 + ffmpeg-worker (32GB RAM 필요)
- DNS: `academy/studio/admin-academy.lemuel.co.kr` → Cloudflare Tunnel → :3001/:3002/:3003
- API: `academy.lemuel.co.kr/api` → :8080 (api-gateway)
- R2: `lemuel-academy` 버킷 (private originals/, public hls/)

```bash
# 르무엘에서
cd /opt/lqc/lemuel-academy/infra
cp .env.example .env  # JWT_SECRET, R2 키 채움
docker compose -f docker-compose.full.yml up -d --build
```

## superpowers 플러그인 (Claude Code)

본 레포의 백엔드/프론트 작업에 도움이 되는 메타 스킬 모음. 설치:

```bash
# 사용자 .claude 디렉토리에서
git clone https://github.com/MyoungSoo7/superpowers ~/.claude/plugins/superpowers
# 또는 Claude Code plugin install 명령 (해당 환경)
```

활용:
- 새 서비스 부트스트랩 시 `superpowers/scaffold` 스킬
- API 변경시 자동 OpenAPI 갱신 워크플로

## 다음 단계 (Phase 2~3)

- [ ] OAuth 실 연동 (NextAuth.js + spring-security-oauth2-client)
- [ ] 라이브 강의 (WebRTC + Janus 또는 mediasoup)
- [ ] 자막 자동 생성 (Whisper)
- [ ] 결제 (포트원, 옵션)
- [ ] Q&A / 노트 / 북마크
- [ ] 추천 알고리즘 (협업 필터링)

## 도움 받을 위치

- 아키텍처 변경 → `docs/architecture.md` 갱신
- 새 엔드포인트 → 본 CLAUDE.md API 흐름 섹션에 추가
- 운영 이슈 → `infra/docker-compose.full.yml` 또는 systemd unit (없으면 추가)
