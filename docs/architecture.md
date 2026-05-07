# lemuel-academy 아키텍처

## 한 줄

"크리에이터가 영상 올리고, 학생이 무료로 보는 동영상 강의 플랫폼. MSA 7개, ffmpeg HLS, R2 스토리지, Spring Boot + Next.js."

## 시스템 컨텍스트

```
┌────────────────────────────────────────────────────────┐
│                  Cloudflare Tunnel                      │
│  academy.lemuel.co.kr / studio.lemuel.co.kr / admin.   │
└────────────┬───────────────────────────────────────────┘
             ↓
      ┌──────────────┐
      │ api-gateway  │  Spring Cloud Gateway — JWT/CORS/rate-limit
      │   (8080)     │
      └──────┬───────┘
             ↓
   ┌─────────┼─────────┬─────────┐
   ↓         ↓         ↓         ↓
┌──────┐ ┌──────┐ ┌──────┐ ┌─────────┐
│user- │ │catalog│ │media-│ │admin    │
│svc   │ │ svc   │ │ svc  │ │svc(BFF) │
└──┬───┘ └──┬───┘ └──┬───┘ └────┬────┘
   ↓        ↓        ↓          ↓
┌────────────────────────────────────┐
│  PostgreSQL  │  Redis  │  R2(HLS)  │
└────────────────────────────────────┘
```

## 서비스별 책임

### user-service
- 회원가입/로그인 (이메일/패스워드 + OAuth: Google/Kakao/Naver)
- JWT 발급/검증
- 역할 관리 (STUDENT, CREATOR, ADMIN)
- 진도 저장 (lesson 별 watched_seconds)
- 즐겨찾기 / 알림 설정
- DB: users, roles, progress, favorites, notifications

### catalog-service
- 강의 CRUD (creator 가 만들고 admin 이 검수)
- 챕터/레슨 구조 관리
- 카테고리/태그/검색 (PG full-text)
- 리뷰/평점
- DB: courses, chapters, lessons, categories, tags, reviews

### media-service
- 영상 업로드 (multipart 또는 chunked 또는 presigned URL)
- 원본을 R2 private bucket 에 저장
- Redis Streams 에 transcode 작업 enqueue
- ffmpeg 워커가 HLS 변환 (1080p/720p/480p × 6초 segment)
- 변환 완료시 R2 public bucket 으로 이동, catalog-service 에 webhook
- 썸네일 자동 생성 (영상 중간 frame)
- DB: videos, transcode_jobs

### admin-bff
- 검수 큐 (강의 status=REVIEW 인 것들)
- 차단/해제
- 신고 처리
- 통계 대시보드 (활성 사용자, 인기 강의)

