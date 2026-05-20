# 知识竞答游戏设计文档 v3

## 概述

基于 Java Swing + MySQL 的知识竞答游戏，题库覆盖通信原理和数据通信网。
玩家答题获取积分，积分累积晋升段位。含打卡机制、错题本、自定义题集。

技术栈：Java 21+ (Swing) + MySQL 8.0 + JDBC + HikariCP + BCrypt + javax.sound

---

## 一、架构设计

### 1.1 单 JFrame + CardLayout

整个游戏只使用一个 `MainFrame`，内部用 `JPanel` 承载不同页面，通过 `CardLayout` 切换，避免多 JFrame 闪烁和位置跳动。

```
MainFrame (JFrame)
├── CardLayout
│   ├── LoginPanel          ("login")
│   ├── MenuPanel           ("menu")
│   ├── CategorySelectPanel ("category")
│   ├── GamePanel           ("game")        ← 核心
│   ├── WinPanel            ("win")
│   ├── LosePanel           ("lose")
│   ├── ReviewPanel         ("review")
│   ├── ProfilePanel        ("profile")
│   ├── WrongBookPanel      ("wrongbook")
│   ├── SetManagerPanel     ("setmanager")
│   ├── PracticePanel       ("practice")
│   └── AdminPanel          ("admin")
```

### 1.2 MVC 分层

```
┌─────────────────────────────────────┐
│  View 层 (youxi.view)              │  JPanel 子类
│  只管：按钮、输入、动画、音效触发     │
│  ⚠️ 禁止在 ActionListener 里直接调 DAO │
├─────────────────────────────────────┤
│  Service 层 (youxi.service)       │  业务逻辑
│  GameService / UserService         │
├─────────────────────────────────────┤
│  DAO 层 (youxi.dao)                │  数据库操作
│  每表一个 DAO，只做 CRUD              │
├─────────────────────────────────────┤
│  Model 层 (youxi.model)           │  实体类
│  User, Question, GameHistory...    │
├─────────────────────────────────────┤
│  Util 层 (youxi.util)             │  工具
│  DBHelper(HikariCP), SoundManager,  │
│  BCryptUtil, ConfigManager          │
└─────────────────────────────────────┘
```

### 1.3 包结构

```
youxi/
├── MainFrame.java              ← 主窗口 + CardLayout
├── model/   (5 个实体类)
├── dao/     (5 个 DAO 类)
├── service/ (3 个 Service 类)
├── view/    (12 个 Panel 类)
└── util/    (4 个工具类)
```

---

## 二、线程安全与防卡顿（关键）

### 2.1 EDT 规则

**所有数据库操作必须在后台线程执行。**

```java
// ✅ 正确做法：View 层触发，后台执行 DB，结果抛回 UI
btnLogin.addActionListener(e -> {
    btnLogin.setEnabled(false); // 先禁用，防连点
    new Thread(() -> {
        User user = userService.login(username, password);
        SwingUtilities.invokeLater(() -> {
            if (user != null) {
                MainFrame.getInstance().show("menu");
            } else {
                JOptionPane.showMessageDialog(this, "登录失败");
            }
            btnLogin.setEnabled(true);
        });
    }).start();
});
```

### 2.2 防抖锁（答题界面）

```java
// GamePanel 中
private volatile boolean isLocked = false;

void onOptionClick(int optionIndex) {
    if (isLocked) return;           // 已锁定，忽略连点
    isLocked = true;

    // 判定对错...
    // 播放动画 + 音效...
    // 0.5 秒后切下一题
    Timer timer = new Timer(500, e -> {
        nextQuestion();
        isLocked = false;           // 解锁
    });
    timer.setRepeats(false);
    timer.start();
}
```

---

## 三、数据库连接管理

**引入 HikariCP 连接池**（企业级标准，单页配置即可）：

```java
// DBHelper.java
public class DBHelper {
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/game_db?...");
        config.setUsername("root");
        config.setPassword("123456");
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(3000);
        config.setIdleTimeout(600000);   // 10 分钟空闲超时
        config.setMaxLifetime(1800000);  // 30 分钟最大存活
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
```

**好处：** 自动处理连接断开重连、连接复用，永远不需要担心 `wait_timeout` 导致的 `CommunicationsException`。

---

## 四、全局设置持久化

**config.properties**（项目根目录，启动时读取，变更时写入）：

```properties
# 游戏设置
sound.enabled=true
vibration.enabled=true
```

**ConfigManager：**
- 启动时：`Properties.load(new FileReader("config.properties"))`
- 开关切换时：同时更新内存变量 和 `Properties.store()` 写入文件
- 不存在配置文件时：使用默认值（全部开启）并自动创建

---

## 五、游戏规则

