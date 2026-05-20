"""
自动化页面结构分析脚本
======================
打开浏览器，截图并输出关键 HTML 结构，帮助确定 CSS 选择器。

用法：
    python analyze.py
"""

import json
import sys
from pathlib import Path
from playwright.sync_api import sync_playwright

OUTPUT_DIR = Path(__file__).parent / "analysis_output"
OUTPUT_DIR.mkdir(exist_ok=True)

BASE_URL = "https://www.huikao8.com"


def analyze_page(page, name, url):
    """分析单个页面：截图 + 提取关键元素"""
    print(f"\n{'='*60}")
    print(f"[{name}] 正在访问: {url}")
    print(f"{'='*60}")

    page.goto(url, wait_until="domcontentloaded", timeout=20000)
    # 等待可能的动态内容加载
    page.wait_for_timeout(3000)

    # 截图
    screenshot_path = OUTPUT_DIR / f"{name}.png"
    page.screenshot(path=str(screenshot_path), full_page=True)
    print(f"  截图已保存: {screenshot_path}")

    # 获取页面 HTML 摘要
    html = page.content()
    html_path = OUTPUT_DIR / f"{name}.html"
    html_path.write_text(html, encoding="utf-8")
    print(f"  HTML 已保存: {html_path} (大小: {len(html)} 字符)")

    # 分析常见的题目相关元素
    analysis = {
        "url": page.url,
        "title": page.title(),
    }

    # 查找所有链接
    links = page.query_selector_all("a[href]")
    question_links = []
    for link in links:
        href = link.get_attribute("href") or ""
        text = link.inner_text().strip()
        cls = link.get_attribute("class") or ""
        if text and len(text) > 3:
            question_links.append({
                "text": text[:80],
                "href": href[:120],
                "class": cls,
            })

    analysis["total_links"] = len(links)
    analysis["text_links_sample"] = question_links[:30]

    # 查找主要的 div/section 容器
    containers = []
    for tag in ["div", "section", "article", "main", "ul", "ol"]:
        for el in page.query_selector_all(tag):
            cls = el.get_attribute("class") or ""
            el_id = el.get_attribute("id") or ""
            if cls or el_id:
                text_preview = el.inner_text().strip()[:100]
                containers.append({
                    "tag": tag,
                    "class": cls,
                    "id": el_id,
                    "text_preview": text_preview,
                })

    analysis["containers_sample"] = containers[:40]

    # 保存分析结果
    analysis_path = OUTPUT_DIR / f"{name}_analysis.json"
    analysis_path.write_text(
        json.dumps(analysis, ensure_ascii=False, indent=2),
        encoding="utf-8"
    )
    print(f"  分析结果: {analysis_path}")


def main():
    print("""
╔══════════════════════════════════════════════════════════╗
║        huikao8.com 页面结构自动化分析                      ║
║                                                          ║
║  脚本将:                                                 ║
║  1. 打开浏览器，请你手动登录                                ║
║  2. 截图首页、登录后的页面                                  ║
║  3. 提取关键 HTML 结构                                     ║
║                                                          ║
║  注意：你需要手动输入登录后的目标页面 URL                     ║
╚══════════════════════════════════════════════════════════╝
""")

    playwright = sync_playwright().start()
    state_path = Path(__file__).parent / "state.json"

    if state_path.exists():
        context = playwright.chromium.launch_persistent_context(
            user_data_dir=str(Path(__file__).parent / "browser_data"),
            headless=False,
            viewport={"width": 1280, "height": 800},
        )
    else:
        browser = playwright.chromium.launch(headless=False)
        context = browser.new_context(viewport={"width": 1280, "height": 800})

    page = context.pages[0] if context.pages else context.new_page()

    # Step 1: 访问首页
    analyze_page(page, "01_homepage", BASE_URL)

    # Step 2: 让用户登录
    if not state_path.exists():
        print("\n" + "="*60)
        print("  请在浏览器中手动登录 huikao8.com")
        print("  登录完成后回到终端按 Enter")
        print("="*60)
        input("\n按 Enter 继续...")
        context.storage_state(path=str(state_path))
        print("[OK] 登录状态已保存")

    # Step 3: 用户提供目标页面 URL
    print("\n" + "="*60)
    print("  现在请在浏览器中导航到你想要爬取的题目页面")
    print("  (例如：题库列表页、某道具体题目页)")
    print("  然后回到终端，输入该页面的 URL")
    print("="*60)

    target_url = input("\n题目页面 URL (直接回车跳过): ").strip()
    if target_url:
        analyze_page(page, "02_question_page", target_url)
    else:
        # 如果用户不输入，尝试分析当前页面
        analyze_page(page, "02_current_page", page.url)

    print("\n" + "="*60)
    print("  分析完成！所有结果保存在:")
    print(f"  {OUTPUT_DIR}")
    print("")
    print("  请查看 analysis_output/ 目录下的文件：")
    print("  - *.png           页面截图")
    print("  - *.html          页面完整 HTML")
    print("  - *_analysis.json 提取的关键元素")
    print("="*60)

    context.close()
    if "browser" in dir():
        browser.close()
    playwright.stop()


if __name__ == "__main__":
    main()
