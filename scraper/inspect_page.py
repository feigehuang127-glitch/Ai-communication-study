"""
页面结构分析工具
================
打开浏览器让你手动登录并导航到题目页面，
然后在终端中输入 CSS 选择器来测试它们匹配到了什么内容。

用法：
    python inspect.py

常用选择器示例：
    a[href*='question']  — 匹配 href 中包含 question 的链接
    .question-body       — 匹配 class="question-body" 的元素
    li                   — 匹配所有 <li> 标签
    .options li          — 匹配 options 容器下的 li
"""

import sys
from pathlib import Path

from playwright.sync_api import sync_playwright

try:
    from config import STATE_FILE, USE_SAVED_STATE
except ImportError:
    print("[ERROR] 找不到 config.py，请确保在 scraper/ 目录下运行")
    sys.exit(1)


def main():
    playwright = sync_playwright().start()
    state_path = Path(__file__).parent / STATE_FILE

    if USE_SAVED_STATE and state_path.exists():
        context = playwright.chromium.launch_persistent_context(
            user_data_dir=str(Path(__file__).parent / "browser_data"),
            headless=False,
            viewport={"width": 1280, "height": 800},
        )
    else:
        browser = playwright.chromium.launch(headless=False)
        context = browser.new_context(viewport={"width": 1280, "height": 800})

    page = context.pages[0] if context.pages else context.new_page()

    if not state_path.exists():
        input("请先在浏览器中手动登录，完成后按 Enter...")
        context.storage_state(path=str(state_path))
        print(f"[OK] 登录状态已保存")

    print("""
╔══════════════════════════════════════════════════════╗
║            页面结构分析工具                           ║
║                                                      ║
║  1. 在浏览器中导航到题目列表页 / 题目详情页            ║
║  2. 在下方输入 CSS 选择器查看匹配结果                  ║
║  3. 输入 :help  查看更多命令                          ║
║  4. 输入 :quit  退出                                  ║
╚══════════════════════════════════════════════════════╝
""")

    while True:
        cmd = input("\n选择器 > ").strip()
        if not cmd:
            continue
        if cmd in (":quit", ":q", "exit"):
            break
        if cmd in (":help", ":h"):
            print("""
命令：
  <css选择器>      — 匹配元素，显示前 10 个的文本
  <css选择器> @    — 同时显示元素的 HTML（精简）
  <css选择器> @@   — 同时显示元素属性（href, class 等）
  :url             — 显示当前页面 URL
  :title           — 显示页面标题
  :html <选择器>   — 显示匹配元素完整 HTML
  :quit            — 退出
""")
            continue
        if cmd == ":url":
            print(f"  当前 URL: {page.url}")
            continue
        if cmd == ":title":
            print(f"  标题: {page.title()}")
            continue
        if cmd.startswith(":html "):
            selector = cmd[6:].strip()
            try:
                el = page.query_selector(selector)
                if el:
                    print(f"  HTML:\n{el.inner_html()[:2000]}")
                else:
                    print("  [未匹配到任何元素]")
            except Exception as e:
                print(f"  [ERROR] {e}")
            continue

        # 解析选择器
        verbose = False
        attrs = False
        if cmd.endswith(" @@"):
            selector = cmd[:-3].strip()
            attrs = True
        elif cmd.endswith(" @"):
            selector = cmd[:-2].strip()
            verbose = True
        else:
            selector = cmd

        try:
            els = page.query_selector_all(selector)
            if not els:
                print(f"  [未匹配到任何元素]")
                continue

            print(f"  [匹配到 {len(els)} 个元素]\n")
            for i, el in enumerate(els[:10]):
                text = el.inner_text().strip()[:120]
                print(f"  [{i+1}] {text}")
                if verbose:
                    html = el.inner_html()[:200]
                    print(f"      HTML: {html}")
                if attrs:
                    for attr_name in ["href", "class", "id", "data-url", "data-id", "data-answer"]:
                        val = el.get_attribute(attr_name)
                        if val:
                            print(f"      {attr_name}: {val}")
                print()
            if len(els) > 10:
                print(f"  ... 还有 {len(els) - 10} 个未显示")
        except Exception as e:
            print(f"  [ERROR] {e}")

    context.close()
    if "browser" in dir():
        browser.close()
    playwright.stop()


if __name__ == "__main__":
    main()
