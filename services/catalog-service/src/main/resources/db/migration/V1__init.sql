CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS catalog.categories (
  id SERIAL PRIMARY KEY,
  slug TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  display_order INT DEFAULT 0
);

INSERT INTO catalog.categories (slug, name, display_order) VALUES
  ('programming','프로그래밍',1),
  ('design','디자인',2),
  ('business','비즈니스',3),
  ('language','어학',4),
  ('hobby','취미',5)
ON CONFLICT (slug) DO NOTHING;

CREATE TABLE IF NOT EXISTS catalog.courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  creator_id UUID NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  thumbnail_url TEXT,
  category_id INT REFERENCES catalog.categories(id),
  status TEXT NOT NULL DEFAULT 'DRAFT',
  view_count BIGINT DEFAULT 0,
  rating_avg NUMERIC(3,2),
  rating_count INT DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT now(),
  published_at TIMESTAMPTZ,
  CONSTRAINT courses_status_check CHECK
    (status IN ('DRAFT','REVIEW','PUBLISHED','REJECTED'))
);
CREATE INDEX IF NOT EXISTS idx_courses_status_published
  ON catalog.courses(status, published_at DESC NULLS LAST);
CREATE INDEX IF NOT EXISTS idx_courses_creator ON catalog.courses(creator_id);
CREATE INDEX IF NOT EXISTS idx_courses_title_trgm
  ON catalog.courses USING gin (title gin_trgm_ops);

CREATE TABLE IF NOT EXISTS catalog.chapters (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  course_id UUID REFERENCES catalog.courses(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  display_order INT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_chapters_course ON catalog.chapters(course_id, display_order);

CREATE TABLE IF NOT EXISTS catalog.lessons (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  chapter_id UUID REFERENCES catalog.chapters(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  video_id UUID,
  duration_sec INT,
  display_order INT NOT NULL DEFAULT 0,
  is_preview BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_lessons_chapter ON catalog.lessons(chapter_id, display_order);

CREATE TABLE IF NOT EXISTS catalog.reviews (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  course_id UUID REFERENCES catalog.courses(id) ON DELETE CASCADE,
  user_id UUID NOT NULL,
  rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  content TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(course_id, user_id)
);
