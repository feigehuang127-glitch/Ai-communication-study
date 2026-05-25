-- 扩展 users 表
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS bio TEXT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS ai_sensitivity ENUM('low','medium','high') DEFAULT 'medium';

-- 扩展 questions 表
ALTER TABLE questions
  ADD COLUMN IF NOT EXISTS college ENUM('ai','comm') DEFAULT 'comm',
  ADD COLUMN IF NOT EXISTS tags JSON DEFAULT NULL;

-- 扩展 wrong_questions 表
ALTER TABLE wrong_questions
  ADD COLUMN IF NOT EXISTS college ENUM('ai','comm') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS tag VARCHAR(50) DEFAULT NULL;

-- 扩展 game_history 表
ALTER TABLE game_history
  ADD COLUMN IF NOT EXISTS college ENUM('ai','comm') DEFAULT 'comm',
  ADD COLUMN IF NOT EXISTS mode ENUM('pve','pvp','daily','practice') DEFAULT 'pve';

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_questions_college ON questions(college);
CREATE INDEX IF NOT EXISTS idx_users_total_score ON users(total_score DESC);
CREATE INDEX IF NOT EXISTS idx_game_history_user_played ON game_history(user_id, played_at DESC);
