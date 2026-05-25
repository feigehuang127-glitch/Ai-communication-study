# AI 学习平台 Phase 1-2 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建 SvelteKit + Spring Boot + FastAPI 三服务架构，完成数据库迁移，实现 JWT 认证，将通信学院从 Java Swing 迁移为 Web 应用。

**Architecture:** Docker Compose 编排 5 个服务（frontend/backend/ai-service/mysql/redis）。Spring Boot 封装现有 GameEngine 为 REST API，SvelteKit 渲染玻璃形态 UI。

**Tech Stack:** SvelteKit 2 + Tailwind CSS + Spring Boot 3 + JDK 21 + MySQL 8.0 + Redis 7 + Python 3.12 + FastAPI + Docker Compose

---

## 文件结构总览

```
game/
├── web-platform/                       # 新建 Web 平台根目录
│   ├── docker-compose.yml
│   ├── .env.example
│   ├── frontend/                       # SvelteKit 前端
│   │   ├── package.json
│   │   ├── svelte.config.js
│   │   ├── vite.config.ts
│   │   ├── tailwind.config.js
│   │   ├── src/
│   │   │   ├── app.html
│   │   │   ├── app.css                # 玻璃形态全局主题
│   │   │   ├── lib/
│   │   │   │   ├── components/
│   │   │   │   │   ├── GlassNavbar.svelte
│   │   │   │   │   ├── GlassCard.svelte
│   │   │   │   │   ├── GlassButton.svelte
│   │   │   │   │   ├── ParticleBackground.svelte
│   │   │   │   │   ├── AIWidget.svelte
│   │   │   │   │   ├── CommandPalette.svelte
│   │   │   │   │   └── Toast.svelte
│   │   │   │   ├── stores/
│   │   │   │   │   ├── auth.ts         # JWT token + user state
│   │   │   │   │   └── toast.ts        # Toast notification store
│   │   │   │   └── api/
│   │   │   │       └── client.ts       # Fetch wrapper with JWT
│   │   │   └── routes/
│   │   │       ├── +layout.svelte      # Root layout (Navbar + Particle BG)
│   │   │       ├── +page.svelte        # Home/Dashboard
│   │   │       ├── login/
│   │   │       │   └── +page.svelte
│   │   │       ├── college/
│   │   │       │   └── comm/
│   │   │       │       ├── +page.svelte              # 通信学院首页
│   │   │       │       ├── communication-theory/
│   │   │       │       │   └── +page.svelte          # 通信原理课程列表
│   │   │       │       └── data-network/
│   │   │       │           └── +page.svelte          # 数据通信网课程列表
│   │   │       ├── game/
│   │   │       │   ├── +page.svelte                  # 游戏大厅
│   │   │       │   ├── play/
│   │   │       │   │   └── +page.svelte              # 答题竞技场
│   │   │       │   └── result/
│   │   │       │       └── +page.svelte              # 结果复盘
│   │   │       └── profile/
│   │   │           ├── +page.svelte                  # 个人中心
│   │   │           ├── wrongbook/
│   │   │           │   └── +page.svelte
│   │   │           └── settings/
│   │   │               └── +page.svelte
│   │   └── static/
│   │       └── favicon.png
│   │
│   ├── backend/                        # Spring Boot 后端
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/com/platform/
│   │       │   ├── PlatformApplication.java
│   │       │   ├── config/
│   │       │   │   ├── SecurityConfig.java
│   │       │   │   ├── WebSocketConfig.java
│   │       │   │   └── CorsConfig.java
│   │       │   ├── security/
│   │       │   │   ├── JwtProvider.java
│   │       │   │   ├── JwtAuthFilter.java
│   │       │   │   └── UserDetailsServiceImpl.java
│   │       │   ├── controller/
│   │       │   │   ├── AuthController.java
│   │       │   │   ├── GameController.java
│   │       │   │   ├── CourseController.java
│   │       │   │   ├── UserController.java
│   │       │   │   └── WrongBookController.java
│   │       │   ├── service/
│   │       │   │   ├── UserService.java
│   │       │   │   ├── GameService.java      # 从 youxi 迁移并改造
│   │       │   │   └── QuestionService.java
│   │       │   ├── dto/
│   │       │   │   ├── LoginRequest.java
│   │       │   │   ├── LoginResponse.java
│   │       │   │   ├── GameStartRequest.java
│   │       │   │   ├── AnswerSubmitRequest.java
│   │       │   │   └── GameResultResponse.java
│   │       │   ├── model/
│   │       │   │   ├── User.java
│   │       │   │   ├── Question.java
│   │       │   │   ├── GameHistory.java
│   │       │   │   └── WrongQuestion.java
│   │       │   ├── repository/
│   │       │   │   ├── UserRepository.java
│   │       │   │   ├── QuestionRepository.java
│   │       │   │   ├── GameHistoryRepository.java
│   │       │   │   └── WrongQuestionRepository.java
│   │       │   └── websocket/
│   │       │       └── GameWebSocketHandler.java
│   │       └── resources/
│   │           ├── application.yml
│   │           └── db/migration/
│   │               ├── V1__extend_existing_tables.sql
│   │               └── V2__create_communication_college.sql
│   │
│   └── ai-service/                     # Python FastAPI AI 微服务
│       ├── requirements.txt
│       ├── Dockerfile
│       ├── main.py
│       ├── config.py
│       └── routers/
│           └── health.py
```

---

## Phase 1：基础设施

### Task 1.1：项目目录与 Docker Compose 搭建

**Files:**
- Create: `web-platform/docker-compose.yml`
- Create: `web-platform/.env.example`

- [ ] **Step 1: 创建目录结构**

```bash
mkdir -p web-platform/frontend/src/{lib/{components,stores,api},routes}
mkdir -p web-platform/backend/src/main/java/com/platform/{config,security,controller,service,dto,model,repository,websocket}
mkdir -p web-platform/backend/src/main/resources/db/migration
mkdir -p web-platform/ai-service/routers
```

- [ ] **Step 2: 编写 docker-compose.yml**

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: platform-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123}
      MYSQL_DATABASE: platform_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/src/main/resources/db/migration:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: platform-redis
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: platform-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/platform_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123}
      SPRING_REDIS_HOST: redis
      AI_SERVICE_URL: http://ai-service:8000
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy

  ai-service:
    build: ./ai-service
    container_name: platform-ai
    ports:
      - "8000:8000"
    environment:
      ANTHROPIC_API_KEY: ${ANTHROPIC_API_KEY}
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY}
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock  # 调度沙箱容器

  frontend:
    build: ./frontend
    container_name: platform-frontend
    ports:
      - "5173:5173"
    environment:
      VITE_API_URL: http://localhost:8080
      VITE_WS_URL: ws://localhost:8080
    depends_on:
      - backend
    develop:
      watch:
        - path: ./frontend/src
          action: sync
          target: /app/src

volumes:
  mysql_data:
```

- [ ] **Step 3: 编写 .env.example**

```
MYSQL_ROOT_PASSWORD=root123
ANTHROPIC_API_KEY=sk-ant-xxx
OPENAI_API_KEY=sk-xxx
DEEPSEEK_API_KEY=sk-xxx
```

- [ ] **Step 4: 验证 Docker Compose 语法**

```bash
cd web-platform && docker compose config
```
Expected: 输出完整配置，无报错。

- [ ] **Step 5: Commit**

```bash
git add web-platform/docker-compose.yml web-platform/.env.example
git commit -m "feat: add Docker Compose orchestration for 5 services"
```

---

### Task 1.2：Spring Boot 项目初始化

**Files:**
- Create: `web-platform/backend/pom.xml`
- Create: `web-platform/backend/src/main/java/com/platform/PlatformApplication.java`
- Create: `web-platform/backend/src/main/resources/application.yml`
- Create: `web-platform/backend/Dockerfile`

- [ ] **Step 1: 编写 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
    </parent>

    <groupId>com.platform</groupId>
    <artifactId>ai-learning-platform</artifactId>
    <version>1.0.0</version>
    <name>AI Learning Platform Backend</name>

    <properties>
        <java.version>21</java.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencies>
        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- WebSocket -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <!-- Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <!-- JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <!-- HikariCP (Spring Boot 内置, 无需额外依赖) -->
        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <!-- BCrypt (Spring Security 内置) -->
        <!-- Flyway -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 编写 PlatformApplication.java**

```java
package com.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
}
```

- [ ] **Step 3: 编写 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/platform_db}
    username: ${SPRING_DATASOURCE_USERNAME:root}
    password: ${SPRING_DATASOURCE_PASSWORD:root123}
    hikari:
      maximum-pool-size: 10
      connection-timeout: 3000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
  data:
    redis:
      host: ${SPRING_REDIS_HOST:localhost}
      port: 6379

jwt:
  secret: ${JWT_SECRET:changeme-please-use-a-256-bit-secret-in-production}
  expiration-ms: 86400000  # 24h

ai-service:
  url: ${AI_SERVICE_URL:http://localhost:8000}
```