### api-gateway
- 단일 진입점 :8080
- JWT 검증 후 user_id, role 헤더 주입
- /api/users/** → user-service
- /api/catalog/** → catalog-service
- /api/media/** → media-service
- /api/admin/** → admin-bff (role=ADMIN 만)

## 데이터 플로우 — 영상 업로드

```
1. 크리에이터가 creator-studio 에서 mp4 업로드 (chunk + presigned PUT)
2. media-service /videos POST
3. R2 private bucket 에 원본 저장
4. videos 테이블 status=UPLOADED 로 row 생성
5. Redis Stream "media:transcode" 에 작업 enqueue { video_id }
6. ffmpeg 워커가 dequeue → R2 다운 → HLS 변환 → R2 public 업로드
   - 1080p (~5Mbps), 720p (~2.5Mbps), 480p (~1Mbps)
   - master playlist (.m3u8) + variant playlists + .ts 세그먼트
   - thumbnail.jpg (5초 지점 frame)
7. status=READY 업데이트 + duration_sec, hls_master_url 채움
8. catalog-service 에 webhook (강의 lesson 의 video_status 갱신)
```

## 데이터 플로우 — 학생이 시청

```
1. learner /courses/{id} 페이지 진입
2. catalog-service 에서 course 메타 + lesson 목록 조회
3. lesson 클릭 → /api/media/videos/{id}/manifest
4. media-service 가 hls_master_url (R2 public) 리턴
5. hls.js 가 m3u8 파싱 → 세그먼트 ABR 다운
6. 시청 중 5초마다 user-service /progress 에 watched_seconds 보고
7. 시청 완료 시 progress.completed=true
```

## DB 스키마 (핵심)

```sql
-- user-service
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT,           -- nullable for OAuth-only
  oauth_provider TEXT,          -- google/kakao/naver
  oauth_id TEXT,
  display_name TEXT NOT NULL,
  avatar_url TEXT,
  role TEXT NOT NULL DEFAULT 'STUDENT',  -- STUDENT/CREATOR/ADMIN
  created_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(oauth_provider, oauth_id)
);

CREATE TABLE progress (
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  lesson_id UUID NOT NULL,
  watched_seconds INT NOT NULL DEFAULT 0,
  completed BOOLEAN NOT NULL DEFAULT FALSE,
  last_watched_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (user_id, lesson_id)
);

-- catalog-service
CREATE TABLE courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  creator_id UUID NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  thumbnail_url TEXT,
  category_id INT,
  status TEXT NOT NULL DEFAULT 'DRAFT',   -- DRAFT/REVIEW/PUBLISHED/REJECTED
  view_count BIGINT DEFAULT 0,
  rating_avg NUMERIC(3,2),
  rating_count INT DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT now(),
  published_at TIMESTAMPTZ
);
CREATE INDEX idx_courses_status_published ON courses(status, published_at DESC);
CREATE INDEX idx_courses_creator ON courses(creator_id);

CREATE TABLE chapters (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  course_id UUID REFERENCES courses(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  display_order INT NOT NULL
);

CREATE TABLE lessons (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  chapter_id UUID REFERENCES chapters(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  video_id UUID,
  duration_sec INT,
  display_order INT NOT NULL,
  is_preview BOOLEAN DEFAULT FALSE
);

-- media-service
CREATE TABLE videos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  creator_id UUID NOT NULL,
  original_url TEXT,
  hls_master_url TEXT,
  thumbnail_url TEXT,
  duration_sec INT,
  status TEXT NOT NULL DEFAULT 'UPLOADING',
  -- UPLOADING/UPLOADED/TRANSCODING/READY/FAILED
  created_at TIMESTAMPTZ DEFAULT now()
);
```

## API 컨트랙트 (핵심)

### user-service
```
POST /api/users/signup      { email, password, displayName }
POST /api/users/login       { email, password }
POST /api/users/oauth/{provider}  { code, redirect_uri }
GET  /api/users/me
POST /api/users/progress    { lesson_id, watched_seconds }
```

### catalog-service
```
GET  /api/catalog/courses                    (목록, ?category, ?q, ?sort)
GET  /api/catalog/courses/{id}               (상세 + 챕터/레슨)
POST /api/catalog/courses                    (creator 권한)
POST /api/catalog/courses/{id}/submit-review (creator → REVIEW)
PATCH /api/catalog/courses/{id}/review       (admin → PUBLISHED/REJECTED)
POST /api/catalog/courses/{id}/reviews       (학생 리뷰)
```

### media-service
```
POST /api/media/upload-url    { filename, content_type, size }  → presigned PUT
POST /api/media/videos        { upload_id }                     → enqueue transcode
GET  /api/media/videos/{id}                                     → status + manifest_url
GET  /api/media/videos/{id}/manifest                            → 302 → R2 public m3u8
```

## 배포

### 르무엘에서 (32GB RAM, 트랜스코딩 무거움)
- 모든 백엔드 + 프론트엔드 컨테이너
- ffmpeg 워커 별도 컨테이너 (CPU bound, 다중 인스턴스)
- nginx → Cloudflare Tunnel → academy.lemuel.co.kr

### 도메인 매핑
- academy.lemuel.co.kr → learner
- studio.lemuel.co.kr → creator-studio
- admin-academy.lemuel.co.kr → admin (Cloudflare Access OAuth 필수)

## 보안 가이드라인

- JWT 만료 24h, refresh token 7일
- 영상 R2 public 은 hotlink 방지 (referer 화이트리스트)
- 업로드 파일 타입 화이트리스트 (mp4, mov, mkv only)
- 관리자 페이지는 Cloudflare Access OAuth 강제
- ffmpeg 워커는 컨테이너 격리 + read-only rootfs
- 영상 URL 은 짧은 만료 (signed URL, 1시간) 도 검토

## 다음 단계 (Phase 2~3)

- 라이브 강의 (탈잉 스타일, WebRTC)
- 자막 자동 생성 (Whisper)
- 추천 알고리즘 (Class101 협업필터링)
- 결제 (포트원, 회수 가능 옵션 시)
- 구독/멤버십 모델
