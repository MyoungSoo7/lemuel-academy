-- 4개 신규 코스 추가 (1일차 챕터 + 7개 레슨씩만, 2~7일차 placeholder).
-- 1) Spring Boot 4 MSA 7일
-- 2) Docker → K8s 마이그레이션 7일
-- 3) 온체인 개발 입문 7일
-- 4) TypeScript + Next.js 15 풀스택 7일

DO $$
DECLARE
  v_devops_cat INT;
  v_creator UUID := '00000000-0000-0000-0000-000000000c7e';
  v_spring_id  UUID := '00000000-0000-0000-0000-00000000a8cb';
  v_d2k_id     UUID := '00000000-0000-0000-0000-00000000a8cc';
  v_chain_id   UUID := '00000000-0000-0000-0000-00000000a8cd';
  v_next_id    UUID := '00000000-0000-0000-0000-00000000a8ce';
  v_chapter_id UUID;
BEGIN
  SELECT id INTO v_devops_cat FROM catalog.categories WHERE slug = 'devops';

  -- 카테고리 — backend, frontend, blockchain
  INSERT INTO catalog.categories (slug, name, display_order) VALUES
    ('backend', '백엔드', 7),
    ('frontend', '프론트엔드', 8),
    ('blockchain', '블록체인', 9)
  ON CONFLICT (slug) DO NOTHING;

  -- 1) Spring Boot 4 MSA -----------------------------------------------------
  INSERT INTO catalog.courses (id, creator_id, title, description, category_id, status, published_at)
  SELECT v_spring_id, v_creator,
    'Spring Boot 4 MSA 7일 — 모놀리스를 마이크로서비스로',
    '르무엘 아카데미를 만든 실 사례로 배우는 Kotlin + Spring Boot 4 마이크로서비스. ' ||
    'Hexagonal architecture / API Gateway + JWT / Webhook + Redis Streams / Testcontainers / ' ||
    'Micrometer + Prometheus / Docker → K8s 까지.',
    id, 'PUBLISHED', now()
  FROM catalog.categories WHERE slug = 'backend'
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title;

  v_chapter_id := '00000000-0000-0000-0001-aaaaaaaaaaaa';
  INSERT INTO catalog.chapters (id, course_id, title, display_order)
  VALUES (v_chapter_id, v_spring_id, '1일차 — 모놀리스에서 마이크로서비스로', 1)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title;

  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview) VALUES
    ('00000000-0000-0000-0011-000000000001', v_chapter_id, '언제 쪼개야 하나 — 5가지 신호', 480, 1, TRUE),
    ('00000000-0000-0000-0011-000000000002', v_chapter_id, 'lemuel-academy 4개 서비스 분해 사례', 540, 2, FALSE),
    ('00000000-0000-0000-0011-000000000003', v_chapter_id, 'Spring Boot 4 + Kotlin 5분 부트스트랩', 480, 3, FALSE),
    ('00000000-0000-0000-0011-000000000004', v_chapter_id, '필수 + 선택 의존성 + 빌드 설정', 420, 4, FALSE),
    ('00000000-0000-0000-0011-000000000005', v_chapter_id, 'Hexagonal architecture 디렉토리 구조', 540, 5, FALSE),
    ('00000000-0000-0000-0011-000000000006', v_chapter_id, '첫 엔티티 + UseCase + Controller', 600, 6, FALSE),
    ('00000000-0000-0000-0011-000000000007', v_chapter_id, '실습: 회원가입 API 한 통', 720, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, duration_sec = EXCLUDED.duration_sec;

  -- 2~7일차 placeholder
  INSERT INTO catalog.chapters (id, course_id, title, display_order) VALUES
    ('00000000-0000-0000-0001-aaaaaaaaaaab', v_spring_id, '2일차 — API Gateway + JWT 인증', 2),
    ('00000000-0000-0000-0001-aaaaaaaaaaac', v_spring_id, '3일차 — JPA + Flyway + Hexagonal', 3),
    ('00000000-0000-0000-0001-aaaaaaaaaaad', v_spring_id, '4일차 — 서비스 간 통신 (REST / Webhook / Redis Streams)', 4),
    ('00000000-0000-0000-0001-aaaaaaaaaaae', v_spring_id, '5일차 — Testcontainers 통합 테스트', 5),
    ('00000000-0000-0000-0001-aaaaaaaaaaaf', v_spring_id, '6일차 — 관측성 (Micrometer + Prometheus)', 6),
    ('00000000-0000-0000-0001-aaaaaaaaaab0', v_spring_id, '7일차 — 배포 (Docker Compose → K8s)', 7)
  ON CONFLICT (id) DO NOTHING;

  -- 2) Docker → K8s 마이그레이션 ----------------------------------------------
  INSERT INTO catalog.courses (id, creator_id, title, description, category_id, status, published_at)
  VALUES (v_d2k_id, v_creator,
    'Docker → 쿠버네티스 마이그레이션 7일',
    '르무엘 인프라 마이그 실 사례. docker-compose → kompose → 손 다듬기 → Helm → ArgoCD GitOps 까지 7일.',
    v_devops_cat, 'PUBLISHED', now())
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title;

  v_chapter_id := '00000000-0000-0000-0002-aaaaaaaaaaaa';
  INSERT INTO catalog.chapters (id, course_id, title, display_order)
  VALUES (v_chapter_id, v_d2k_id, '1일차 — 왜, 언제, 어떻게 옮길까', 1)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title;

  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview) VALUES
    ('00000000-0000-0000-0021-000000000001', v_chapter_id, '도커 컴포즈로 충분한 경우 / 옮길 신호 5가지', 480, 1, TRUE),
    ('00000000-0000-0000-0021-000000000002', v_chapter_id, '마이그레이션 전 체크리스트', 420, 2, FALSE),
    ('00000000-0000-0000-0021-000000000003', v_chapter_id, 'kompose — 자동 변환 첫 발', 540, 3, FALSE),
    ('00000000-0000-0000-0021-000000000004', v_chapter_id, '자동 변환의 한계 4가지', 480, 4, FALSE),
    ('00000000-0000-0000-0021-000000000005', v_chapter_id, 'Volume / hostPath → PVC 다듬기', 540, 5, FALSE),
    ('00000000-0000-0000-0021-000000000006', v_chapter_id, 'env 평문 → ConfigMap + Secret 분리', 480, 6, FALSE),
    ('00000000-0000-0000-0021-000000000007', v_chapter_id, '실습: Postgres 컨테이너 K3s 로 옮기기', 720, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, duration_sec = EXCLUDED.duration_sec;

  INSERT INTO catalog.chapters (id, course_id, title, display_order) VALUES
    ('00000000-0000-0000-0002-aaaaaaaaaaab', v_d2k_id, '2일차 — Stateless 서비스 1개 K3s 로', 2),
    ('00000000-0000-0000-0002-aaaaaaaaaaac', v_d2k_id, '3일차 — Stateful (DB) → StatefulSet + PVC', 3),
    ('00000000-0000-0000-0002-aaaaaaaaaaad', v_d2k_id, '4일차 — Ingress + Cloudflare Tunnel 통합', 4),
    ('00000000-0000-0000-0002-aaaaaaaaaaae', v_d2k_id, '5일차 — Helm 차트로 묶기 + 환경별 values', 5),
    ('00000000-0000-0000-0002-aaaaaaaaaaaf', v_d2k_id, '6일차 — Prometheus + Grafana 모니터링', 6),
    ('00000000-0000-0000-0002-aaaaaaaaaab0', v_d2k_id, '7일차 — GitOps (ArgoCD) 로 자동 배포', 7)
  ON CONFLICT (id) DO NOTHING;

  -- 3) 온체인 개발 입문 ------------------------------------------------------
  INSERT INTO catalog.courses (id, creator_id, title, description, category_id, status, published_at)
  SELECT v_chain_id, v_creator,
    '온체인 개발 입문 7일 — Solidity + Hardhat + ERC-20/721 + USDC 결제',
    'lemuel-token + cert-minter + USDC 결제 컨트랙트 실 코드 기반. ' ||
    'Solidity → ERC-20/721 → 결제/Escrow → testnet 배포 → Frontend 연동 → 자체 노드 운영까지.',
    id, 'PUBLISHED', now()
  FROM catalog.categories WHERE slug = 'blockchain'
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title;

  v_chapter_id := '00000000-0000-0000-0003-aaaaaaaaaaaa';
  INSERT INTO catalog.chapters (id, course_id, title, display_order)
  VALUES (v_chapter_id, v_chain_id, '1일차 — Solidity + Hardhat 첫 컨트랙트', 1)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title;

  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview) VALUES
    ('00000000-0000-0000-0031-000000000001', v_chapter_id, '도구 선택 — Hardhat vs Foundry vs Truffle', 420, 1, TRUE),
    ('00000000-0000-0000-0031-000000000002', v_chapter_id, '5분 부트스트랩 + 디렉토리 구조', 480, 2, FALSE),
    ('00000000-0000-0000-0031-000000000003', v_chapter_id, '첫 컨트랙트 — Counter (가장 작은 예)', 540, 3, FALSE),
    ('00000000-0000-0000-0031-000000000004', v_chapter_id, 'pure / view / external / payable 키워드', 480, 4, FALSE),
    ('00000000-0000-0000-0031-000000000005', v_chapter_id, 'event + indexed 의 의미', 420, 5, FALSE),
    ('00000000-0000-0000-0031-000000000006', v_chapter_id, '테스트 + 가스 측정 (REPORT_GAS=true)', 540, 6, FALSE),
    ('00000000-0000-0000-0031-000000000007', v_chapter_id, '입문자 함정 5가지 (버전/underflow/require)', 600, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, duration_sec = EXCLUDED.duration_sec;

  INSERT INTO catalog.chapters (id, course_id, title, display_order) VALUES
    ('00000000-0000-0000-0003-aaaaaaaaaaab', v_chain_id, '2일차 — ERC-20 토큰 발행 + OpenZeppelin', 2),
    ('00000000-0000-0000-0003-aaaaaaaaaaac', v_chain_id, '3일차 — ERC-721 NFT + 메타데이터 + Soulbound', 3),
    ('00000000-0000-0000-0003-aaaaaaaaaaad', v_chain_id, '4일차 — 결제/Escrow 컨트랙트 + 보안 패턴', 4),
    ('00000000-0000-0000-0003-aaaaaaaaaaae', v_chain_id, '5일차 — testnet 배포 (Sepolia) + Etherscan 검증', 5),
    ('00000000-0000-0000-0003-aaaaaaaaaaaf', v_chain_id, '6일차 — Frontend 연동 (ethers.js + MetaMask)', 6),
    ('00000000-0000-0000-0003-aaaaaaaaaab0', v_chain_id, '7일차 — 실 운영 (자체 노드 + 인덱서 + 백엔드)', 7)
  ON CONFLICT (id) DO NOTHING;

  -- 4) TypeScript + Next.js 15 풀스택 ----------------------------------------
  INSERT INTO catalog.courses (id, creator_id, title, description, category_id, status, published_at)
  SELECT v_next_id, v_creator,
    'TypeScript + Next.js 15 풀스택 7일',
    'jen.lemuel.co.kr + academy.lemuel.co.kr 실 사례. ' ||
    'App Router → Server/Client → Server Action → 인증 → 디자인시스템 → API 통합 → Vercel 배포.',
    id, 'PUBLISHED', now()
  FROM catalog.categories WHERE slug = 'frontend'
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title;

  v_chapter_id := '00000000-0000-0000-0004-aaaaaaaaaaaa';
  INSERT INTO catalog.chapters (id, course_id, title, display_order)
  VALUES (v_chapter_id, v_next_id, '1일차 — App Router 첫 페이지', 1)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title;

  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview) VALUES
    ('00000000-0000-0000-0041-000000000001', v_chapter_id, '왜 Next.js 15 + React 19', 420, 1, TRUE),
    ('00000000-0000-0000-0041-000000000002', v_chapter_id, '5분 부트스트랩 (Turbopack)', 360, 2, FALSE),
    ('00000000-0000-0000-0041-000000000003', v_chapter_id, 'App Router 디렉토리 + 동적 라우트', 540, 3, FALSE),
    ('00000000-0000-0000-0041-000000000004', v_chapter_id, 'Server Component 가 default 인 의미', 600, 4, FALSE),
    ('00000000-0000-0000-0041-000000000005', v_chapter_id, '"use client" 를 언제 써야 하나', 540, 5, FALSE),
    ('00000000-0000-0000-0041-000000000006', v_chapter_id, '첫 API Route + healthz', 480, 6, FALSE),
    ('00000000-0000-0000-0041-000000000007', v_chapter_id, '입문자 함정 5가지 (직렬화/캐시/env/Image)', 720, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, duration_sec = EXCLUDED.duration_sec;

  INSERT INTO catalog.chapters (id, course_id, title, display_order) VALUES
    ('00000000-0000-0000-0004-aaaaaaaaaaab', v_next_id, '2일차 — 데이터 fetching + 캐싱 + revalidate', 2),
    ('00000000-0000-0000-0004-aaaaaaaaaaac', v_next_id, '3일차 — Server Action + Form 처리', 3),
    ('00000000-0000-0000-0004-aaaaaaaaaaad', v_next_id, '4일차 — 인증 (NextAuth.js v5 / Clerk)', 4),
    ('00000000-0000-0000-0004-aaaaaaaaaaae', v_next_id, '5일차 — Tailwind + shadcn/ui 디자인 시스템', 5),
    ('00000000-0000-0000-0004-aaaaaaaaaaaf', v_next_id, '6일차 — API 통합 (axios / SWR / TanStack Query)', 6),
    ('00000000-0000-0000-0004-aaaaaaaaaab0', v_next_id, '7일차 — 배포 (Vercel + Docker)', 7)
  ON CONFLICT (id) DO NOTHING;
END $$;