| 规则项 | 设定 |
|--------|------|
| 每轮题数 | 10 题 |
| 每题时限 | 10 秒 |
| 超时豁免 | 2 次/轮（不算对，**中断连击**） |
| 即时获胜 | 连对 5 题 → **+3 分** |
| 常规获胜 | 答对 7 题 → **+2 分** |
| 失败 | -1 分（青铜/白银不扣分） |
| 每日打卡 | 每天首次登录 +1 分 |
| 错题出狱 | 训练模式同题连对 2 次 → 自动从错题本移除 |

---

## 六、段位系统

| 积分 | 段位 | 难度范围 |
|------|------|---------|
| 0–9 | 青铜 | 1–3 |
| 10–19 | 白银 | 2–4 |
| 20–29 | 黄金 | 3–6 |
| 30–39 | 铂金 | 5–8 |
| 40–49 | 钻石 | 7–9 |
| 50+ | 王者 | 8–10 |

- 青铜/白银：失败不扣分（保护段位）
- 跨阈值时触发晋升动画（RankUpDialog）

---

## 七、数据库设计（8 张表）

### users

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT PK AUTO_INCREMENT | |
| username | VARCHAR(50) UNIQUE NOT NULL | |
| password_hash | VARCHAR(200) NOT NULL | BCrypt |
| role | ENUM('player','admin') DEFAULT 'player' | |
| total_score | INT DEFAULT 0 | |
| rank | VARCHAR(20) DEFAULT '青铜' | |
| last_checkin_date | DATE | 最后打卡日（防重复） |
| created_at | TIMESTAMP DEFAULT CURRENT_TIMESTAMP | |

### questions

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT PK AUTO_INCREMENT | |
| content | TEXT NOT NULL | 题目内容 |
| type | ENUM('single','multiple','judge') | |
| option_a | VARCHAR(200) NOT NULL | |
| option_b | VARCHAR(200) NOT NULL | |
| option_c | VARCHAR(200) | NULL for judge |
| option_d | VARCHAR(200) | NULL for judge |
| answer | VARCHAR(50) NOT NULL | 已排序，如 "A" / "AC" |
| explanation | TEXT | 题解 |
| category | ENUM('通信原理','数据通信网') NOT NULL | |
| difficulty | TINYINT NOT NULL CHECK(1-10) | |
| created_at | TIMESTAMP DEFAULT CURRENT_TIMESTAMP | |

### game_history

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT PK | |
| user_id | INT FK INDEX | |
| category | VARCHAR(20) | |
| result | ENUM('win_combo','win','lose') | |
| correct_count | INT | |
| total_time_seconds | INT | |
| score_earned | INT | 可为负 |
| rank_before | VARCHAR(20) | |
| rank_after | VARCHAR(20) | |
| played_at | TIMESTAMP | |

### wrong_questions

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT PK | |
| user_id | INT FK INDEX | |
| question_id | INT FK | |
| wrong_count | INT DEFAULT 1 | |
| correct_streak | INT DEFAULT 0 | ≥2 则自动删除 |
| last_wrong_at | TIMESTAMP | |

### question_sets

| 字段 | 类型 |
|------|------|
| id | INT PK |
| user_id | INT FK |
| name | VARCHAR(100) |
| category | VARCHAR(20) |
| created_at | TIMESTAMP |

### question_set_items

| 字段 | 类型 |
|------|------|
| id | INT PK |
| set_id | INT FK ON DELETE CASCADE |
| question_id | INT FK |

### check_in_log

| 字段 | 类型 |
|------|------|
| id | INT PK |
| user_id | INT FK |
| check_in_date | DATE |
| created_at | TIMESTAMP |

### practice_log

| 字段 | 类型 |
|------|------|
| id | INT PK |
| user_id | INT FK INDEX |
| type | ENUM('game','practice') |
| category | VARCHAR(20) |
| duration_seconds | INT |
| created_at | TIMESTAMP |

---

## 八、关键业务逻辑

### 8.1 多选答案比对
用户选 ["C","A"] → Java 层排序为 "AC" → 与数据库 answer "AC" 比对

### 8.2 题库枯竭降级
```java
// GameService.generateQuestions()
List<Question> pool = questionDAO.findByDifficultyRange(minDiff, maxDiff);
if (pool.size() < 10) {
    // 向上下借题
    pool.addAll(questionDAO.findByDifficultyRange(minDiff - 2, maxDiff + 2));
    // 发 Toast 提示
    SwingUtilities.invokeLater(() ->
        showToast("当前段位题库不足，已补充其他难度题目"));
}
// 随机抽取 10 题
Collections.shuffle(pool);
return pool.subList(0, Math.min(10, pool.size()));
```

---

## 九、界面清单（12 个 Panel）

