"""
爬虫配置文件
============
使用方法：
1. 先运行 inspect.py 打开浏览器，手动登录并导航到题目列表页
2. 按 F12 打开开发者工具，查看题目页面的 HTML 结构
3. 根据实际结构修改下方 CSS 选择器
4. 运行 scraper.py 开始爬取
"""

# ============================================================
# 网站入口（根据你要爬的题库类型修改）
# ============================================================
BASE_URL = "https://www.huikao8.com"
LOGIN_URL = f"{BASE_URL}/login"  # 登录页，如果不对请修改
START_URL = f"{BASE_URL}"        # 从哪个页面开始爬取，改为你要爬的题库首页

# ============================================================
# CSS 选择器 — 需根据实际页面结构调整
# ============================================================
SELECTORS = {
    # --- 题目列表页 ---
    "question_links": "a[href*='question'], a[href*='topic'], a[href*='item']",
    "next_page": "a.next, a:has-text('下一页'), .pagination .next",

    # --- 题目详情页 ---
    "question_body": ".question-body, .topic-content, .question-content",
    "options": ".option, .options li, .answer-item",
    "correct_answer": ".correct, .answer, .right-answer",
    "analysis": ".analysis, .explanation, .parse-content",

    # --- 登录页 ---
    "username_input": "input[name='username'], input[type='text']",
    "password_input": "input[name='password'], input[type='password']",
    "login_button": "button[type='submit'], input[type='submit']",
}

# ============================================================
# 等待时间（毫秒），避免请求过快被 ban
# ============================================================
MIN_DELAY = 1000   # 每次请求最小间隔
MAX_DELAY = 3000   # 每次请求最大间隔

# ============================================================
# 输出文件
# ============================================================
OUTPUT_FILE = "questions.json"

# ============================================================
# 是否使用已保存的登录状态（state.json）
# 首次运行后自动保存，后续无需重复登录
# ============================================================
USE_SAVED_STATE = True
STATE_FILE = "state.json"
