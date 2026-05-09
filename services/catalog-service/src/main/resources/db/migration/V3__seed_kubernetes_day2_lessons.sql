-- Kubernetes 입문 — 2일차 챕터 + 7개 레슨 추가.
-- V2 에서 placeholder 로 만든 chapter (id = ...d2c2) 에 레슨을 채워넣음.

DO $$
DECLARE
  v_chapter2_id UUID := '00000000-0000-0000-0000-00000000d2c2';
BEGIN
  -- 챕터 제목 다듬기 (V2 에서 placeholder 로 들어간 텍스트를 정식 문구로)
  UPDATE catalog.chapters
     SET title = '2일차 — Pod / Deployment / Service / Ingress 4종 세트',
         display_order = 2
   WHERE id = v_chapter2_id;

  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview)
  VALUES
    ('00000000-0000-0000-0002-000000000001', v_chapter2_id,
     'Pod 가 뭔가요? 왜 컨테이너를 직접 안 다루나', 480, 1, TRUE),
    ('00000000-0000-0000-0002-000000000002', v_chapter2_id,
     '실습: 첫 Pod yaml 한 통 적용하기', 540, 2, FALSE),
    ('00000000-0000-0000-0002-000000000003', v_chapter2_id,
     'Deployment — N개 굴리기 + 무중단 배포의 핵심', 720, 3, FALSE),
    ('00000000-0000-0000-0002-000000000004', v_chapter2_id,
     'Service 4종 (ClusterIP / NodePort / LoadBalancer / ExternalName) 언제 뭘 쓰나', 600, 4, FALSE),
    ('00000000-0000-0000-0002-000000000005', v_chapter2_id,
     'Ingress — HTTP 도메인/경로 라우팅', 600, 5, FALSE),
    ('00000000-0000-0000-0002-000000000006', v_chapter2_id,
     '4종 세트가 함께 그리는 트래픽 흐름도', 480, 6, FALSE),
    ('00000000-0000-0000-0002-000000000007', v_chapter2_id,
     '실습: minikube 에서 nginx 5분 안에 노출하기', 720, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    duration_sec = EXCLUDED.duration_sec,
    display_order = EXCLUDED.display_order;
END $$;
