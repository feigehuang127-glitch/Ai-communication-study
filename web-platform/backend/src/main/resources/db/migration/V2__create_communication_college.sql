-- 学院表
CREATE TABLE IF NOT EXISTS colleges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    college_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    level VARCHAR(5) DEFAULT NULL COMMENT 'L1-L5',
    `order` INT DEFAULT 0,
    prerequisites JSON DEFAULT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (college_id) REFERENCES colleges(id)
);

-- 章节表
CREATE TABLE IF NOT EXISTS chapters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    `order` INT DEFAULT 0,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- 课时表
CREATE TABLE IF NOT EXISTS lessons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content_type ENUM('video','text','quiz','code') NOT NULL DEFAULT 'text',
    content JSON NOT NULL,
    lab_ref VARCHAR(200) DEFAULT NULL,
    `order` INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id)
);

-- 用户进度表
CREATE TABLE IF NOT EXISTS user_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    lesson_id BIGINT NOT NULL,
    status ENUM('not_started','in_progress','completed') DEFAULT 'not_started',
    score INT DEFAULT 0,
    time_spent INT DEFAULT 0 COMMENT '秒',
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    UNIQUE KEY uk_user_lesson (user_id, lesson_id)
);

-- 用户技能进度表（状态机）
CREATE TABLE IF NOT EXISTS user_skills_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    skill_id VARCHAR(100) NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0-LOCKED 1-UNLOCKED 2-STUDYING 3-MASTERED',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_user_skill (user_id, skill_id)
);

-- 独立错题本表
CREATE TABLE IF NOT EXISTS user_wrong_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    question_id INT NOT NULL,
    error_count INT DEFAULT 1,
    status TINYINT DEFAULT 0 COMMENT '0-待消灭 1-已掌握',
    last_wrong_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    UNIQUE KEY uk_user_question (user_id, question_id)
);

-- 学习报告表
CREATE TABLE IF NOT EXISTS learning_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    course_id BIGINT NOT NULL,
    report_type ENUM('weekly','chapter') NOT NULL,
    content JSON NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- 沙箱会话表
CREATE TABLE IF NOT EXISTS sandbox_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    container_id VARCHAR(100),
    template_ref VARCHAR(100),
    source_course_id BIGINT DEFAULT NULL,
    source_lesson_id BIGINT DEFAULT NULL,
    status ENUM('starting','running','stopped','error') DEFAULT 'starting',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 实验室项目表
CREATE TABLE IF NOT EXISTS lab_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type ENUM('sandbox','prompt','agent') NOT NULL,
    title VARCHAR(200) NOT NULL,
    snapshot JSON NOT NULL,
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- AI 对话表
CREATE TABLE IF NOT EXISTS ai_conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    context_page VARCHAR(100),
    context_lesson_id BIGINT DEFAULT NULL,
    model_used VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS ai_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role ENUM('user','assistant') NOT NULL,
    content TEXT NOT NULL,
    tokens_used INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id)
);

-- 行为事件表（仅存结论）
CREATE TABLE IF NOT EXISTS behavior_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    session_id VARCHAR(100),
    event_type ENUM('HESITATION','CONFUSION','MASTERY','QUIT_RISK','INACTIVITY') NOT NULL,
    context JSON DEFAULT NULL,
    severity TINYINT DEFAULT 1 COMMENT '1-5',
    triggered_rule_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 游戏模式表
CREATE TABLE IF NOT EXISTS game_modes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('pve','pvp','daily') NOT NULL,
    name VARCHAR(100) NOT NULL,
    rules JSON DEFAULT NULL
);

-- PVP 对战表
CREATE TABLE IF NOT EXISTS pvp_matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player1_id INT NOT NULL,
    player2_id INT NOT NULL,
    winner_id INT DEFAULT NULL,
    questions JSON NOT NULL,
    scores JSON DEFAULT NULL,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player1_id) REFERENCES users(id),
    FOREIGN KEY (player2_id) REFERENCES users(id)
);

-- 预置通信学院和课程数据
INSERT INTO colleges (name, slug, icon, description) VALUES
('通信学院', 'comm', 'signal', '通信原理与数据通信网知识体系');

INSERT INTO courses (college_id, title, slug, level, `order`, description) VALUES
(1, '信号与系统', 'signals-and-systems', 'L1', 1, '连续与离散信号分析、傅里叶变换基础'),
(1, '通信原理', 'communication-theory', 'L2', 2, '模拟与数字调制、信道编码、传输理论'),
(1, '数据通信网', 'data-network', 'L3', 3, '路由交换、网络协议栈、TCP/IP体系结构'),
(1, '前沿专题', 'advanced-topics', 'L5', 4, '网络仿真、现代通信算法与SDN');