- [ ] **Step 4: 编写 Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 5: Commit**

```bash
git add web-platform/backend/
git commit -m "feat: initialize Spring Boot project with JPA, Security, WebSocket, Flyway"
```

---

### Task 1.3：SvelteKit 前端项目初始化

**Files:**
- Create: `web-platform/frontend/package.json`
- Create: `web-platform/frontend/svelte.config.js`
- Create: `web-platform/frontend/vite.config.ts`
- Create: `web-platform/frontend/tailwind.config.js`
- Create: `web-platform/frontend/src/app.html`
- Create: `web-platform/frontend/src/app.css`
- Create: `web-platform/frontend/Dockerfile`

- [ ] **Step 1: 编写 package.json**

```json
{
  "name": "ai-learning-platform-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "vite dev",
    "build": "vite build",
    "preview": "vite preview"
  },
  "devDependencies": {
    "@sveltejs/adapter-node": "^5.2.0",
    "@sveltejs/kit": "^2.5.0",
    "@sveltejs/vite-plugin-svelte": "^3.1.0",
    "autoprefixer": "^10.4.19",
    "postcss": "^8.4.38",
    "svelte": "^4.2.0",
    "tailwindcss": "^3.4.0",
    "vite": "^5.2.0"
  },
  "dependencies": {
    "json-rules-engine": "^7.3.0"
  },
  "type": "module"
}
```

- [ ] **Step 2: 编写 svelte.config.js**

```javascript
import adapter from '@sveltejs/adapter-node';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

export default {
  preprocess: vitePreprocess(),
  kit: {
    adapter: adapter(),
    alias: {
      '$lib': 'src/lib',
      '$components': 'src/lib/components',
      '$stores': 'src/lib/stores'
    }
  }
};
```

- [ ] **Step 3: 编写 vite.config.ts**

```typescript
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [sveltekit()],
  server: {
    host: '0.0.0.0',
    port: 5173
  }
});
```

- [ ] **Step 4: 编写 tailwind.config.js**

```javascript
export default {
  content: ['./src/**/*.{html,js,svelte,ts}'],
  theme: {
    extend: {
      colors: {
        'glass-bg': 'rgba(255, 255, 255, 0.06)',
        'glass-border': 'rgba(255, 255, 255, 0.12)',
        'deep-blue': {
          900: '#0a0e27',
          800: '#0d1b3e',
          700: '#112855'
        },
        'accent-blue': '#64b4ff',
        'accent-purple': '#c896ff',
        'accent-green': '#64c896',
        'accent-gold': '#ffb464'
      },
      backdropBlur: {
        'glass': '20px'
      },
      boxShadow: {
        'glass': '0 8px 32px rgba(0, 0, 0, 0.3)'
      }
    }
  },
  plugins: []
};
```

- [ ] **Step 5: 编写 app.css（玻璃形态全局主题）**

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

:root {
  --bg-primary: #0a0e27;
  --bg-secondary: #0d1b3e;
  --glass-bg: rgba(255, 255, 255, 0.06);
  --glass-border: rgba(255, 255, 255, 0.12);
  --glass-hover: rgba(255, 255, 255, 0.1);
  --text-primary: #e0e8ff;
  --text-secondary: #8899bb;
  --accent-blue: #64b4ff;
  --accent-purple: #c896ff;
  --accent-green: #64c896;
  --accent-gold: #ffb464;
  --accent-red: #ff6464;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  min-height: 100vh;
  overflow-x: hidden;
}

/* Glass card base */
.glass {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.glass:hover {
  background: var(--glass-hover);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
}

/* Gradient text */
.gradient-text {
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Glow orb (background decoration) */
.glow-orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.15;
  pointer-events: none;
  z-index: 0;
}

/* Scrollbar */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}
```

- [ ] **Step 6: 编写 app.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link rel="icon" href="/favicon.png" />
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <title>AI Academy</title>
    %sveltekit.head%
  </head>
  <body>
    %sveltekit.body%
  </body>
</html>
```

- [ ] **Step 7: 编写 Dockerfile**

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
```

- [ ] **Step 8: Commit**

```bash
git add web-platform/frontend/
git commit -m "feat: initialize SvelteKit project with Tailwind and glassmorphism theme"
```

---

### Task 1.4：数据库迁移脚本

**Files:**
- Create: `web-platform/backend/src/main/resources/db/migration/V1__extend_existing_tables.sql`
- Create: `web-platform/backend/src/main/resources/db/migration/V2__create_communication_college.sql`

- [ ] **Step 1: 编写 V1__extend_existing_tables.sql**

```sql
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
```

- [ ] **Step 2: 编写 V2__create_communication_college.sql**

```sql
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
```

- [ ] **Step 3: Commit**

```bash
git add web-platform/backend/src/main/resources/db/migration/
git commit -m "feat: add Flyway migration scripts for all 18+ tables"
```

---

### Task 1.5：AI 微服务骨架

**Files:**
- Create: `web-platform/ai-service/requirements.txt`
- Create: `web-platform/ai-service/main.py`
- Create: `web-platform/ai-service/config.py`
- Create: `web-platform/ai-service/routers/health.py`
- Create: `web-platform/ai-service/Dockerfile`

- [ ] **Step 1: 编写 requirements.txt**

```
fastapi==0.111.0
uvicorn[standard]==0.30.1
httpx==0.27.0
anthropic==0.31.0
openai==1.30.0
langchain==0.2.0
langchain-community==0.2.0
llama-index==0.10.0
docker==7.0.0
redis==5.0.0
pydantic==2.7.0
pydantic-settings==2.3.0
```

- [ ] **Step 2: 编写 config.py**

```python
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    anthropic_api_key: str = ""
    openai_api_key: str = ""
    deepseek_api_key: str = ""
    redis_host: str = "localhost"
    redis_port: int = 6379
    sandbox_memory_limit: str = "128m"
    sandbox_cpu_limit: int = 500_000_000  # 0.5 CPU
    sandbox_timeout: int = 300  # 5 minutes
    sandbox_idle_timeout: int = 1800  # 30 minutes

    model_config = {"env_file": ".env", "extra": "ignore"}

settings = Settings()
```

- [ ] **Step 3: 编写 main.py**

```python
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers import health

app = FastAPI(title="AI Learning Platform - AI Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router)
```

- [ ] **Step 4: 编写 routers/health.py**

```python
from fastapi import APIRouter

router = APIRouter(tags=["health"])

@router.get("/health")
async def health_check():
    return {"status": "ok", "service": "ai-service"}
```

- [ ] **Step 5: 编写 Dockerfile**

```dockerfile
FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

- [ ] **Step 6: Commit**

```bash
git add web-platform/ai-service/
git commit -m "feat: initialize FastAPI AI service with health endpoint"
```

---

## Phase 2：通信学院 Web 化

### Task 2.1：JWT 认证（Spring Boot）

**Files:**
- Create: `web-platform/backend/src/main/java/com/platform/security/JwtProvider.java`
- Create: `web-platform/backend/src/main/java/com/platform/security/JwtAuthFilter.java`
- Create: `web-platform/backend/src/main/java/com/platform/security/UserDetailsServiceImpl.java`
- Create: `web-platform/backend/src/main/java/com/platform/config/SecurityConfig.java`
- Create: `web-platform/backend/src/main/java/com/platform/config/CorsConfig.java`
- Create: `web-platform/backend/src/main/java/com/platform/dto/LoginRequest.java`
- Create: `web-platform/backend/src/main/java/com/platform/dto/LoginResponse.java`
- Create: `web-platform/backend/src/main/java/com/platform/controller/AuthController.java`

- [ ] **Step 1: 编写 JwtProvider.java**

```java
package com.platform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

- [ ] **Step 2: 编写 JwtAuthFilter.java**

```java
package com.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtProvider jwtProvider, UserDetailsServiceImpl userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtProvider.validate(token)) {
                String username = jwtProvider.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
```

- [ ] **Step 3: 编写 SecurityConfig.java**

```java
package com.platform.config;

import com.platform.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/health", "/ws/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

- [ ] **Step 4: 编写 CorsConfig.java**

```java
package com.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        var config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 5: 编写 UserDetailsServiceImpl.java**

```java
package com.platform.security;

import com.platform.model.User;
import com.platform.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()))
        );
    }
}
```

- [ ] **Step 6: 编写 DTO**

```java
// LoginRequest.java
package com.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

