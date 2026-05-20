"""
深度分析脚本（非交互式）
========================
访问 /question 题库页面，分析题目列表和详情页结构。
"""
import json
import sys
from pathlib import Path
from playwright.sync_api import sync_playwright

OUTPUT_DIR = Path(__file__).parent / "analysis_output"
OUTPUT_DIR.mkdir(exist_ok=True)
STATE_FILE = Path(__file__).parent / "state.json"


def save_page_info(page, name):
    """保存页面信息"""
    screenshot = OUTPUT_DIR / f"{name}.png"
    html_file = OUTPUT_DIR / f"{name}.html"
    analysis_file = OUTPUT_DIR / f"{name}_analysis.json"

    page.screenshot(path=str(screenshot), full_page=True)
    html_file.write_text(page.content(), encoding="utf-8")

    # 分析关键元素
    info = {
        "url": page.url,
        "title": page.title(),
        "total_links": 0,
        "sample_links": [],
        "forms": [],
        "containers": [],
        "lists": [],
    }

    # 链接
    links = page.query_selector_all("a[href]")
    info["total_links"] = len(links)
    for link in links[:50]:
        href = link.get_attribute("href") or ""
        text = link.inner_text().strip()
        cls = link.get_attribute("class") or ""
        if text:
            info["sample_links"].append({"text": text[:80], "href": href[:120], "class": cls})

    # 表单
    for form in page.query_selector_all("form"):
        action = form.get_attribute("action") or ""
        inputs = []
        for inp in form.query_selector_all("input"):
            inputs.append({
                "name": inp.get_attribute("name") or "",
                "type": inp.get_attribute("type") or "",
                "placeholder": inp.get_attribute("placeholder") or "",
            })
        buttons = []
        for btn in form.query_selector_all("button"):
            buttons.append(btn.inner_text().strip())
        info["forms"].append({"action": action, "inputs": inputs, "buttons": buttons})

    # 主要容器
    for el in page.query_selector_all("div[class], section[class], ul[class]"):
        cls = el.get_attribute("class") or ""
        text = el.inner_text().strip()[:120]
        if len(text) > 5:
            info["containers"].append({"tag": el.evaluate("el => el.tagName.toLowerCase()"), "class": cls, "text_preview": text})

    # 列表
    for el in page.query_selector_all("ul, ol"):
        items = el.query_selector_all(":scope > li")
        if items:
            sample = [it.inner_text().strip()[:80] for it in items[:5]]
            info["lists"].append({"items_count": len(items), "sample": sample})

    analysis_file.write_text(json.dumps(info, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"  [{name}] URL={page.url} | 截图={screenshot}")
    return info


def main():
    mode = sys.argv[1] if len(sys.argv) > 1 else "explore"

    print(f"""
╔══════════════════════════════════════════════════════════╗
║        huikao8.com 深度结构分析 (mode={mode})            ║
╚══════════════════════════════════════════════════════════╝
""")

    playwright = sync_playwright().start()

    if STATE_FILE.exists():
        print("[INFO] 使用已保存的登录状态")
        context = playwright.chromium.launch_persistent_context(
            user_data_dir=str(Path(__file__).parent / "browser_data"),
            headless=False,
            viewport={"width": 1280, "height": 800},
        )
    else:
        print("[INFO] 无登录状态，以游客模式访问")
        browser = playwright.chromium.launch(headless=False)
        context = browser.new_context(viewport={"width": 1280, "height": 800})

    page = context.pages[0] if context.pages else context.new_page()

    # 1. 题库列表页
    print("\n--- 分析题库列表页 ---")
    page.goto("https://www.huikao8.com/question", wait_until="networkidle", timeout=30000)
    page.wait_for_timeout(3000)
    save_page_info(page, "02_question_list")

    # 2. 尝试找一道具体题目
    print("\n--- 尝试找到题目详情入口 ---")
    # 查找可能的题目链接
    question_links = []
    for link in page.query_selector_all("a[href]"):
        href = link.get_attribute("href") or ""
        if any(kw in href.lower() for kw in ["question", "topic", "item", "detail", "problem", "exam"]):
            question_links.append(href)

    print(f"  找到 {len(question_links)} 个可能的题目链接")
    for href in question_links[:10]:
        print(f"    {href}")

    # 3. 如果找到题目链接，访问第一个
    if question_links:
        first_url = question_links[0]
        if not first_url.startswith("http"):
            first_url = f"https://www.huikao8.com{first_url}"
        print(f"\n--- 访问题目详情页: {first_url} ---")
        page.goto(first_url, wait_until="networkidle", timeout=30000)
        page.wait_for_timeout(3000)
        save_page_info(page, "03_question_detail")

        # 分析题目详情页的具体元素
        print("\n--- 题目详情页元素分析 ---")
        all_text = page.inner_text("body")
        print(f"  页面总文本长度: {len(all_text)}")
        print(f"  前500字: {all_text[:500]}")

    # 4. 尝试访问 VIP 题库
    print("\n--- 分析 VIP 题库页 ---")
    page.goto("https://www.huikao8.com/vipQuestion", wait_until="networkidle", timeout=30000)
    page.wait_for_timeout(3000)
    save_page_info(page, "04_vip_question_list")

    print("\n[DONE] 分析完成，结果保存在 analysis_output/")
    context.close()
    if "browser" in dir():
        browser.close()
    playwright.stop()


if __name__ == "__main__":
    main()
