-- =============================================
-- 数据库索引优化 (P2.5)
-- 在 game_db 数据库上执行
-- =============================================

USE game_db;

-- 题目按学科查询（题型、学科选择页）
CREATE INDEX IF NOT EXISTS idx_questions_category ON questions(category);

-- 题目按类型筛选
CREATE INDEX IF NOT EXISTS idx_questions_type ON questions(type);

-- 错题本按用户查询（高频查询）
CREATE INDEX IF NOT EXISTS idx_wrong_questions_user ON wrong_questions(user_id);

-- 游戏历史按用户+时间查询（积分页历史列表）
CREATE INDEX IF NOT EXISTS idx_game_history_user ON game_history(user_id, created_at);

-- 用户排名查询优化
CREATE INDEX IF NOT EXISTS idx_users_score ON users(total_score);

SELECT 'Indexes created successfully' AS result;
