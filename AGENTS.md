# AGENTS.md — lemuel-academy 작업 가이드

AI 에이전트 / 개발자가 이 레포에서 작업할 때 따라야 할 규칙 + 자주 하는 일 + 디버깅 체크리스트.

## 작업 시작 전

1. `docs/architecture.md` 먼저 읽기 — 시스템 컨텍스트 / 데이터 플로우 / DB 스키마
2. `CLAUDE.md` 의 코드 컨벤션 + 자주 빠지는 함정 5가지 확인
3. 변경할 서비스의 `services/<svc>/AGENTS.md` (있으면) 읽기

## 자주 하는 작업 5가지

### 1. 새 엔드포인트 추가 (catalog-service 기준)

```
1. services/catalog-service/src/main/kotlin/co/lemuel/academy/catalog/api/<X>Controller.kt
2. CLAUDE.md "주요 흐름" 섹션에 API 추가
3. 필요시 도메인 엔티티 / Repository 추가
4. Flyway V<N+1>__add_<X>.sql 마이그레이션 (기존 V<N>__init.sql 수정 금지)
5. 테스트: ./gradlew test (있으면)
```

### 2. 새 React 페이지 추가 (learner 기준)

```
1. frontend/learner/src/app/<route>/page.tsx
2. 데이터 fetch: SWR (클라) 또는 fetch + revalidate (서버컴포)
3. 인증 필요시: localStorage('lqa_token') → Authorization 헤더
4. AutoLogin 컴포넌트는 root layout 에만 1번 마운트 (이미 함)
```

### 3. R2 버킷에 새 prefix 추가 (예: 자막)

```
1. media-service R2Service 에 keyFor<X>() 헬퍼 추가
2. presigned URL 발급 흐름은 Video 와 동일 패턴
3. CORS 정책: R2 콘솔에서 PUT origin 화이트리스트 (academy.lemuel.co.kr)
4. ffmpeg-worker scripts/transcode.sh 에 처리 단계 추가 (필요시)
```

### 4. ffmpeg 트랜스코딩 옵션 변경

```
파일: services/media-service/scripts/transcode.sh
- 비트레이트 / 해상도 / preset 변경
- 변경 후 docker compose -f docker-compose.full.yml build ffmpeg-worker
- 기존 영상은 재처리 안 됨 (manual re-enqueue 필요)
```

### 5. JWT 만료 / refresh 추가

```
1. user-service JwtService 에 refreshToken 발급 추가
2. POST /api/users/refresh 엔드포인트
3. api-gateway JwtFilter 의 만료 처리 정책 합의
4. 프론트 axios/fetch interceptor 로 401 → /refresh → 재시도
```

## 디버깅 체크리스트

### 로컬 docker compose 가 안 뜸
- [ ] `JWT_SECRET` env 설정했나? (256bit 이상)
- [ ] `R2_*` 4개 다 채웠나? (media-service 기동 실패 원인 #1)
- [ ] `docker compose logs <service>` 확인
- [ ] postgres healthcheck 통과 후 다른 서비스 뜸 (depends_on)

### 영상 업로드는 되는데 시청이 안 됨
- [ ] /api/media/videos/{id} status 가 READY 인가?
- [ ] hls_master_url 이 채워졌나?
- [ ] R2 public URL 이 브라우저에서 200 응답 (CORS / public-read 정책)?
- [ ] hls.js 가 master.m3u8 파싱 성공 (브라우저 콘솔)?

### 로그인은 되는데 API 호출시 401
- [ ] localStorage('lqa_token') 에 값 있나?
- [ ] Authorization: Bearer <token> 헤더 보냈나?
- [ ] api-gateway 와 user-service 가 같은 JWT_SECRET 인가?
- [ ] 토큰 만료 (24h)

### Flyway 마이그레이션 실패
- [ ] V<N>__init.sql 을 수정하지 말고 V<N+1>__... 로 새 파일
- [ ] 이미 적용된 환경에서는 `flyway repair` 또는 schema_history 직접 정리
- [ ] schemas + default-schema 둘 다 설정

## 원칙

- **테스트 후 PR 마인드셋**: 변경 후 영향 받는 서비스/페이지에서 한 번 띄워서 확인
- **DB 변경은 마이그레이션으로**: 직접 DDL 금지, 항상 Flyway V<N>__<desc>.sql
- **MVP 단계라 깊은 추상화 금지**: 처음부터 `interface BaseService` 같은 거 만들지 말 것
- **운영 키 절대 커밋 금지**: `.gitignore` 에 .env, secrets/ 등록되어 있음, gh 키 검출 한번 더 체크