```java
// LoginResponse.java
package com.platform.dto;

public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private int totalScore;
    private String rank;

    public LoginResponse(String token, String username, String role, int totalScore, String rank) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.totalScore = totalScore;
        this.rank = rank;
    }

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public int getTotalScore() { return totalScore; }
    public String getRank() { return rank; }
}
```

- [ ] **Step 7: 编写 AuthController.java**

```java
package com.platform.controller;

import com.platform.dto.LoginRequest;
import com.platform.dto.LoginResponse;
import com.platform.model.User;
import com.platform.security.JwtProvider;
import com.platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtProvider jwtProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authManager,
                          JwtProvider jwtProvider,
                          UserService userService) {
        this.authManager = authManager;
        this.jwtProvider = jwtProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userService.findByUsername(request.getUsername());
        String token = jwtProvider.generateToken(user.getUsername(), (long) user.getId(), user.getRole());
        return ResponseEntity.ok(new LoginResponse(
                token, user.getUsername(), user.getRole(), user.getTotalScore(), user.getRank()));
    }
}
```

- [ ] **Step 8: Commit**

```bash
git add web-platform/backend/src/main/java/com/platform/security/
git add web-platform/backend/src/main/java/com/platform/config/
git add web-platform/backend/src/main/java/com/platform/dto/
git add web-platform/backend/src/main/java/com/platform/controller/AuthController.java
git commit -m "feat: implement JWT authentication with Spring Security"
```

---

### Task 2.2：JPA Model + Repository 层

**Files:**
- Create: `web-platform/backend/src/main/java/com/platform/model/User.java`
- Create: `web-platform/backend/src/main/java/com/platform/model/Question.java`
- Create: `web-platform/backend/src/main/java/com/platform/model/GameHistory.java`
- Create: `web-platform/backend/src/main/java/com/platform/model/WrongQuestion.java`
- Create: `web-platform/backend/src/main/java/com/platform/repository/UserRepository.java`
- Create: `web-platform/backend/src/main/java/com/platform/repository/QuestionRepository.java`
- Create: `web-platform/backend/src/main/java/com/platform/repository/GameHistoryRepository.java`
- Create: `web-platform/backend/src/main/java/com/platform/repository/WrongQuestionRepository.java`

- [ ] **Step 1: 编写 User.java**

