# AI 交互式学习平台设计文档

## 概述

将现有 Java Swing 知识竞答游戏改造为现代 Web 应用，扩展 AI 知识学习体系。平台以"学-练-赛"闭环为核心，内置 AI 行为监测与多角色助教系统。

**技术栈：** SvelteKit (前端) + Spring Boot (业务后端) + Python/FastAPI (AI 微服务) + MySQL 8.0 + Redis

---

## 一、项目定位

### 1.1 双轨并行

- **AI 学院**：提示词工程 / Skills 开发 / Agent 开发，三大支柱 + L1-L5 能力阶梯 + 项目驱动 + 游戏化闯关
- **通信学院**：保留原 Java Swing 应用的通信原理 & 数据通信网题库，扩展课程体系
- 共享用户体系、积分、段位系统

### 1.2 目标用户

开发者实战型——面向有编程基础的技术人员，以可运行的代码为核心，从入门到生产级。

---

## 二、技术架构

### 2.1 三层分离

```
SvelteKit Frontend (:5173)
    ↕ REST + WebSocket
Spring Boot Backend (:8080)  ──→  MySQL 8.0 + Redis
    ↕ gRPC / HTTP
Python/FastAPI AI Service      ──→  Docker Sandbox
```

- **SvelteKit**：页面路由、AI 行为监测采集层、玻璃形态 UI 组件、动画系统
- **Spring Boot**：用户认证、游戏引擎、课程管理、进度追踪、HikariCP 连接池（复用现有技术栈）
- **Python/FastAPI**：LLM 网关（Claude/DeepSeek/GPT-4o）、RAG 引擎（LangChain/LlamaIndex）、Docker 沙箱调度、行为分析引擎

### 2.2 关键设计决策

| 决策 | 方案 | 理由 |
|------|------|------|
| AI 服务层 | 独立 Python 微服务 | Python AI 生态最完整；独立扩展不影响业务 |
| 代码沙箱 | Docker 服务端 | 真正隔离，支持多语言（Python/JS/Go） |
| 角色路由 | 前端确定性路由 | 页面决定 AI 人格，不额外调 LLM 增加延迟 |
| 规则引擎 | 前端 `json-rules-engine` | 零延迟干预；后端只负责下发 JSON 规则配置 |

---

## 三、页面路由结构

```
/ (首页/智能控制台)
│  状态驱动布局：新用户→学院入口引导；老用户→续播+每日推荐
│  AI 助手常驻微件（Ctrl+K 命令面板 + 右下角悬浮角标）
│
├── /college (学院总览)
│   ├── /ai (AI 学院)
│   │   ├── /prompt-engineering    (L1-L5)
│   │   ├── /skills                (L1-L5)
│   │   ├── /agent                 (L1-L5)
│   │   ├── /projects              (综合实战项目)
│   │   └── /course/[slug]         (统一课程详情与交互学习页)
│   └── /comm (通信学院)
│       ├── /signals-and-systems    (信号与系统)
│       ├── /communication-theory   (通信原理)
│       ├── /data-network           (数据通信网)
│       ├── /advanced-topics        (前沿专题)
│       └── /course/[slug]          (统一课程详情页)
│
├── /lab (实验场中心)
│   ├── /sandbox                   (Docker 代码沙箱 + 网络拓扑仿真)
│   ├── /prompt-playground         (多模型横向评测实验室)
│   └── /agent-builder             (Agent 可视化拖拽构建器 + 一键发布)
│
├── /game (游戏化中心)
│   ├── /lobby                     (游戏大厅：每日挑战/PVE闯关/PVP排位)
│   ├── /play                      (核心答题竞技场)
│   └── /result                    (复盘结算：课程追溯 + 错题一键收录)
│
├── /profile (个人中心)
│   ├── /                          (基本信息 + 技能树可视化 + 资产荣誉)
│   ├── /wrongbook                 (错题分类归档 + 一键重挑战)
│   └── /settings                  (账户设置 + AI 干预频率滑块)
│
└── /admin                         (课程发布 + 题库管理 + 沙箱镜像管理)
```

### 3.1 全局组件（所有页面共享）

- Glass Navbar（毛玻璃导航栏）
- AI Widget（右下角悬浮微件，常驻）
- Behavior Monitor（行为采集脚本）
- Particle Background（粒子背景动画）
- Toast System（通知系统）

### 3.2 模块联动（"传送门"机制）

- 课程页 → 一键跳转实验场（携带 `course_id` + `lesson_id`，自动加载代码模板）
- 实验结果 → 一键发布到游戏（Agent 变成 NPC 擂主）
- 游戏结果页 → 错题追溯回课程页 + 一键加入错题本
- 错题本 → 一键重新挑战（传入 `source=wrongbook` 上下文）

---

## 四、AI 行为监测系统

### 4.1 三层架构

**Layer 1: 采集层（浏览器端）**
- 事件类型：click、scroll depth/velocity、hover dwell、text selection、code editor interactions、page visibility、answer latency、option changes
- 节流：高频事件（mousemove/scroll）throttle 200ms
- 聚合：Web Worker 异步聚合，每 3-5 秒或行为节点触发时批量发送
- 暂离检测：Page Visibility API 区分"暂离"与"犹豫"

