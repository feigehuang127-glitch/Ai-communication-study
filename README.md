# AI Academy (AI 智能交互式学习平台)

AI Academy 是一款面向全栈大模型应用、提示词工程与通信网体系的**自适应、个性化交互式学习平台**。系统由微服务化 AI 多角色导师系统（FastAPI）、高性能网关与业务核心（Spring Boot 21）以及流式现代前端（SvelteKit）共同构建。通过前端无感行为埋点捕获，配合后端的准实时规则匹配，实现 AI 导师的主动干预指导与动态天梯对战。

---

## 核心特性

- **智能 AI 多角色导师系统**：基于 FastAPI 实现大模型网关，集成了**讲解老师（Lecturer）**、**代码导师（Code Mentor）**、**陪练同学（Study Buddy）**和**学习分析师（Analyst）**四大原生 Persona，通过 RAG 知识库无缝注入当前课时上下文。
- **准实时用户行为监测引擎 (Adaptive Intervention)**：前端基于多线程 Web Worker 准实时聚合用户的点击、滚动深度、文本选中、答题犹豫度（Latency）等行为状态机，准实时向后端刷写，控制 AI 助手触发弹性干预。
- **游戏化天梯竞技 (Gamified Matchmaking)**：基于 Redis `ZSet` 实现高性能积分排行榜，自研天梯队列匹配逻辑，配合多选题部分得分（Partial Credit）衰减算法，动态适配用户段位难度。
- **强隔离代码沙箱 (Code Sandbox)**：系统支持 Python / JavaScript 在线轻量级执行，通过容器组多租户限制（CPU 纳秒配额、内存硬上限、网络断开及非特权约束）保障宿主机底层安全。

---

## 技术栈架构

### 前端 (Frontend)
- **核心框架**：SvelteKit 2.5 + Svelte 4.2 (响应式无虚拟 DOM 运行时)
- **样式系统**：TailwindCSS 3.4 + Glassmorphism（现代流光玻璃微拟物风格）
- **状态管理**：Svelte Writable Store 派生架构
- **并发机制**：原生 Web Worker 行为数据流监听器

### 后端 (Core Business Service)
- **核心框架**：Spring Boot 3.3.0 + Java 21 (虚拟线程友好时代架构)
- **安全认证**：Spring Security 6.x + JWT 无状态令牌
- **持久化层**：Spring Data JPA + Hibernate + Flyway 数据库版本迁移控制
- **数据存储**：MySQL 8.0 (关系型数据、JSON 扩展类型字段) + Redis 7.x-alpine (高性能天梯 ZSet 排行榜)

### AI 微服务 (AI Engine & Gateway)
- **核心框架**：FastAPI 0.111 + Pydantic v2 (极速异步 ASGI 运行时)
- **大模型调用**：Anthropic API / OpenAI / DeepSeek (流式 SSE)
- **沙箱编排**：Docker SDK for Python

---

## 项目目录拓扑结构

```text
ai-communication-study/
├── ai-service/                # Python FastAPI AI 微服务
│   ├── prompts/               # 四大教学 Persona 系统提示词
│   ├── routers/               # 大模型流式聊天、沙箱操控、多模型对比 API
│   ├── services/              # 编排层（LLM 路由、沙箱隔离管理器、RAG 引擎）
│   └── config.py              # Pydantic BaseSettings 环境变量驱动配置
├── backend/                   # Java Spring Boot 业务核心
│   ├── src/main/java/com/platform/
│   │   ├── config/            # 跨域安全、Redis、SecurityFilterChain 配置
│   │   ├── controller/        # 行为监测接入端点、天梯对战、后台管理
│   │   ├── model/             # 多表关联及 JSON 映射（BehaviorEvent, PvpMatch等）
│   │   └── service/           # 核心业务（天梯O(1)去重、部分打分动态博弈算法）
│   └── src/main/resources/    # db/migration 目录内管理 Flyway SQL 脚本
├── frontend/                  # SvelteKit 现代极简流光前端
│   ├── src/lib/               # 共享层（API 客户端、Web Worker 聚合、核心组件等）
│   └── src/routes/            # 核心视图路由（天梯大厅、AI 实验室、管理后台、个人报告）
└── docker-compose.yml         # 统一微服务集群本地一键编排容器配置文件
```

---

## 快速本地开发部署

### 1. 准备环境配置文件

在项目根目录下创建 `.env` 文件，并配置您的凭证信息：

```env
# 数据库 root 密码
MYSQL_ROOT_PASSWORD=your_secure_password_123

# 大模型 API 密钥（选填，至少填写一项）
ANTHROPIC_API_KEY=sk-ant-xxxxxxxxxxxxxxxxxx
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxx
DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxxxx

# 沙箱控制开关（本地开发测试可设为 true，需注意 Docker 控制安全）
SANDBOX_ENABLED=false
```

### 2. 构建并一键启动微服务集群

系统内部集成了 Flyway 迁移脚本，首次启动会自动初始化 MySQL 8.0 库表并自动向 `platform_db` 灌入预置的学院、课程、章节、以及大模型前沿体系基础数据：

```bash
docker-compose up --build -d
```

启动成功后，集群各服务的映射端点如下：

- **前端用户界面 (Frontend)**: `http://localhost:5173`
- **业务网关控制层 (Backend)**: `http://localhost:8080`
- **AI 核心智能体服务 (AI Service)**: `http://localhost:8000`

---

## 生产环境安全加固指南

1. **大模型沙箱强隔离**：在实际线上多租户高并发场景下，切勿开启宿主机 `docker.sock` 挂载权限。建议在独立的微型 KVM 虚拟机（例如 AWS Firecracker）中，或者通过指定具有独立内核态文件系统过滤的 `--runtime=runsc` (gVisor) 环境执行不受信任的学生程序。
2. **CORS 域限制**：在生产环境下的 `application.yml` 或系统环境变量中，显式覆盖 `cors.allowed-origins`，将其配置为准确的生产域名，规避通配符带来的钓鱼与跨域泄露风险。
3. **JWT 签名篡改防御**：确保替换 `jwt.secret` 默认提供的占位符字符串，在生产环境生成长度不低于 256 位的高强度密钥，防止攻击者逆向爆破生成管理员 Token。