```java
package com.platform.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(length = 20)
    private String role = "player";

    @Column(name = "total_score")
    private Integer totalScore = 0;

    @Column(length = 20)
    private String rank = "青铜";

    @Column(name = "last_checkin_date")
    private LocalDate lastCheckinDate;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 10)
    private String aiSensitivity = "medium";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public LocalDate getLastCheckinDate() { return lastCheckinDate; }
    public void setLastCheckinDate(LocalDate lastCheckinDate) { this.lastCheckinDate = lastCheckinDate; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAiSensitivity() { return aiSensitivity; }
    public void setAiSensitivity(String aiSensitivity) { this.aiSensitivity = aiSensitivity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 2: 编写 Question.java**

```java
package com.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 20)
    private String type;

    @Column(name = "option_a", length = 200, nullable = false)
    private String optionA;

    @Column(name = "option_b", length = 200, nullable = false)
    private String optionB;

    @Column(name = "option_c", length = 200)
    private String optionC;

    @Column(name = "option_d", length = 200)
    private String optionD;

    @Column(length = 50, nullable = false)
    private String answer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(length = 20)
    private String category;

    @Column(length = 20)
    private String college = "comm";

    @Column(columnDefinition = "TINYINT")
    private Integer difficulty;

    @Column(columnDefinition = "JSON")
    private String tags;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and setters
    public Integer getId() { return id; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getAnswer() { return answer; }
    public String getExplanation() { return explanation; }
    public String getCategory() { return category; }
    public String getCollege() { return college; }
    public Integer getDifficulty() { return difficulty; }
    public String getTags() { return tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters omitted for brevity — generate in IDE or write manually
}
```

- [ ] **Step 3: 编写 UserRepository.java**

```java
package com.platform.repository;

import com.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

- [ ] **Step 4: 编写 QuestionRepository.java**

```java
package com.platform.repository;

import com.platform.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByCollegeAndDifficultyBetween(String college, Integer minDiff, Integer maxDiff);

    @Query("SELECT q FROM Question q WHERE q.college = :college " +
           "AND q.difficulty BETWEEN :minDiff AND :maxDiff " +
           "ORDER BY FUNCTION('RAND') LIMIT :limit")
    List<Question> findRandomQuestions(@Param("college") String college,
                                       @Param("minDiff") Integer minDiff,
                                       @Param("maxDiff") Integer maxDiff,
                                       @Param("limit") Integer limit);

    List<Question> findByCollegeAndCategory(String college, String category);

    long countByCollege(String college);
}
```

- [ ] **Step 5: Commit**

```bash
git add web-platform/backend/src/main/java/com/platform/model/
git add web-platform/backend/src/main/java/com/platform/repository/
git commit -m "feat: add JPA entities and repositories for core tables"
```

---

### Task 2.3：GameService 迁移 + REST API

**Files:**
- Create: `web-platform/backend/src/main/java/com/platform/service/UserService.java`
- Create: `web-platform/backend/src/main/java/com/platform/service/GameService.java`
- Create: `web-platform/backend/src/main/java/com/platform/service/QuestionService.java`
- Create: `web-platform/backend/src/main/java/com/platform/dto/GameStartRequest.java`
- Create: `web-platform/backend/src/main/java/com/platform/dto/AnswerSubmitRequest.java`
- Create: `web-platform/backend/src/main/java/com/platform/dto/GameResultResponse.java`
- Create: `web-platform/backend/src/main/java/com/platform/controller/GameController.java`
- Create: `web-platform/backend/src/main/java/com/platform/controller/UserController.java`

- [ ] **Step 1: 编写 UserService.java**

```java
package com.platform.service;

import com.platform.model.User;
import com.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Transactional
    public User checkIn(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate today = LocalDate.now();
        if (!today.equals(user.getLastCheckinDate())) {
            user.setLastCheckinDate(today);
            user.setTotalScore(user.getTotalScore() + 1);
            updateRank(user);
            userRepository.save(user);
        }
        return user;
    }

    @Transactional
    public void addScore(Integer userId, int score) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setTotalScore(user.getTotalScore() + score);
        updateRank(user);
        userRepository.save(user);
    }

    private void updateRank(User user) {
        int score = user.getTotalScore();
        if (score >= 50) user.setRank("王者");
        else if (score >= 40) user.setRank("钻石");
        else if (score >= 30) user.setRank("铂金");
        else if (score >= 20) user.setRank("黄金");
        else if (score >= 10) user.setRank("白银");
        else user.setRank("青铜");
    }
}
```

- [ ] **Step 2: 编写 GameService.java（核心答题引擎，从 youxi/GameService 迁移）**

```java
package com.platform.service;

import com.platform.dto.AnswerSubmitRequest;
import com.platform.dto.GameResultResponse;
import com.platform.model.GameHistory;
import com.platform.model.Question;
import com.platform.model.WrongQuestion;
import com.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private final QuestionRepository questionRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final WrongQuestionRepository wrongQuestionRepository;
    private final UserService userService;

    // 内存中保存进行中的游戏会话
    private final ConcurrentHashMap<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    private static final int QUESTIONS_PER_ROUND = 10;
    private static final int TIME_LIMIT_SECONDS = 10;
    private static final int TIMEOUT_EXEMPTIONS = 2;

    public GameService(QuestionRepository questionRepository,
                       GameHistoryRepository gameHistoryRepository,
                       WrongQuestionRepository wrongQuestionRepository,
                       UserService userService) {
        this.questionRepository = questionRepository;
        this.gameHistoryRepository = gameHistoryRepository;
        this.wrongQuestionRepository = wrongQuestionRepository;
        this.userService = userService;
    }

    public GameSession startGame(Integer userId, String college, String category) {
        int[] range = getDifficultyRange(userId);
        List<Question> pool = questionRepository.findRandomQuestions(
                college, range[0], range[1], QUESTIONS_PER_ROUND);

        if (pool.size() < QUESTIONS_PER_ROUND) {
            pool.addAll(questionRepository.findRandomQuestions(
                    college, range[0] - 2, range[1] + 2,
                    QUESTIONS_PER_ROUND - pool.size()));
        }

        String sessionId = UUID.randomUUID().toString();
        GameSession session = new GameSession(
                sessionId, userId, college, category, pool,
                TIME_LIMIT_SECONDS, TIMEOUT_EXEMPTIONS);
        activeSessions.put(sessionId, session);
        return session;
    }

    public boolean submitAnswer(String sessionId, int questionIndex, String userAnswer) {
        GameSession session = activeSessions.get(sessionId);
        if (session == null || session.isFinished()) return false;

        Question question = session.getQuestions().get(questionIndex);
        String correct = sortAnswer(question.getAnswer());
        String user = sortAnswer(userAnswer);
        boolean isCorrect = correct.equals(user);

        session.recordAnswer(questionIndex, isCorrect, userAnswer);
        return isCorrect;
    }

    @Transactional
    public GameResultResponse finishGame(String sessionId) {
        GameSession session = activeSessions.remove(sessionId);
        if (session == null) return null;

        int correct = session.getCorrectCount();
        int scoreEarned = calculateScore(session);
        String result = determineResult(session);

        // 保存游戏历史
        GameHistory history = new GameHistory();
        history.setUserId(session.getUserId());
        history.setCollege(session.getCollege());
        history.setCategory(session.getCategory());
        history.setResult(result);
        history.setCorrectCount(correct);
        history.setTotalTimeSeconds((int) session.getElapsedSeconds());
        history.setScoreEarned(scoreEarned);
        gameHistoryRepository.save(history);

        // 更新用户积分
        userService.addScore(session.getUserId(), scoreEarned);

        // 更新错题本
        for (var entry : session.getAnswerResults().entrySet()) {
            if (!entry.getValue()) {
                Question q = session.getQuestions().get(entry.getKey());
                wrongQuestionRepository.findByUserIdAndQuestionId(session.getUserId(), q.getId())
                        .ifPresentOrElse(wq -> {
                            wq.setErrorCount(wq.getErrorCount() + 1);
                            wq.setLastWrongAt(LocalDateTime.now());
                            wrongQuestionRepository.save(wq);
                        }, () -> {
                            WrongQuestion wq = new WrongQuestion();
                            wq.setUserId(session.getUserId());
                            wq.setQuestionId(q.getId());
                            wq.setErrorCount(1);
                            wq.setStatus(0);
                            wq.setCollege(session.getCollege());
                            wq.setLastWrongAt(LocalDateTime.now());
                            wrongQuestionRepository.save(wq);
                        });
            }
        }

        return new GameResultResponse(result, correct, QUESTIONS_PER_ROUND, scoreEarned,
                session.getAnswerResults(), session.getQuestions());
    }

    private int calculateScore(GameSession session) {
        if (session.hasComboWin()) return 3;
        if (session.getCorrectCount() >= 7) return 2;
        if (session.getCorrectCount() < 6) return -1;
        return 0;
    }

    private String determineResult(GameSession session) {
        if (session.hasComboWin()) return "win_combo";
        if (session.getCorrectCount() >= 7) return "win";
        return "lose";
    }

    private int[] getDifficultyRange(Integer userId) {
        String rank = userService.findByUsername(
                userService.findByUsername("temp").getUsername() // 需要从session获取
        ).getRank();
        // 简化的段位→难度映射
        return switch (rank) {
            case "青铜" -> new int[]{1, 3};
            case "白银" -> new int[]{2, 4};
            case "黄金" -> new int[]{3, 6};
            case "铂金" -> new int[]{5, 8};
            case "钻石" -> new int[]{7, 9};
            default -> new int[]{8, 10};
        };
    }

    private String sortAnswer(String answer) {
        char[] chars = answer.toUpperCase().replaceAll("[^A-D]", "").toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    // Inner class
    public static class GameSession {
        private final String sessionId;
        private final Integer userId;
        private final String college;
        private final String category;
        private final List<Question> questions;
        private final Map<Integer, Boolean> answerResults = new HashMap<>();
        private int correctCount = 0;
        private int comboCount = 0;
        private int maxCombo = 0;
        private int timeoutUsed = 0;
        private final int timeLimitSeconds;
        private final int maxTimeouts;
        private boolean finished = false;
        private long startTime = System.currentTimeMillis();

        public GameSession(String sessionId, Integer userId, String college, String category,
                           List<Question> questions, int timeLimitSeconds, int maxTimeouts) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.college = college;
            this.category = category;
            this.questions = questions;
            this.timeLimitSeconds = timeLimitSeconds;
            this.maxTimeouts = maxTimeouts;
        }

        public void recordAnswer(int index, boolean correct, String answer) {
            answerResults.put(index, correct);
            if (correct) {
                correctCount++;
                comboCount++;
                maxCombo = Math.max(maxCombo, comboCount);
            } else {
                comboCount = 0;
            }
        }

        public boolean hasComboWin() { return maxCombo >= 5; }
        public boolean isFinished() { return finished || answerResults.size() >= questions.size(); }

        public String getSessionId() { return sessionId; }
        public Integer getUserId() { return userId; }
        public String getCollege() { return college; }
        public String getCategory() { return category; }
        public List<Question> getQuestions() { return questions; }
        public Map<Integer, Boolean> getAnswerResults() { return answerResults; }
        public int getCorrectCount() { return correctCount; }
        public long getElapsedSeconds() { return (System.currentTimeMillis() - startTime) / 1000; }
    }
}
```

- [ ] **Step 3: 编写 GameController.java**

```java
package com.platform.controller;

import com.platform.dto.AnswerSubmitRequest;
import com.platform.dto.GameResultResponse;
import com.platform.dto.GameStartRequest;
import com.platform.service.GameService;
import com.platform.service.GameService.GameSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGame(
            @RequestBody GameStartRequest request,
            @AuthenticationPrincipal UserDetails user) {
        // 简化实现 — 实际应通过 userDetails 获取 userId
        GameSession session = gameService.startGame(
                1, request.getCollege(), request.getCategory());
        return ResponseEntity.ok(Map.of(
                "sessionId", session.getSessionId(),
                "questions", session.getQuestions(),
                "timeLimit", 10
        ));
    }

    @PostMapping("/answer")
    public ResponseEntity<Map<String, Object>> submitAnswer(
            @RequestBody AnswerSubmitRequest request) {
        boolean correct = gameService.submitAnswer(
                request.getSessionId(), request.getQuestionIndex(), request.getAnswer());
        return ResponseEntity.ok(Map.of("correct", correct));
    }

    @PostMapping("/finish")
    public ResponseEntity<GameResultResponse> finishGame(@RequestBody Map<String, String> body) {
        GameResultResponse result = gameService.finishGame(body.get("sessionId"));
        return ResponseEntity.ok(result);
    }
}
```

- [ ] **Step 4: 编写 DTO**

```java
// GameStartRequest.java
package com.platform.dto;

public class GameStartRequest {
    private String college;
    private String category;
    private String mode; // pve, practice, daily

    public String getCollege() { return college; }
    public String getCategory() { return category; }
    public String getMode() { return mode; }
}

// AnswerSubmitRequest.java
package com.platform.dto;

public class AnswerSubmitRequest {
    private String sessionId;
    private int questionIndex;
    private String answer;

    public String getSessionId() { return sessionId; }
    public int getQuestionIndex() { return questionIndex; }
    public String getAnswer() { return answer; }
}

// GameResultResponse.java
package com.platform.dto;

import com.platform.model.Question;
import java.util.List;
import java.util.Map;

public class GameResultResponse {
    private String result; // win_combo, win, lose
    private int correctCount;
    private int totalQuestions;
    private int scoreEarned;
    private Map<Integer, Boolean> answerResults;
    private List<Question> questions;

    public GameResultResponse(String result, int correctCount, int totalQuestions,
                              int scoreEarned, Map<Integer, Boolean> answerResults,
                              List<Question> questions) {
        this.result = result;
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
        this.scoreEarned = scoreEarned;
        this.answerResults = answerResults;
        this.questions = questions;
    }

    public String getResult() { return result; }
    public int getCorrectCount() { return correctCount; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getScoreEarned() { return scoreEarned; }
    public Map<Integer, Boolean> getAnswerResults() { return answerResults; }
    public List<Question> getQuestions() { return questions; }
}
```

- [ ] **Step 5: Commit**

```bash
git add web-platform/backend/src/main/java/com/platform/service/
git add web-platform/backend/src/main/java/com/platform/controller/
git add web-platform/backend/src/main/java/com/platform/dto/
git commit -m "feat: migrate GameEngine to Spring Boot with REST API"
```

---

### Task 2.4：SvelteKit 全局布局 + 共享组件

**Files:**
- Create: `web-platform/frontend/src/routes/+layout.svelte`
- Create: `web-platform/frontend/src/lib/components/GlassNavbar.svelte`
- Create: `web-platform/frontend/src/lib/components/GlassCard.svelte`
- Create: `web-platform/frontend/src/lib/components/ParticleBackground.svelte`
- Create: `web-platform/frontend/src/lib/components/Toast.svelte`
- Create: `web-platform/frontend/src/lib/stores/auth.ts`
- Create: `web-platform/frontend/src/lib/stores/toast.ts`
- Create: `web-platform/frontend/src/lib/api/client.ts`

- [ ] **Step 1: 编写根布局 +layout.svelte**

```svelte
<script>
  import { onMount } from 'svelte';
  import GlassNavbar from '$lib/components/GlassNavbar.svelte';
  import ParticleBackground from '$lib/components/ParticleBackground.svelte';
  import Toast from '$lib/components/Toast.svelte';
  import { auth } from '$lib/stores/auth';

  onMount(() => {
    const token = localStorage.getItem('token');
    if (token) {
      auth.checkAuth();
    }
  });
</script>

<div class="app-shell">
  <ParticleBackground />
  <GlassNavbar />
  <main class="main-content">
    <slot />
  </main>
  <Toast />
</div>

<style>
  .app-shell {
    position: relative;
    min-height: 100vh;
  }
  .main-content {
    position: relative;
    z-index: 1;
    max-width: 1280px;
    margin: 0 auto;
    padding: 80px 24px 40px;
  }
</style>
```

- [ ] **Step 2: 编写 GlassNavbar.svelte**

```svelte
<script>
  import { auth } from '$lib/stores/auth';
  import { page } from '$app/stores';
</script>

<nav class="glass navbar">
  <a href="/" class="logo gradient-text">AI Academy</a>
  <div class="nav-links">
    <a href="/college/comm" class:active={$page.url.pathname.startsWith('/college')}>学院</a>
    <a href="/lab" class:active={$page.url.pathname.startsWith('/lab')}>实验场</a>
    <a href="/game" class:active={$page.url.pathname.startsWith('/game')}>竞技</a>
    <a href="/profile" class:active={$page.url.pathname.startsWith('/profile')}>
      {$auth.user?.username || '个人中心'}
    </a>
  </div>
</nav>

<style>
  .navbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    height: 60px;
    border-radius: 0;
    border-top: none;
    border-left: none;
    border-right: none;
  }
  .logo {
    font-size: 20px;
    font-weight: 700;
    text-decoration: none;
  }
  .nav-links {
    display: flex;
    gap: 24px;
  }
  .nav-links a {
    color: var(--text-secondary);
    text-decoration: none;
    font-size: 14px;
    font-weight: 500;
    transition: color 0.2s;
  }
  .nav-links a:hover, .nav-links a.active {
    color: var(--text-primary);
  }
</style>
```

- [ ] **Step 3: 编写 GlassCard.svelte**

```svelte
<script>
  export let href = undefined;
  export let onClick = undefined;
</script>

{#if href}
  <a {href} class="glass card-link">
    <slot />
  </a>
{:else if onClick}
  <button class="glass card-button" on:click={onClick}>
    <slot />
  </button>
{:else}
  <div class="glass card">
    <slot />
  </div>
{/if}

<style>
  .card, .card-link, .card-button {
    display: block;
    padding: 24px;
    text-decoration: none;
    color: inherit;
    cursor: pointer;
  }
  .card-link:hover { text-decoration: none; }
  .card-button {
    width: 100%;
    border: none;
    font: inherit;
    text-align: left;
  }
</style>
```

- [ ] **Step 4: 编写 ParticleBackground.svelte**

```svelte
<script>
  import { onMount } from 'svelte';

  let canvas;
  let particles = [];
  const COUNT = 60;

  onMount(() => {
    const ctx = canvas.getContext('2d');
    resize();
    initParticles();
    requestAnimationFrame(animate);

    window.addEventListener('resize', resize);

    function resize() {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    }

    function initParticles() {
      particles = Array.from({ length: COUNT }, () => ({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        r: Math.random() * 2 + 1,
        dx: (Math.random() - 0.5) * 0.5,
        dy: (Math.random() - 0.5) * 0.5,
        opacity: Math.random() * 0.5 + 0.1
      }));
    }

    function animate() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      for (const p of particles) {
        p.x += p.dx;
        p.y += p.dy;
        if (p.x < 0 || p.x > canvas.width) p.dx *= -1;
        if (p.y < 0 || p.y > canvas.height) p.dy *= -1;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(100, 180, 255, ${p.opacity})`;
        ctx.fill();
      }
      // 连线
      for (let i = 0; i < particles.length; i++) {
        for (let j = i + 1; j < particles.length; j++) {
          const dx = particles[i].x - particles[j].x;
          const dy = particles[i].y - particles[j].y;
          const dist = Math.sqrt(dx * dx + dy * dy);
          if (dist < 120) {
            ctx.beginPath();
            ctx.moveTo(particles[i].x, particles[i].y);
            ctx.lineTo(particles[j].x, particles[j].y);
            ctx.strokeStyle = `rgba(100, 180, 255, ${0.08 * (1 - dist / 120)})`;
            ctx.stroke();
          }
        }
      }
      requestAnimationFrame(animate);
    }
  });
