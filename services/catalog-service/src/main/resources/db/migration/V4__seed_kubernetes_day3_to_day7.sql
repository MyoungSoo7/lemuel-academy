-- K8s 입문 7일 코스의 3~7일차 챕터 제목 정식화 + 7개씩 레슨 채워넣기.
-- 챕터 UUID 는 V2 에서 미리 만들어둔 placeholder 를 사용.

DO $$
DECLARE
  v_d3 UUID := '00000000-0000-0000-0000-00000000d3c3';
  v_d4 UUID := '00000000-0000-0000-0000-00000000d4c4';
  v_d5 UUID := '00000000-0000-0000-0000-00000000d5c5';
  v_d6 UUID := '00000000-0000-0000-0000-00000000d6c6';
  v_d7 UUID := '00000000-0000-0000-0000-00000000d7c7';
BEGIN
  -- 챕터 제목 정식화
  UPDATE catalog.chapters SET title = '3일차 — ConfigMap / Secret / Volume / PVC' WHERE id = v_d3;
  UPDATE catalog.chapters SET title = '4일차 — 무중단 배포 (RollingUpdate / Canary / Blue-Green)' WHERE id = v_d4;
  UPDATE catalog.chapters SET title = '5일차 — 모니터링 + 로깅 (Prometheus / Grafana / Loki)' WHERE id = v_d5;
  UPDATE catalog.chapters SET title = '6일차 — 보안 + RBAC + NetworkPolicy' WHERE id = v_d6;
  UPDATE catalog.chapters SET title = '7일차 — 종합 프로젝트: 작은 SaaS 운영' WHERE id = v_d7;

  -- 3일차 레슨
  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview) VALUES
    ('00000000-0000-0000-0003-000000000001', v_d3, 'ConfigMap — 환경별 설정값 분리', 480, 1, FALSE),
    ('00000000-0000-0000-0003-000000000002', v_d3, 'Secret — 비밀번호 관리 + base64 ≠ 암호화 함정', 540, 2, FALSE),
    ('00000000-0000-0000-0003-000000000003', v_d3, 'Volume 종류 — emptyDir / hostPath / configMap / PVC', 480, 3, FALSE),
    ('00000000-0000-0000-0003-000000000004', v_d3, 'PV / PVC — 표준 디스크 인터페이스', 600, 4, FALSE),
    ('00000000-0000-0000-0003-000000000005', v_d3, 'Dynamic Provisioning + StorageClass', 540, 5, FALSE),
    ('00000000-0000-0000-0003-000000000006', v_d3, 'AccessMode 3가지 — RWO/ROX/RWX', 360, 6, FALSE),
    ('00000000-0000-0000-0003-000000000007', v_d3, '실습: Postgres + ConfigMap + Secret + PVC 한 통', 720, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, duration_sec = EXCLUDED.duration_sec, display_order = EXCLUDED.display_order;

  -- 4일차 레슨
  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview) VALUES
    ('00000000-0000-0000-0004-000000000001', v_d4, 'RollingUpdate — Deployment 의 기본 무중단 전략', 540, 1, FALSE),
    ('00000000-0000-0000-0004-000000000002', v_d4, 'maxSurge / maxUnavailable — 갈아끼우는 속도 조절', 420, 2, FALSE),
    ('00000000-0000-0000-0004-000000000003', v_d4, 'readinessProbe 가 없으면 무중단이 아니다', 480, 3, FALSE),
    ('00000000-0000-0000-0004-000000000004', v_d4, 'Canary — replica 비율 vs Service Mesh', 540, 4, FALSE),
    ('00000000-0000-0000-0004-000000000005', v_d4, 'Blue-Green — Service selector 스위치 패턴', 480, 5, FALSE),
    ('00000000-0000-0000-0004-000000000006', v_d4, 'Rollback — kubectl rollout undo 한 줄', 360, 6, FALSE),
    ('00000000-0000-0000-0004-000000000007', v_d4, 'Graceful Shutdown — preStop sleep + SIGTERM 처리', 600, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, duration_sec = EXCLUDED.duration_sec, display_order = EXCLUDED.display_order;

  -- 5일차 레슨
  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview) VALUES
    ('00000000-0000-0000-0005-000000000001', v_d5, 'Observability 3 기둥 — metrics / logs / traces', 420, 1, FALSE),
    ('00000000-0000-0000-0005-000000000002', v_d5, 'Prometheus — Pull 방식 메트릭 수집', 540, 2, FALSE),
    ('00000000-0000-0000-0005-000000000003', v_d5, 'ServiceMonitor + 앱이 /metrics 노출하기', 480, 3, FALSE),
    ('00000000-0000-0000-0005-000000000004', v_d5, 'PromQL 기초 — rate / sum / 임계값', 540, 4, FALSE),
    ('00000000-0000-0000-0005-000000000005', v_d5, 'Grafana — 대시보드 import + 알람 룰', 480, 5, FALSE),
    ('00000000-0000-0000-0005-000000000006', v_d5, 'Loki + LogQL — 가벼운 로그 집계', 540, 6, FALSE),
    ('00000000-0000-0000-0005-000000000007', v_d5, '실습: kube-prometheus-stack Helm 설치 + 4가지 필수 알람', 720, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, duration_sec = EXCLUDED.duration_sec, display_order = EXCLUDED.display_order;

  -- 6일차 레슨
  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview) VALUES
    ('00000000-0000-0000-0006-000000000001', v_d6, 'RBAC — Role / RoleBinding / Subject 4종', 540, 1, FALSE),
    ('00000000-0000-0000-0006-000000000002', v_d6, 'Least Privilege — cluster-admin 의 함정', 420, 2, FALSE),
    ('00000000-0000-0000-0006-000000000003', v_d6, 'ServiceAccount — Pod 의 신원과 default SA 의 함정', 540, 3, FALSE),
    ('00000000-0000-0000-0006-000000000004', v_d6, 'NetworkPolicy + CNI (Calico/Cilium) 가 지원해야 동작', 600, 4, FALSE),
    ('00000000-0000-0000-0006-000000000005', v_d6, 'Secret 보안 — etcd 암호화 / SOPS / Sealed Secrets', 540, 5, FALSE),
    ('00000000-0000-0000-0006-000000000006', v_d6, 'Pod Security Admission — restricted 등급 + securityContext', 480, 6, FALSE),
    ('00000000-0000-0000-0006-000000000007', v_d6, '실습: 운영 시작 전 7가지 보안 체크리스트', 600, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, duration_sec = EXCLUDED.duration_sec, display_order = EXCLUDED.display_order;

  -- 7일차 레슨
  INSERT INTO catalog.lessons (id, chapter_id, title, duration_sec, display_order, is_preview) VALUES
    ('00000000-0000-0000-0007-000000000001', v_d7, '프로젝트 개요 — Lemuel-Todo SaaS', 360, 1, FALSE),
    ('00000000-0000-0000-0007-000000000002', v_d7, 'Helm Chart 구조 + 템플릿 변수', 600, 2, FALSE),
    ('00000000-0000-0000-0007-000000000003', v_d7, 'values-staging / values-prod 분리', 420, 3, FALSE),
    ('00000000-0000-0000-0007-000000000004', v_d7, '1~6일차 모든 개념을 한 chart 에 통합', 720, 4, FALSE),
    ('00000000-0000-0000-0007-000000000005', v_d7, 'GitOps with ArgoCD — git push = 배포', 600, 5, FALSE),
    ('00000000-0000-0000-0007-000000000006', v_d7, 'Application + automated sync (prune/selfHeal)', 480, 6, FALSE),
    ('00000000-0000-0000-0007-000000000007', v_d7, '7일 후 — 다음 학습 로드맵 (Mesh / HPA / Velero / CKAD)', 540, 7, FALSE)
  ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, duration_sec = EXCLUDED.duration_sec, display_order = EXCLUDED.display_order;
END $$;
