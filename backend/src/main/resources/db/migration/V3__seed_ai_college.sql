-- AI 学院
INSERT INTO colleges (name, slug, icon, description) VALUES
('AI 学院', 'ai', 'robot', '提示词工程 · Skills 开发 · Agent 构建');

SET @ai_college_id = LAST_INSERT_ID();

-- 提示词工程课程
INSERT INTO courses (college_id, title, slug, level, `order`, description) VALUES
(@ai_college_id, '提示词工程基础', 'prompt-engineering', 'L1', 1, '从零掌握 Prompt 设计方法论：指令型/角色型/思维链/少样本提示');

SET @pe_course_id = LAST_INSERT_ID();

INSERT INTO chapters (course_id, title, `order`) VALUES
(@pe_course_id, '认识大语言模型', 1),
(@pe_course_id, '基础提示词模式', 2),
(@pe_course_id, '思维链与推理', 3);

SET @ch1_id = (SELECT id FROM chapters WHERE course_id = @pe_course_id AND `order` = 1);
SET @ch2_id = (SELECT id FROM chapters WHERE course_id = @pe_course_id AND `order` = 2);
SET @ch3_id = (SELECT id FROM chapters WHERE course_id = @pe_course_id AND `order` = 3);

INSERT INTO lessons (chapter_id, title, content_type, content, `order`) VALUES
(@ch1_id, 'LLM 工作原理', 'text',
 '{"body":"## Token 与上下文窗口\n\nLLM 将文本切分为 token（词元）进行处理。一个 token 约等于 0.75 个英文单词或 0.5 个中文字。\n\n### 关键概念\n- **上下文窗口**：模型一次能处理的最大 token 数（如 GPT-4 为 128K）\n- **Temperature**：控制输出的随机性（0=确定，1=创造性）\n- **Top-P**：核采样，只从累积概率达到 P 的 token 中选择"}',
 1),
(@ch1_id, '主流模型对比', 'text',
 '{"body":"## 模型选择指南\n\n| 模型 | 优势 | 适用场景 |\n|------|------|--------|\n| Claude | 长文理解、代码 | 技术文档、Agent |\n| GPT-4o | 多模态、速度 | 通用对话、图像 |\n| DeepSeek | 推理、性价比 | 代码生成、数学 |"}',
 2),
(@ch2_id, '指令型提示词', 'text',
 '{"body":"## 指令型 Prompt 公式\n\n**角色 + 任务 + 格式 + 约束**\n\n### 示例\n```\n你是一个资深 Java 架构师。请审查以下代码，关注线程安全和性能问题。\n用列表形式输出，每个问题一行，标注严重程度。\n```\n\n### 常见指令类型\n- **总结**：用 3 句话概括以下内容\n- **翻译**：将以下文本翻译为英文，保持技术术语准确性\n- **提取**：从以下邮件中提取姓名、日期和行动项"}',
 1),
(@ch2_id, '角色扮演提示词', 'text',
 '{"body":"## 角色扮演 Prompt 模式\n\n有效的角色定义包含三个要素：\n1. **身份**（你是谁）\n2. **能力**（你擅长什么）\n3. **边界**（你不能做什么）\n\n### 示例\n```\n你是一个经验丰富的 SRE 工程师。\n能力：诊断分布式系统故障，阅读日志和监控数据。\n边界：不猜测，不确定时说需要更多信息。\n```"}',
 2),
(@ch3_id, '思维链 (Chain-of-Thought)', 'text',
 '{"body":"## 思维链 Prompt\n\n让模型展示推理过程，而不是直接给答案。\n\n### 零样本思维链\n在问题末尾添加：**让我们一步一步地思考。**\n\n### 少样本思维链\n提供 2-3 个带推理步骤的示例，再问目标问题。\n\n### 示例\n```\nQ: 一个班有 30 个学生，60% 是男生。后来转来 5 个女生。\n现在男生占百分之几？让我们一步一步地思考。\n```"}',
 1);

-- Agent 开发课程
INSERT INTO courses (college_id, title, slug, level, `order`, description) VALUES
(@ai_college_id, 'Agent 开发入门', 'agent-development', 'L2', 2, '构建你的第一个 AI Agent：工具调用、记忆管理、多步推理');

SET @agent_course_id = LAST_INSERT_ID();

INSERT INTO chapters (course_id, title, `order`) VALUES
(@agent_course_id, 'Agent 架构基础', 1),
(@agent_course_id, '工具调用 (Tool Use)', 2),
(@agent_course_id, '记忆与状态管理', 3);

SET @ag_ch1 = (SELECT id FROM chapters WHERE course_id = @agent_course_id AND `order` = 1);
SET @ag_ch2 = (SELECT id FROM chapters WHERE course_id = @agent_course_id AND `order` = 2);
SET @ag_ch3 = (SELECT id FROM chapters WHERE course_id = @agent_course_id AND `order` = 3);

INSERT INTO lessons (chapter_id, title, content_type, content, `order`) VALUES
(@ag_ch1, '什么是 AI Agent', 'text',
 '{"body":"## Agent = LLM + 工具 + 记忆 + 规划\n\nAgent 是一个能够自主感知环境、做出决策、执行行动的智能体。\n\n### 核心组件\n- **大脑 (LLM)**：理解任务、生成计划\n- **工具 (Tools)**：搜索、计算、API 调用\n- **记忆 (Memory)**：短期（对话上下文）+ 长期（向量数据库）\n- **规划 (Planning)**：任务分解、自我反思"}',
 1),
(@ag_ch1, 'Agent 循环', 'text',
 '{"body":"## Agent 核心循环\n\n```\n1. 感知 → 读取用户输入 + 环境状态\n2. 思考 → LLM 推理下一步行动\n3. 行动 → 调用工具或返回结果\n4. 观察 → 收集行动结果\n5. 重复 → 直到任务完成\n```\n\n这就是 ReAct (Reasoning + Acting) 模式。"}',
 2),
(@ag_ch2, '函数调用 (Function Calling)', 'text',
 '{"body":"## Function Calling 机制\n\n### 工作流程\n1. 定义工具 schema（JSON Schema 格式）\n2. LLM 决定是否调用工具，输出函数名+参数\n3. 你的代码执行函数，将结果返回 LLM\n4. LLM 基于结果生成最终回复\n\n### 工具定义示例\n```json\n{\n  "name": "search_code",\n  "description": "搜索代码库",\n  "parameters": {\n    "query": {"type": "string"},\n    "language": {"type": "string", "enum": ["java", "python", "js"]}\n  }\n}\n```"}',
 1),
(@ag_ch3, '对话记忆实现', 'code',
 '{"body":"## 实现对话记忆\n\n```python\nfrom langchain.memory import ConversationBufferMemory\n\nmemory = ConversationBufferMemory(\n    memory_key="chat_history",\n    return_messages=True\n)\n\n# 每次对话后将交互存入记忆\nmemory.save_context(\n    {"input": "帮我写一个排序函数"},\n    {"output": "这是快速排序实现..."}\n)\n```\n\n### 记忆类型\n- **Buffer**：保留最近 N 条消息\n- **Summary**：用 LLM 压缩历史为摘要\n- **Vector**：嵌入存储，语义检索相关历史"}',
 1);
