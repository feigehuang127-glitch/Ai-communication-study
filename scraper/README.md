# 会考吧题库爬虫使用指南

## 第一步：安装依赖

```bash
cd scraper
pip install -r requirements.txt
playwright install chromium
```

## 第二步：分析页面结构（关键！）

运行 inspect 工具，它会打开浏览器：

```bash
python inspect.py
```

1. 在浏览器中手动登录
2. 导航到你想要爬取的**题目列表页**
3. 在终端中使用 CSS 选择器找出正确的元素，例如：
   - 输入 `a` 看看所有链接
   - 输入 `.question` 或 `.topic` 试试题目链接的 class
   - 点开一道题，试试 `.options li` 或类似的选项选择器
4. 找到正确选择器后，修改 `config.py` 中的 `SELECTORS`

## 第三步：运行爬虫

**首次运行（需要登录）：**
```bash
python scraper.py --login
```

浏览器会打开，手动完成登录（含验证码），完成后回到终端按 Enter。

**后续运行（复用登录态）：**
```bash
python scraper.py
```

## 第四步：转换格式供 Java 项目使用

```bash
python convert.py questions.json questions.csv
```

生成的 CSV 可直接导入到 game 项目中使用。

## 配置说明

`config.py` 中需要根据实际页面调整的参数：

| 参数 | 说明 |
|------|------|
| `BASE_URL` | 网站根地址 |
| `LOGIN_URL` | 登录页地址 |
| `START_URL` | 从哪个题库列表页开始爬 |
| `question_links` | 题目详情页链接的 CSS 选择器 |
| `question_body` | 题目正文的 CSS 选择器 |
| `options` | 选项列表的 CSS 选择器 |
| `correct_answer` | 正确答案的 CSS 选择器 |
| `MIN_DELAY / MAX_DELAY` | 请求间隔（毫秒），建议 ≥ 1000 |

## 断点续爬

爬虫每 50 题自动保存一次，按 Ctrl+C 中断也会保存。重新运行会自动跳过已爬取的题目（基于 URL 去重）。

## 输出格式

```json
[
  {
    "id": "a1b2c3d4",
    "url": "https://www.huikao8.com/question/123",
    "question": "以下哪个是Java的基本数据类型？",
    "options": ["int", "String", "List", "Map"],
    "answer": "A",
    "analysis": "int 是 Java 的 8 种基本数据类型之一"
  }
]
```