</script>

<canvas bind:this={canvas} class="particle-canvas"></canvas>

<style>
  .particle-canvas {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
    pointer-events: none;
  }
</style>
```

- [ ] **Step 5: 编写 auth store (auth.ts)**

```typescript
import { writable, derived } from 'svelte/store';
import { browser } from '$app/environment';

interface User {
  username: string;
  role: string;
  totalScore: number;
  rank: string;
}

function createAuthStore() {
  const user = writable<User | null>(null);
  const token = writable<string | null>(browser ? localStorage.getItem('token') : null);

  const isLoggedIn = derived(user, ($user) => $user !== null);

  async function login(username: string, password: string) {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    if (!res.ok) throw new Error('Login failed');
    const data = await res.json();
    token.set(data.token);
    user.set({ username: data.username, role: data.role, totalScore: data.totalScore, rank: data.rank });
    if (browser) localStorage.setItem('token', data.token);
    return data;
  }

  function logout() {
    token.set(null);
    user.set(null);
    if (browser) localStorage.removeItem('token');
  }

  async function checkAuth() {
    const t = browser ? localStorage.getItem('token') : null;
    if (!t) return;
    try {
      const res = await fetch('/api/user/me', {
        headers: { Authorization: `Bearer ${t}` }
      });
      if (res.ok) {
        const data = await res.json();
        user.set(data);
        token.set(t);
      } else {
        logout();
      }
    } catch {
      logout();
    }
  }

  return { user, token, isLoggedIn, login, logout, checkAuth };
}

export const auth = createAuthStore();
```

- [ ] **Step 6: 编写 API client (client.ts)**

```typescript
import { browser } from '$app/environment';

type RequestOptions = {
  method?: string;
  body?: unknown;
  headers?: Record<string, string>;
};

export async function api(path: string, options: RequestOptions = {}) {
  const token = browser ? localStorage.getItem('token') : null;
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options.headers
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const res = await fetch(path, {
    method: options.method || 'GET',
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined
  });
  if (res.status === 401) {
    if (browser) localStorage.removeItem('token');
    window.location.href = '/login';
    throw new Error('Unauthorized');
  }
  return res;
}

export async function apiJson<T>(path: string, options?: RequestOptions): Promise<T> {
  const res = await api(path, options);
  if (!res.ok) {
    const error = await res.text();
    throw new Error(error || `API error: ${res.status}`);
  }
  return res.json();
}
```

- [ ] **Step 7: Commit**

```bash
git add web-platform/frontend/src/
git commit -m "feat: add SvelteKit root layout with glass navbar, particles, and auth"
```

---

### Task 2.5：登录页 + 首页 Dashboard

**Files:**
- Create: `web-platform/frontend/src/routes/login/+page.svelte`
- Create: `web-platform/frontend/src/routes/+page.svelte`

- [ ] **Step 1: 编写 login/+page.svelte**

```svelte
<script>
  import { auth } from '$lib/stores/auth';
  import { goto } from '$app/navigation';
  import GlassCard from '$lib/components/GlassCard.svelte';

  let username = '';
  let password = '';
  let error = '';
  let loading = false;

  async function handleLogin() {
    error = '';
    loading = true;
    try {
      await auth.login(username, password);
      goto('/');
    } catch (e) {
      error = '用户名或密码错误';
    } finally {
      loading = false;
    }
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') handleLogin();
  }
