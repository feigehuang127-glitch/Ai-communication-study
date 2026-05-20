-- ============================================
-- 知识竞答游戏 - 数据库初始化脚本
-- 数据库: game_db (需预先通过 IDEA 或命令行创建)
-- 执行: 通过 Python 脚本执行以保留 UTF-8 编码
--   python3 -c "import subprocess; f=open('sql/init.sql','r',encoding='utf-8'); sql=f.read(); f.close(); subprocess.run(['docker','exec','-i','mysql','mysql','-uroot','-p123456','--default-character-set=utf8mb4','game_db'],input=sql.encode('utf-8'))"
-- ============================================

-- 删除旧表（按外键依赖顺序）
DROP TABLE IF EXISTS practice_log;
DROP TABLE IF EXISTS check_in_log;
DROP TABLE IF EXISTS question_set_items;
DROP TABLE IF EXISTS question_sets;
DROP TABLE IF EXISTS wrong_questions;
DROP TABLE IF EXISTS game_history;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS users;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    role VARCHAR(10) DEFAULT 'player',
    total_score INT DEFAULT 0,
    `rank` VARCHAR(20) DEFAULT '青铜',
    last_checkin_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. 题库表
-- ============================================
CREATE TABLE questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category VARCHAR(20) NOT NULL,
    type VARCHAR(10) NOT NULL,
    content TEXT NOT NULL,
    option_a VARCHAR(200) NOT NULL,
    option_b VARCHAR(200) NOT NULL,
    option_c VARCHAR(200),
    option_d VARCHAR(200),
    answer VARCHAR(50) NOT NULL,
    explanation TEXT,
    difficulty TINYINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_q_category (category),
    INDEX idx_q_difficulty (difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. 游戏记录表
-- ============================================
CREATE TABLE game_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    category VARCHAR(20),
    result VARCHAR(20),
    correct_count INT DEFAULT 0,
    total_time_seconds INT DEFAULT 0,
    score_earned INT DEFAULT 0,
    rank_before VARCHAR(20),
    rank_after VARCHAR(20),
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_gh_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. 错题本表
-- ============================================
CREATE TABLE wrong_questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    question_id INT NOT NULL,
    wrong_count INT DEFAULT 1,
    correct_streak INT DEFAULT 0,
    last_wrong_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    INDEX idx_wq_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 5. 题集表
-- ============================================
CREATE TABLE question_sets (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 6. 题集-题目关联表
-- ============================================
CREATE TABLE question_set_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    set_id INT NOT NULL,
    question_id INT NOT NULL,
    FOREIGN KEY (set_id) REFERENCES question_sets(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 7. 签到记录表
-- ============================================
CREATE TABLE check_in_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 8. 练习记录表
-- ============================================
CREATE TABLE practice_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    type VARCHAR(20),
    category VARCHAR(20),
    duration_seconds INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_pl_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 预置测试账号 (密码均为 123456 的 BCrypt 哈希)
-- ============================================
INSERT INTO users (username, password_hash, role) VALUES
('admin',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin'),
('player', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'player');
