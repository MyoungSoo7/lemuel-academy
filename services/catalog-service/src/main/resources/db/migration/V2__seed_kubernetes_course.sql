-- Kubernetes 입문 강의 시드. 1일차부터 챕터 기반으로 추가.
-- creator_id: demo-creator@academy.local (user-service V1 시드와 일치하는 가짜 UUID)
--   안전하게 처리하려고 INSERT … ON CONFLICT DO NOTHING + 명시적 UUID.

DO $$
DECLARE
  v_course_id UUID := '00000000-0000-0000-0000-00000000a8ca';  -- 'k8s-course' (deterministic)
  v_creator_id UUID := '00000000-0000-0000-0000-000000000c7e';  -- demo-creator
  v_category_id INT;
  v_chapter1_id UUID;
BEGIN
  -- 카테고리 (devops 추가)
  INSERT INTO catalog.categories (slug, name, display_order) VALUES
    ('devops','데브옵스/인프라',6)
  ON CONFLICT (slug) DO NOTHING;

  SELECT id INTO v_category_id FROM catalog.categories WHERE slug = 'devops';

  -- 코스
  INSERT INTO catalog.courses
    (id, creator_id, title, description, category_id, status, published_at)
  VALUES (
    v_course_id, v_creator_id,
    '쿠버네티스 입문 — 컨테이너 오케스트레이션 7일 완성',
    '도커는 익혔는데 쿠버네티스가 막막한 분들을 위한 7일 입문 코스. ' ||
    '클러스터 아키텍처 → Pod/Deployment/Service → 무중단 배포 → 운영까지. ' ||
    '실습 환경은 minikube (로컬) + k3s (가벼운 서버).',
    v_category_id,
    'PUBLISHED',
    now()
  ) ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    category_id = EXCLUDED.category_id,
    status = 'PUBLISHED';

  -- 1일차 챕터
  v_chapter1_id := '00000000-0000-0000-0000-00000000d1c1';
  INSERT INTO catalog.chapters (id, course_id, title, display_order)
  VALUES (v_chapter1_id, v_course_id,
          '1일차 — 쿠버네티스 개념 + 클러스터 아키텍처', 1)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, display_order = 1;

  -- 1일차 레슨들 (video_id 는 추후 강의 녹화/업로드 후 채움)
  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview)
  VALUES
    ('00000000-0000-0000-0001-000000000001', v_chapter1_id,
     '왜 도커만으론 부족한가 (오케스트레이션의 필요성)', 480, 1, TRUE),
    ('00000000-0000-0000-0001-000000000002', v_chapter1_id,
     'k8s 등장 배경 + 6대 해결 영역', 540, 2, FALSE),
    ('00000000-0000-0000-0001-000000000003', v_chapter1_id,
     'Master / Worker 노드 구조 한눈에 보기', 600, 3, FALSE),
    ('00000000-0000-0000-0001-000000000004', v_chapter1_id,
     'Control Plane 4대 컴포넌트: API server / etcd / Scheduler / Controller Manager', 720, 4, FALSE),
    ('00000000-0000-0000-0001-000000000005', v_chapter1_id,
     'Worker Node 3종: kubelet / kube-proxy / Container Runtime', 540, 5, FALSE),
    ('00000000-0000-0000-0001-000000000006', v_chapter1_id,
     '마스터 HA — Active-Active vs Leader Election + 짝수의 함정', 600, 6, FALSE),
    ('00000000-0000-0000-0001-000000000007', v_chapter1_id,
     '실습: minikube 설치 + 첫 클러스터 띄우기', 720, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    duration_sec = EXCLUDED.duration_sec,
    display_order = EXCLUDED.display_order;

  -- 2~7일차 챕터 (제목만 placeholder)
  INSERT INTO catalog.chapters (id, course_id, title, display_order) VALUES
    ('00000000-0000-0000-0000-00000000d2c2', v_course_id, '2일차 — Pod / Deployment / Service / Ingress', 2),
    ('00000000-0000-0000-0000-00000000d3c3', v_course_id, '3일차 — ConfigMap / Secret / Volume / PVC', 3),
    ('00000000-0000-0000-0000-00000000d4c4', v_course_id, '4일차 — 무중단 배포 (RollingUpdate / canary / blue-green)', 4),
    ('00000000-0000-0000-0000-00000000d5c5', v_course_id, '5일차 — 모니터링 + 로깅 (Prometheus / Loki / Grafana)', 5),
    ('00000000-0000-0000-0000-00000000d6c6', v_course_id, '6일차 — 보안 + RBAC + NetworkPolicy', 6),
    ('00000000-0000-0000-0000-00000000d7c7', v_course_id, '7일차 — 종합 프로젝트: 작은 SaaS 클러스터 운영', 7)
  ON CONFLICT (id) DO NOTHING;
END $$;