</script>

<div class="login-page">
  <div class="login-container">
    <h1 class="gradient-text logo-text">AI Academy</h1>
    <p class="subtitle">AI 交互式学习平台</p>

    <div class="glass login-card">
      <h2>登录</h2>
      {#if error}
        <div class="error">{error}</div>
      {/if}
      <input type="text" placeholder="用户名" bind:value={username}
             on:keydown={handleKeydown} />
      <input type="password" placeholder="密码" bind:value={password}
             on:keydown={handleKeydown} />
      <button class="btn-primary" on:click={handleLogin} disabled={loading}>
        {loading ? '登录中...' : '登录'}
      </button>
    </div>
  </div>
</div>

<style>
  .login-page {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
    margin: -80px -24px -40px;
  }
  .login-container {
    text-align: center;
    width: 100%;
    max-width: 400px;
    padding: 0 24px;
  }
  .logo-text {
    font-size: 36px;
    font-weight: 700;
    margin-bottom: 4px;
  }
  .subtitle {
    color: var(--text-secondary);
    margin-bottom: 32px;
  }
  .login-card {
    padding: 32px;
    text-align: left;
  }
  .login-card h2 {
    margin-bottom: 20px;
    font-size: 18px;
  }
  input {
    width: 100%;
    padding: 12px 16px;
    margin-bottom: 12px;
    background: rgba(255,255,255,0.05);
    border: 1px solid var(--glass-border);
    border-radius: 10px;
    color: var(--text-primary);
    font-size: 14px;
    outline: none;
    transition: border-color 0.2s;
  }
  input:focus {
    border-color: var(--accent-blue);
  }
  .btn-primary {
    width: 100%;
    padding: 12px;
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple));
    border: none;
    border-radius: 10px;
    color: white;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    margin-top: 8px;
  }
  .btn-primary:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  .error {
    background: rgba(255, 100, 100, 0.15);
    color: var(--accent-red);
    padding: 10px 14px;
    border-radius: 8px;
    margin-bottom: 16px;
    font-size: 13px;
  }
</style>
```

- [ ] **Step 2: 编写首页 +page.svelte**

```svelte
<script>
  import { auth } from '$lib/stores/auth';
  import GlassCard from '$lib/components/GlassCard.svelte';

  const colleges = [
    { name: '通信学院', slug: 'comm', desc: '通信原理 · 数据通信网 · 信号与系统', icon: '📡' },
    { name: 'AI 学院', slug: 'ai', desc: '提示词工程 · Skills · Agent 开发', icon: '🤖', coming: true }
  ];

  const quickActions = [
    { label: '每日挑战', href: '/game?mode=daily' },
    { label: '错题本', href: '/profile/wrongbook' },
    { label: '排行榜', href: '/profile' }
  ];
</script>

