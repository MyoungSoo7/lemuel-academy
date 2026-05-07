CREATE TABLE IF NOT EXISTS users_svc.users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT,
  oauth_provider TEXT,
  oauth_id TEXT,
  display_name TEXT NOT NULL,
  avatar_url TEXT,
  role TEXT NOT NULL DEFAULT 'STUDENT',
  created_at TIMESTAMPTZ DEFAULT now(),
  CONSTRAINT users_role_check CHECK (role IN ('STUDENT','CREATOR','ADMIN')),
  CONSTRAINT users_oauth_unique UNIQUE(oauth_provider, oauth_id)
);

CREATE TABLE IF NOT EXISTS users_svc.progress (
  user_id UUID REFERENCES users_svc.users(id) ON DELETE CASCADE,
  lesson_id UUID NOT NULL,
  watched_seconds INT NOT NULL DEFAULT 0,
  completed BOOLEAN NOT NULL DEFAULT FALSE,
  last_watched_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (user_id, lesson_id)
);

CREATE TABLE IF NOT EXISTS users_svc.favorites (
  user_id UUID REFERENCES users_svc.users(id) ON DELETE CASCADE,
  course_id UUID NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (user_id, course_id)
);