| 卡片名 | 类名 | 功能 |
|--------|------|------|
| "login" | LoginPanel | 登录 |
| "menu" | MenuPanel | 主菜单 + 打卡 + 音效/震动开关 |
| "category" | CategorySelectPanel | 选择题库学科 |
| "game" | GamePanel | 答题（含倒计时 + 进度条 + 状态栏） |
| "win" | WinPanel | 获胜 + 段位晋升检测 |
| "lose" | LosePanel | 失败 |
| "review" | ReviewPanel | 逐题复盘 + 题解 + 加错题/加题集 |
| "profile" | ProfilePanel | 个人中心（积分/段位/历史/今日时长） |
| "wrongbook" | WrongBookPanel | 错题本 + 进入训练 |
| "setmanager" | SetManagerPanel | 题集管理 |
| "practice" | PracticePanel | 训练模式（无倒计时） |
| "admin" | AdminPanel | 管理后台（admin 专属 CRUD） |

---

## 十、界面流转

```
"login"      → "menu"
"menu"       → "category" / "profile" / "wrongbook" / "setmanager" / "admin"
"category"   → "game"
"game"       → "win" / "lose"
"win"/"lose" → "review" → "menu"
"wrongbook"  → "practice" → "wrongbook"
"setmanager" → "practice" → "setmanager"
```

---

## 十一、答题界面视觉规范

| 元素 | 规格 |
|------|------|
| **进度条** | >5s 绿 → 3-5s 黄 → <3s 红+脉冲，通过 `paintComponent` 自绘 |
| **倒计时数字** | 等宽 24pt，同步变色 |
| **<3s 报警** | 红色脉冲 + SoundManager.playAlarm() |
| **正确选项** | 绿框+浅绿底+绿圆标 + SoundManager.playBingo() |
| **错误选项** | 红框+浅红底+红圆标 + 震动(±4px) + SoundManager.playError() |
| **锁定** | 点击后 isLocked=true，防连点，0.5s 后解锁切题 |
| **通信原理背景** | 深蓝底 + 正弦波纹 + 电路纹理 |
| **数据通信网背景** | 青蓝底 + 0/1 矩阵 + 节点拓扑 |
| **音效/震动** | 通过 config.properties 持久化控制 |

---

## 十二、开发策略（5 个 Phase）

### Phase 1：基础设施 + 控制台验证
- 执行建表 SQL（8 张表，含索引和外键）
- 引入 HikariCP jar，配置连接池
- 预置 20 道题（两个学科各 10，覆盖难度 1-5）
- 建 admin 和测试玩家账号
- **控制台 main 测试：DAO 全通，能查到题并打印**

### Phase 2：核心游戏链路
- MainFrame + CardLayout 框架
- LoginPanel → MenuPanel → CategorySelectPanel
- GamePanel + javax.swing.Timer 倒计时 + 进度条
- GameService（出题/判分/连对/获胜/题库降级）
- WinPanel / LosePanel
- 积分更新 + 段位晋升

### Phase 3：辅助功能
- ReviewPanel（题目回顾 + 题解 + 错题本/题集操作）
- WrongBookPanel + 错题出狱逻辑
- SetManagerPanel + PracticePanel
- ProfilePanel（历史 + 时长统计）
- 每日打卡
- AdminPanel（CRUD 题目）

### Phase 4：视觉优化
- 进度条颜色渐变（Graphics2D）
- 震动动画（Timer + translateX）
- 正确/错误视觉反馈
- 学科主题背景（paintComponent 自绘）
- SoundManager 预加载（启动时 Clip 缓存）
- config.properties 持久化

### Phase 5：打磨
- BCrypt 集成
- 空状态提示（题库为空 / 无错题 / 无历史）
- 全局字体统一
- 异常兜底

---

## 十三、知识点覆盖

| 知识点 | 载体 |
|--------|------|
| JDBC + PreparedStatement | 所有数据库操作 |
| **HikariCP 连接池** | DBHelper |
| **SwingWorker / 后台线程** | 所有 DAO 调用 |
| **EDT 线程安全** | invokeLater 抛回 UI |
| MVC 分层架构 | 全项目 |
| CardLayout 页面切换 | MainFrame |
| **Graphics2D 自绘** | 进度条、学科背景 |
| javax.swing.Timer | 倒计时、震动动画、防抖延迟 |
| **单例模式** | SoundManager, ConfigManager |
| BCrypt 密码哈希 | 注册/登录 |
| 数据库索引 + 级联删除 | 建表 SQL |
| javax.sound.sampled.Clip | 音效播放 |
| Properties IO | 配置持久化 |
| 多表联查 | 历史记录、题集关联 |
| DAO / Service 分层 | 全局架构 |
| **防抖锁** | GamePanel.isLocked |