**Layer 2: 分析层（AI Service）**
- 快路径（实时）：困惑检测、即时流失预警 → 驱动前端干预
- 慢路径（离线）：学习风格分类、薄弱点推断、长周期心流评估 → 生成学习报告

**Layer 3: 干预层（前端）**
- 干预升级链：Toast → 内联卡片 → AI 侧边栏 → 鼓励动画

### 4.2 规则引擎（前端 json-rules-engine）

```
RULE 陷入僵局:
  WHEN answer_latency > 10s AND option_changes >= 2 AND NOT help_requested
  THEN → 内联卡片："需要看个小提示吗？"

RULE 走马观花:
  WHEN dwell_time < min_read_time AND consecutive_errors >= 2
  THEN → AI 侧边栏：主动梳理本页核心概念

RULE 高歌猛进:
  WHEN accuracy = 100% AND avg_time < baseline_30%
  THEN → 跳级动画 + 提示："当前难度已通关，是否跳级？"

RULE 即将流失:
  WHEN tab_hidden > 60s OR (mouse_exit AND inactivity > 90s)
  THEN → 浏览器标题闪烁提醒
```

### 4.3 隐私设计

- 原始生物行为特征（鼠标轨迹、毫秒时间戳）仅在本地内存中用于规则匹配，**绝不上传服务器**
- 上传到后端的仅为脱敏结论（如 `question_12_hesitation_level: "high"`）
- 行为结论数据存储不超过 30 天
- 用户可在设置中完全关闭行为监测，AI 退化为纯被动模式
- 用户可调节「AI 干预频率」滑块（低/中/高），全局缩放规则阈值

---

## 五、AI 多角色助教系统

### 5.1 四角色定义

| 角色 | 触发场景 | 风格 | 核心能力 |
|------|---------|------|---------|
| 讲解老师 (Lecturer) | 课程页 / "为什么"类提问 | 耐心、深入浅出、多用类比 | RAG 课程资料 + 图解生成 |
| 代码导师 (Code Mentor) | 实验场 / 沙箱 / 代码提交 | 精确、Socratic 反问式 | Docker 沙箱执行 + 逐行审查 |
| 陪练同学 (Study Buddy) | 游戏 / 答题 / 每日挑战 | 鼓励型、竞技感 | 同时答题 + 赛后交流思路 |
| 学习分析师 (Analyst) | 章节完成 / 每周报告 | 数据驱动、建设性 | 行为数据聚合 + 个性化建议 |

### 5.2 角色路由器

- **默认路由**：页面决定人格（`/college`→Lecturer，`/lab`→Code Mentor，`/game`→Study Buddy，`/profile`→Analyst），不调 LLM
- **动态溢出**：仅当用户显式提问偏离当前场景时（前端关键词/Embedding 匹配），临时切换人格
- **兜底机制**：代码导师 Socratic 模式连续 3 次无进展，或用户输入"直接告诉我"，立即切换为讲解模式

### 5.3 跨角色记忆

- 各角色共享 `behavior_events` 和 `learning_reports` 中的薄弱点上下文
- 代码导师加载时，自动注入讲解老师诊断出的知识点薄弱项
- System Prompt 动态拼接：角色基 Prompt + 用户薄弱点 + 当前页面上下文

### 5.4 交互载体

- **Ctrl+K 命令面板**：全局导航 + 语义搜索 + AI 快捷命令（类似 Raycast）
- **侧边面板**：持久对话 + 历史记录 + 角色切换器
- **内联卡片**：行为触发干预，用完即毁，不保留历史
- **悬浮角标**：轻度通知，点击展开

---

## 六、数据库设计

### 6.1 总览

**复用并扩展（8 张）：**
- `users`（+avatar_url, +bio, +ai_sensitivity）
- `questions`（+college ENUM('ai','comm'), +tags JSON）
- `game_history`、`wrong_questions`（+college, +tag）
- `question_sets`、`question_set_items`、`check_in_log`、`practice_log`

**新增课程体系（4 张）：**
- `colleges` — id, name, slug, icon, description
- `courses` — id, college_id, title, slug, level(L1-L5), order, prerequisites JSON
- `chapters` — id, course_id, title, order
- `lessons` — id, chapter_id, title, content_type(video/text/quiz/code), content JSON, lab_ref, order
  - `content` 字段存 JSON：视频/文本/测验/代码四种类型共用一张表，避免多表 JOIN

**新增学习进度（3 张）：**
- `user_progress` — user_id, lesson_id, status(not_started/in_progress/completed), score, time_spent
- `user_skills_progress` — user_id, skill_id, status(0-LOCKED/1-UNLOCKED/2-STUDYING/3-MASTERED), updated_at
  - 单表状态机，替代分离的 unlocked/mastered 双表设计
- `learning_reports` — user_id, course_id, report_type(weekly/chapter), content JSON, generated_at

**新增错题本（1 张）：**
- `user_wrong_questions` — user_id, question_id, error_count, status(0-待消灭/1-已掌握), last_wrong_at
  - 独立于 game_history，支持反复重做直到掌握的业务特性

