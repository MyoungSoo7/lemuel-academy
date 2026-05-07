CREATE TABLE IF NOT EXISTS media.videos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  creator_id UUID NOT NULL,
  original_url TEXT,
  hls_master_url TEXT,
  thumbnail_url TEXT,
  duration_sec INT,
  status TEXT NOT NULL DEFAULT 'UPLOADING',
  error_message TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  ready_at TIMESTAMPTZ,
  CONSTRAINT videos_status_check CHECK
    (status IN ('UPLOADING','UPLOADED','TRANSCODING','READY','FAILED'))
);
CREATE INDEX IF NOT EXISTS idx_videos_creator ON media.videos(creator_id);
CREATE INDEX IF NOT EXISTS idx_videos_status ON media.videos(status);