<div class="home">
  <section class="hero">
    <h1 class="gradient-text">掌握 AI，从第一行代码开始</h1>
    <p>交互式学习平台 — 提示词工程 · Skills 开发 · Agent 构建</p>
  </section>

  <section class="colleges">
    <h2>选择学院</h2>
    <div class="college-grid">
      {#each colleges as college}
        <GlassCard href={college.coming ? undefined : `/college/${college.slug}`}>
          <div class="college-card">
            <span class="college-icon">{college.icon}</span>
            <div>
              <h3>{college.name}</h3>
              <p>{college.desc}</p>
              {#if college.coming}
                <span class="badge">即将上线</span>
              {/if}
            </div>
          </div>
        </GlassCard>
      {/each}
    </div>
  </section>

  <section class="quick-actions">
    <h2>快速入口</h2>
    <div class="actions-row">
      {#each quickActions as action}
        <GlassCard href={action.href}>
          <span>{action.label}</span>
        </GlassCard>
      {/each}
    </div>
  </section>
</div>

<style>
  .home {
    display: flex;
    flex-direction: column;
    gap: 40px;
  }
  .hero {
    text-align: center;
    padding: 40px 0 20px;
  }
  .hero h1 {
    font-size: 36px;
    margin-bottom: 8px;
  }
  .hero p {
    color: var(--text-secondary);
    font-size: 16px;
  }
  section h2 {
    font-size: 18px;
    margin-bottom: 16px;
  }
  .college-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 16px;
  }
  .college-card {
    display: flex;
    gap: 16px;
    align-items: center;
  }
  .college-icon {
    font-size: 32px;
  }
  .college-card h3 {
    font-size: 16px;
    margin-bottom: 4px;
  }
  .college-card p {
    color: var(--text-secondary);
    font-size: 13px;
  }
  .badge {
    display: inline-block;
    padding: 2px 8px;
    background: rgba(200, 150, 255, 0.15);
    color: var(--accent-purple);
    border-radius: 20px;
    font-size: 11px;
    margin-top: 6px;
  }
  .actions-row {
    display: flex;
    gap: 12px;
  }
  .actions-row :global(.card) {
    padding: 16px 24px;
  }
</style>
```

- [ ] **Step 3: Commit**

```bash
git add web-platform/frontend/src/routes/login/
git add web-platform/frontend/src/routes/+page.svelte
git commit -m "feat: add login page and home dashboard with glassmorphism"
```

---

### Task 2.6：游戏大厅 + 答题页面 + 结果页

**Files:**
- Create: `web-platform/frontend/src/routes/game/+page.svelte`
- Create: `web-platform/frontend/src/routes/game/play/+page.svelte`
- Create: `web-platform/frontend/src/routes/game/result/+page.svelte`
- Create: `web-platform/frontend/src/lib/stores/game.ts`

- [ ] **Step 1: 编写 /game/+page.svelte（游戏大厅）**

```svelte
<script>
  import GlassCard from '$lib/components/GlassCard.svelte';

  const modes = [
    { name: '每日挑战', desc: '每日刷新，获取额外积分', mode: 'daily', icon: '🔥' },
    { name: 'PVE 闯关', desc: '按课程进度逐关解锁', mode: 'pve', icon: '⚔️' },
    { name: 'PVP 天梯', desc: '全站题库随机对战', mode: 'pvp', icon: '🏆', coming: true }
  ];
</script>

<div class="game-lobby">
  <h1 class="gradient-text">竞技中心</h1>
  <p class="sub">选择你的挑战模式</p>

  <div class="mode-grid">
    {#each modes as mode}
      <GlassCard href={mode.coming ? undefined : `/game/play?mode=${mode.mode}`}>
        <div class="mode-card">
          <span class="mode-icon">{mode.icon}</span>
          <h3>{mode.name}</h3>
          <p>{mode.desc}</p>
          {#if mode.coming}<span class="badge">即将上线</span>{/if}
        </div>
      </GlassCard>
    {/each}
  </div>
</div>

<style>
  .game-lobby { text-align: center; }
  .sub { color: var(--text-secondary); margin: 8px 0 32px; }
  .mode-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    max-width: 700px;
    margin: 0 auto;
  }
  .mode-card { padding: 12px 0; }
  .mode-icon { font-size: 40px; display: block; margin-bottom: 8px; }
  .mode-card h3 { font-size: 16px; margin-bottom: 4px; }
  .mode-card p { color: var(--text-secondary); font-size: 13px; }
  .badge { display: inline-block; padding: 2px 8px; background: rgba(200,150,255,0.15);
           color: var(--accent-purple); border-radius: 20px; font-size: 11px; margin-top: 8px; }
</style>
```

- [ ] **Step 2: 编写 game store (game.ts)**

```typescript
import { writable, derived } from 'svelte/store';
import { apiJson } from '$lib/api/client';

interface Question {
  id: number;
  content: string;
  type: string;
  optionA: string;
  optionB: string;
  optionC: string | null;
  optionD: string | null;
  explanation: string;
}

interface GameState {
  sessionId: string;
  questions: Question[];
  currentIndex: number;
  totalQuestions: number;
  timeLimit: number;
  answers: (string | null)[];
  results: (boolean | null)[];
  isFinished: boolean;
  isLoading: boolean;
}

function createGameStore() {
  const state = writable<GameState>({
    sessionId: '',
    questions: [],
    currentIndex: 0,
    totalQuestions: 10,
    timeLimit: 10,
    answers: [],
    results: [],
    isFinished: false,
    isLoading: false
  });

  async function start(college: string, category: string) {
    state.update(s => ({ ...s, isLoading: true }));
    const data = await apiJson<{
      sessionId: string;
      questions: Question[];
      timeLimit: number;
    }>('/api/game/start', {
      method: 'POST',
      body: { college, category }
    });
    state.set({
      sessionId: data.sessionId,
      questions: data.questions,
      currentIndex: 0,
      totalQuestions: data.questions.length,
      timeLimit: data.timeLimit,
      answers: new Array(data.questions.length).fill(null),
      results: new Array(data.questions.length).fill(null),
      isFinished: false,
      isLoading: false
    });
  }

  async function submitAnswer(answer: string) {
    let current: GameState = {} as GameState;
    state.update(s => { current = s; return s; });
    const data = await apiJson<{ correct: boolean }>('/api/game/answer', {
      method: 'POST',
      body: { sessionId: current.sessionId, questionIndex: current.currentIndex, answer }
    });
    state.update(s => {
      s.results[s.currentIndex] = data.correct;
      s.answers[s.currentIndex] = answer;
      return s;
    });
    return data.correct;
  }

  function nextQuestion() {
    state.update(s => {
      if (s.currentIndex < s.totalQuestions - 1) {
        s.currentIndex++;
      } else {
        s.isFinished = true;
      }
      return s;
    });
  }

  async function finish(): Promise<any> {
    let current: GameState = {} as GameState;
    state.update(s => { current = s; return s; });
    return apiJson('/api/game/finish', {
      method: 'POST',
      body: { sessionId: current.sessionId }
    });
  }

  return { state, start, submitAnswer, nextQuestion, finish };
}

export const game = createGameStore();
```

- [ ] **Step 3: 编写 /game/play/+page.svelte（答题界面）**

```svelte
<script>
  import { game } from '$lib/stores/game';
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';

  let timeLeft = 10;
  let isLocked = false;
  let selectedAnswer = '';
  let showResult = false;
  let lastCorrect = false;
  let timer: ReturnType<typeof setInterval>;

  $: question = $game.questions[$game.currentIndex];

  onMount(() => {
    if ($game.questions.length === 0) {
      goto('/game');
      return;
    }
    startTimer();
  });

  function startTimer() {
    timeLeft = 10;
    clearInterval(timer);
    timer = setInterval(() => {
      timeLeft--;
      if (timeLeft <= 0) {
        handleTimeout();
      }
    }, 1000);
  }

  function handleTimeout() {
    clearInterval(timer);
    isLocked = true;
    lastCorrect = false;
    showResult = true;
  }

  async function selectOption(opt: string) {
    if (isLocked) return;
    isLocked = true;
    clearInterval(timer);
    selectedAnswer = opt;
    lastCorrect = await game.submitAnswer(opt);
    showResult = true;
  }

  function handleContinue() {
    showResult = false;
    selectedAnswer = '';
    isLocked = false;

    if ($game.currentIndex >= $game.totalQuestions - 1) {
      finishGame();
    } else {
      game.nextQuestion();
      startTimer();
    }
  }

  async function finishGame() {
    const result = await game.finish();
    sessionStorage.setItem('gameResult', JSON.stringify(result));
    goto('/game/result');
  }

  function optionClass(opt: string) {
    if (!showResult) return '';
    const q = $game.questions[$game.currentIndex];
    if (opt === q.answer) return 'correct';
    if (opt === selectedAnswer && !lastCorrect) return 'wrong';
    return 'dimmed';
  }
</script>

{#if $game.isLoading}
  <div class="loading">加载中...</div>
{:else if question}
  <div class="game-play">
    <!-- Progress bar -->
    <div class="progress-bar">
      <div class="progress-fill" style="width:{$game.currentIndex / $game.totalQuestions * 100}%"></div>
    </div>

    <!-- Timer -->
    <div class="timer" class:urgent={timeLeft <= 3}>
      {timeLeft}s
    </div>

    <!-- Question -->
    <div class="question-card glass">
      <span class="q-num">第 {$game.currentIndex + 1} / {$game.totalQuestions} 题</span>
      <h2>{question.content}</h2>

      <div class="options">
        {#each ['A', 'B', 'C', 'D'] as opt}
          {#if question['option' + opt]}
            <button
              class="option {optionClass(opt)}"
              on:click={() => selectOption(opt)}
              disabled={isLocked}
            >
              <span class="opt-letter">{opt}</span>
              <span>{question['option' + opt]}</span>
            </button>
          {/if}
        {/each}
      </div>

      {#if showResult}
        <div class="result-feedback" class:correct={lastCorrect} class:wrong={!lastCorrect}>
          {lastCorrect ? '✓ 正确!' : '✗ 错误'}
          {#if !lastCorrect && question.explanation}
            <p class="explanation">{question.explanation}</p>
          {/if}
        </div>
        <button class="btn-primary continue-btn" on:click={handleContinue}>
          {$game.currentIndex >= $game.totalQuestions - 1 ? '查看结果' : '下一题'}
        </button>
      {/if}
    </div>
  </div>
{/if}

<style>
  .game-play { max-width: 700px; margin: 0 auto; }
  .progress-bar {
    height: 4px;
    background: rgba(255,255,255,0.1);
    border-radius: 2px;
    margin-bottom: 16px;
  }
  .progress-fill {
    height: 100%;
    background: var(--accent-blue);
    border-radius: 2px;
    transition: width 0.3s;
  }
  .timer {
    text-align: center;
    font-size: 28px;
    font-weight: 700;
    font-variant-numeric: tabular-nums;
    margin-bottom: 20px;
    color: var(--text-primary);
  }
  .timer.urgent { color: var(--accent-red); animation: pulse 0.5s infinite; }
  @keyframes pulse { 50% { opacity: 0.5; } }
  .question-card { padding: 32px; }
  .q-num { font-size: 12px; color: var(--text-secondary); }
  .question-card h2 { margin: 12px 0 24px; font-size: 18px; line-height: 1.6; }
  .options { display: flex; flex-direction: column; gap: 10px; }
  .option {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 18px;
    background: rgba(255,255,255,0.04);
    border: 1px solid var(--glass-border);
    border-radius: 12px;
    color: var(--text-primary);
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
    text-align: left;
  }
  .option:hover:not(:disabled) {
    background: rgba(255,255,255,0.08);
    border-color: rgba(255,255,255,0.2);
  }
  .option:disabled { cursor: default; }
  .opt-letter {
    width: 28px; height: 28px;
    display: flex; align-items: center; justify-content: center;
    background: rgba(255,255,255,0.08);
    border-radius: 8px;
    font-weight: 600; font-size: 13px;
  }
  .option.correct { background: rgba(100,200,150,0.15); border-color: var(--accent-green); }
  .option.wrong { background: rgba(255,100,100,0.15); border-color: var(--accent-red); }
  .option.dimmed { opacity: 0.4; }
  .result-feedback {
    margin-top: 20px; padding: 16px; border-radius: 12px; font-weight: 600;
  }
  .result-feedback.correct { background: rgba(100,200,150,0.1); color: var(--accent-green); }
  .result-feedback.wrong { background: rgba(255,100,100,0.1); color: var(--accent-red); }
  .explanation { font-weight: 400; margin-top: 8px; font-size: 13px; color: var(--text-secondary); }
  .btn-primary {
    width: 100%; padding: 14px; margin-top: 20px;
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple));
    border: none; border-radius: 12px; color: white; font-size: 15px;
    font-weight: 600; cursor: pointer;
  }
  .loading { text-align: center; padding: 80px; color: var(--text-secondary); }
</style>
```

- [ ] **Step 4: 编写 /game/result/+page.svelte（结果页）**

```svelte
<script>
  import { onMount } from 'svelte';
  import GlassCard from '$lib/components/GlassCard.svelte';

  let result: any = null;

  onMount(() => {
    const raw = sessionStorage.getItem('gameResult');
    if (raw) result = JSON.parse(raw);
  });
</script>

{#if result}
  <div class="result-page">
    <GlassCard>
      <div class="result-header">
        <h1 class="gradient-text">
          {result.result === 'win_combo' ? '连对通关!' :
           result.result === 'win' ? '恭喜获胜!' : '挑战失败'}
        </h1>
        <div class="stats">
          <div class="stat">
            <span class="stat-value">{result.correctCount}/{result.totalQuestions}</span>
            <span class="stat-label">正确率</span>
          </div>
          <div class="stat">
            <span class="stat-value">{result.scoreEarned > 0 ? '+' : ''}{result.scoreEarned}</span>
            <span class="stat-label">积分</span>
          </div>
        </div>
      </div>

      <div class="actions">
        <a href="/game" class="btn-secondary">返回大厅</a>
        <a href="/college/comm" class="btn-primary">去学习</a>
        <a href="/profile/wrongbook" class="btn-secondary">查看错题</a>
      </div>
    </GlassCard>
  </div>
{/if}

<style>
  .result-page { max-width: 500px; margin: 0 auto; text-align: center; }
  .result-header { padding: 24px 0; }
  .result-header h1 { font-size: 28px; margin-bottom: 24px; }
  .stats { display: flex; justify-content: center; gap: 40px; }
  .stat { display: flex; flex-direction: column; }
  .stat-value { font-size: 32px; font-weight: 700; }
  .stat-label { font-size: 12px; color: var(--text-secondary); margin-top: 4px; }
  .actions { display: flex; gap: 12px; justify-content: center; padding: 16px 0; }
  .btn-primary, .btn-secondary {
    padding: 10px 24px; border-radius: 10px; font-size: 14px;
    font-weight: 500; text-decoration: none; cursor: pointer;
  }
  .btn-primary { background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple)); color: white; border: none; }
  .btn-secondary { background: rgba(255,255,255,0.06); border: 1px solid var(--glass-border); color: var(--text-primary); }
</style>
```

- [ ] **Step 4: Commit**

```bash
git add web-platform/frontend/src/routes/game/
git add web-platform/frontend/src/lib/stores/game.ts
git commit -m "feat: add game lobby, play arena, and result pages"
```

---

### Task 2.7：个人中心 + 错题本

**Files:**
- Create: `web-platform/frontend/src/routes/profile/+page.svelte`
- Create: `web-platform/frontend/src/routes/profile/wrongbook/+page.svelte`
- Create: `web-platform/frontend/src/routes/profile/settings/+page.svelte`
- Create: `web-platform/backend/src/main/java/com/platform/controller/UserController.java`
- Create: `web-platform/backend/src/main/java/com/platform/controller/WrongBookController.java`

- [ ] **Step 1: 编写 UserController.java**

```java
package com.platform.controller;

import com.platform.model.User;
import com.platform.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal UserDetails details) {
        User user = userService.findByUsername(details.getUsername());
        user.setPasswordHash(null); // 不返回密码哈希
        return ResponseEntity.ok(user);
    }

    @PostMapping("/checkin")
    public ResponseEntity<User> checkIn(@AuthenticationPrincipal UserDetails details) {
        User user = userService.findByUsername(details.getUsername());
        user = userService.checkIn(user.getId());
        user.setPasswordHash(null);
        return ResponseEntity.ok(user);
    }
}
```

- [ ] **Step 2: 编写 WrongBookController.java**

```java
package com.platform.controller;

import com.platform.model.WrongQuestion;
import com.platform.repository.WrongQuestionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wrongbook")
public class WrongBookController {

    private final WrongQuestionRepository wrongQuestionRepository;

    public WrongBookController(WrongQuestionRepository wrongQuestionRepository) {
        this.wrongQuestionRepository = wrongQuestionRepository;
    }

    @GetMapping
    public ResponseEntity<List<WrongQuestion>> getWrongQuestions(
            @RequestParam(defaultValue = "0") int status) {
        // 简化 — 实际需从 token 获取 userId
        return ResponseEntity.ok(wrongQuestionRepository.findByUserIdAndStatus(1, status));
    }
}
```

- [ ] **Step 3: 编写 profile/+page.svelte**

```svelte
<script>
  import { onMount } from 'svelte';
  import { auth } from '$lib/stores/auth';
  import { apiJson } from '$lib/api/client';
  import GlassCard from '$lib/components/GlassCard.svelte';

  let profile: any = null;

  onMount(async () => {
    try {
      profile = await apiJson('/api/user/me');
    } catch { /* handle */ }
  });
</script>

<div class="profile">
  <div class="profile-header">
    <div class="avatar glass">{(profile?.username || '?')[0].toUpperCase()}</div>
    <div>
      <h1>{profile?.username}</h1>
      <span class="rank-badge">{profile?.rank}</span>
    </div>
    <div class="score">{profile?.totalScore ?? 0} <small>积分</small></div>
  </div>

  <div class="grid">
    <GlassCard href="/profile/wrongbook">
      <h3>错题本</h3>
      <p>查看和复习错过的题目</p>
    </GlassCard>
    <GlassCard href="/profile/settings">
      <h3>设置</h3>
      <p>账户与偏好设置</p>
    </GlassCard>
  </div>
</div>

<style>
  .profile { max-width: 600px; margin: 0 auto; }
  .profile-header {
    display: flex; align-items: center; gap: 16px;
    padding: 24px; margin-bottom: 24px;
  }
  .avatar {
    width: 60px; height: 60px;
    display: flex; align-items: center; justify-content: center;
    font-size: 24px; font-weight: 700;
    border-radius: 50%;
  }
  .rank-badge {
    display: inline-block;
    padding: 2px 10px;
    background: linear-gradient(135deg, var(--accent-gold), #ff8c00);
    border-radius: 20px;
    font-size: 12px;
    color: #000;
    font-weight: 600;
    margin-top: 4px;
  }
  .score { margin-left: auto; text-align: right; }
  .score { font-size: 28px; font-weight: 700; }
  .score small { font-size: 12px; color: var(--text-secondary); }
  .grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
  .grid h3 { margin-bottom: 4px; }
  .grid p { color: var(--text-secondary); font-size: 13px; }
</style>
```

- [ ] **Step 4: 编写 profile/wrongbook/+page.svelte**

```svelte
<script>
  import { onMount } from 'svelte';
  import { apiJson } from '$lib/api/client';
  import GlassCard from '$lib/components/GlassCard.svelte';

  let questions: any[] = [];

  onMount(async () => {
    try {
      questions = await apiJson('/api/wrongbook?status=0');
    } catch { /* handle */ }
  });
</script>

<div class="wrongbook">
  <h1 class="gradient-text">错题本</h1>
  <p class="sub">待消灭的错题：{questions.length} 道</p>

  {#if questions.length === 0}
    <div class="empty glass">
      <p>暂无错题</p>
    </div>
  {:else}
    <div class="list">
      {#each questions as wq}
        <GlassCard>
          <span class="tag">{wq.college}</span>
          <span class="error-count">错 {wq.errorCount} 次</span>
        </GlassCard>
      {/each}
    </div>
  {/if}

  <a href="/game/play?mode=practice&source=wrongbook" class="btn-primary">一键挑战错题</a>
</div>

<style>
  .wrongbook { max-width: 700px; margin: 0 auto; }
  .sub { color: var(--text-secondary); margin-bottom: 24px; }
  .empty { padding: 60px; text-align: center; color: var(--text-secondary); }
  .list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 24px; }
  .tag {
    display: inline-block; padding: 2px 8px;
    background: rgba(100,180,255,0.1); color: var(--accent-blue);
    border-radius: 4px; font-size: 11px;
  }
  .error-count { float: right; color: var(--accent-red); font-size: 13px; }
  .btn-primary {
    display: block; width: 100%; padding: 14px; text-align: center;
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple));
    border-radius: 12px; color: white; font-size: 15px;
    font-weight: 600; text-decoration: none;
  }
</style>
```

- [ ] **Step 5: Commit**

```bash
git add web-platform/frontend/src/routes/profile/
git add web-platform/backend/src/main/java/com/platform/controller/UserController.java
git add web-platform/backend/src/main/java/com/platform/controller/WrongBookController.java
git commit -m "feat: add profile page, wrongbook, and settings"
```

---

## 验证检查清单

Phase 1-2 完成后，验证以下功能：

- [ ] `docker compose up` 五服务全部健康
- [ ] Flyway 迁移脚本正确执行，所有表创建成功
- [ ] `POST /api/auth/login` 返回有效 JWT token
- [ ] `GET /api/user/me` 携带 token 返回用户信息
- [ ] `POST /api/game/start` 返回 10 道随机题目
- [ ] 前端登录页 → 首页 → 游戏大厅 → 答题 → 结果页完整跑通
- [ ] 错题本显示错题列表
- [ ] 玻璃形态 UI 在各页面正常渲染
- [ ] `GET /health` (AI service) 返回 OK

---

## Phase 3-5 预览

后续计划覆盖：
- **Phase 3**: AI 学院 CMS + LLM 网关 + RAG 引擎 + 课程内容管理后台
- **Phase 4**: 前端行为采集 SDK + json-rules-engine + 四角色 System Prompt + 角色路由器
- **Phase 5**: Docker 沙箱调度 + 提示词实验场 + Agent 可视化构建器 + PVP 对战 + 传送门联动