**新增实验场（2 张）：**
- `sandbox_sessions` — user_id, container_id, template_ref, source_course_id, source_lesson_id, status, expires_at
- `lab_projects` — user_id, type(sandbox/prompt/agent), title, snapshot JSON, is_published

**新增 AI 与行为（2 张）：**
- `ai_conversations` + `ai_messages` — 对话记录
- `behavior_events` — user_id, session_id, event_type, context JSON, severity(1-5), triggered_rule_id
  - **仅存储聚合结论**，原始生物特征数据绝不上传

**新增游戏扩展（2 张）：**
- `game_modes` — type(pve/pvp/daily), name, rules JSON
- `pvp_matches` — player1_id, player2_id, winner_id, questions JSON, scores JSON

### 6.2 关键约束

- JSON 字段中不放需要 WHERE/JOIN 的核心字段（course_id、user_id 必须抽出来并建索引）
- 排行榜使用 Redis ZSET，MySQL `users.total_score` 逆序索引仅做兜底
- Spring Boot 侧使用 MyBatis-Plus `JacksonTypeHandler` 处理 JSON 字段映射

---

## 七、游戏系统

### 7.1 游戏模式

- **PVE 闯关**：紧扣学院 L1-L5 课程进度，逐关解锁
- **PVP 天梯**：全站题库随机抽题，实时对战
- **每日挑战**：每日刷新的一组题目，完成后获取额外积分

### 7.2 段位系统（复用）

沿用现有 6 段位设计（青铜→王者），青铜/白银失败不扣分。

### 7.3 GameEngine 解耦

答题引擎设计为独立 Service，`/game/play` 和 `/profile/wrongbook` 的重新挑战调用同一组件，仅传入的"题池上下文"不同。

---

## 八、实验场（Lab）

### 8.1 代码沙箱

- Docker 容器隔离，每用户独立容器
- 从课程页携带 `course_id` + `lesson_id` 跳转时，自动加载代码模板
- 支持 Python（Agent 开发、LangChain）和 JavaScript（MCP Server、Skills）
- 容器预热池减少冷启动延迟，空闲 30 分钟自动回收

### 8.2 提示词实验场

- 多模型横向评测：同一 Prompt 并发调用多个 LLM，左右分栏对比输出
- 支持 ChainForge 风格的流水线式 Prompt 编排
- 内置评估指标（准确性、创造性、安全性）

### 8.3 Agent 构建器

- 可视化拖拽构建 Agent 工作流
- 一键发布：构建完成的 Agent 可发布为游戏中的"NPC 擂主"

### 8.4 跨学科实验（通信 × AI）

- AI 辅助协议分析：Python 抓包 + AI 逐字段解释 TCP/SIP 报文
- 提示词驱动网络配置：自然语言描述 → AI 生成 Cisco/Huawei 配置脚本 → Docker 容器验证
- Agent 网络故障诊断：构建排障 Agent，注入故障场景，自动诊断

---

## 九、视觉设计

**风格**：玻璃形态 / 深空蓝（Glassmorphism + Deep Space Blue）
- 毛玻璃卡片（backdrop-filter: blur）+ 渐变光晕 + 深邃空间感
- 深色主题为主，支持亮色切换
- Svelte 动画系统：页面过渡（spring）、粒子背景（canvas）、卡片 hover 微动效

---

## 十、迁移策略

### Phase 1：基础架构建设
- 搭建 SvelteKit + Spring Boot + FastAPI 三服务 Docker Compose
- 复用现有 MySQL 数据库，执行扩展迁移脚本
- 实现用户认证（JWT）+ 现有用户数据平滑迁移

### Phase 2：通信学院 Web 化
- 将 Java Swing 12 个 Panel 逐页迁移为 Web 页面
- GameEngine 核心逻辑用 Java 保持不变，Spring Boot 封装为 REST API
- 复用现有 8 张表，不做破坏性变更

### Phase 3：AI 学院上线
- 课程体系 CMS（colleges/courses/chapters/lessons 四表 + 管理后台）
- AI 服务层集成（LLM 网关 + RAG + 沙箱调度）
- 首批上线：提示词工程 L1-L3 + Agent 开发 L1-L2

### Phase 4：AI 行为监测 + 多角色系统
- 前端行为采集 SDK + Web Worker 聚合
- 规则引擎 + 干预层 UI 组件
- 四角色 System Prompt 调优 + 角色路由器

### Phase 5：实验场 + 游戏化完整闭环
- Docker 沙箱 + 提示词实验场 + Agent 构建器
- PVP 对战 + 天梯排行榜
- 模块联动"传送门"机制

---

## 十一、部署架构

```
Docker Compose (5 services)
├── sveltekit-frontend    (Node.js, port 5173)
├── spring-boot-backend   (JDK 21, port 8080)
├── fastapi-ai-service    (Python 3.12, port 8000)
├── mysql                 (8.0, port 3306)
└── redis                 (7.x, port 6379)
```

所有服务容器化，一键 `docker compose up` 启动。
