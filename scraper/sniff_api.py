"""
API 嗅探工具
============
打开浏览器，拦截所有网络请求，帮助找到真正的题目 API。

用法：
    python sniff_api.py

流程：
    1. 浏览器自动打开 huikao8.com
    2. 你手动登录，然后导航到刷题页面
    3. 做几道题（让 API 请求触发）
    4. 回到终端按 Ctrl+C 停止
    5. 脚本输出所有捕获到的 API 请求

输出保存在 analysis_output/api_sniff.json
"""

import json
import sys
from pathlib import Path
from urllib.parse import urlparse, parse_qs
from playwright.sync_api import sync_playwright

OUTPUT_DIR = Path(__file__).parent / "analysis_output"
OUTPUT_DIR.mkdir(exist_ok=True)
STATE_FILE = Path(__file__).parent / "state.json"
BROWSER_DATA = Path(__file__).parent / "browser_data"

API_LOG = []


def handle_request(request):
    """捕获所有请求"""
    url = request.url
    parsed = urlparse(url)
    method = request.method
    resource_type = request.resource_type
    post_data = request.post_data

    # 过滤掉静态资源
    skip_extensions = ('.js', '.css', '.png', '.jpg', '.jpeg', '.gif', '.svg',
                       '.ico', '.woff', '.woff2', '.ttf', '.eot', '.webp')
    if any(url.lower().endswith(ext) for ext in skip_extensions):
        return

    # 过滤掉常见的第三方
    skip_domains = ('google', 'gtag', 'analytics', 'baidu', 'cnzz',
                    'hm.baidu', 'facebook', 'twitter')
    if any(d in parsed.netloc for d in skip_domains):
        return

    entry = {
        "url": url,
        "method": method,
        "type": resource_type,
        "post_data": post_data[:500] if post_data else None,
    }

    # 尝试解析 JSON 响应
    if resource_type in ("xhr", "fetch"):
        entry["is_api"] = True

    API_LOG.append(entry)


def handle_response(response):
    """捕获响应，提取 JSON 数据"""
    url = response.url
    # 找到对应的请求条目
    for entry in reversed(API_LOG):
        if entry["url"] == url:
            try:
                body = response.text()
                # 尝试解析 JSON
                if body and body.strip().startswith('{'):
                    data = json.loads(body)
                    entry["response_json"] = data
                    # 截取关键信息
                    if isinstance(data, dict):
                        entry["response_keys"] = list(data.keys())
                        # 判断是否是题目数据
                        for key in data:
                            val = data[key]
                            if isinstance(val, list) and val and isinstance(val[0], dict):
                                entry["response_sample_keys"] = list(val[0].keys())
                                entry["response_item_count"] = len(val)
            except Exception:
                entry["response_preview"] = (response.body() or b"")[:200].decode("utf-8", errors="replace")
            break


def main():
    print("""
╔══════════════════════════════════════════════════════════════╗
║              huikao8.com API 嗅探工具                        ║
║                                                              ║
║  1. 浏览器将自动打开                                          ║
║  2. 请手动登录你的账号                                        ║
║  3. 导航到你要爬取的题库 → 点开做题                            ║
║  4. 做几道题（触发题目 API 请求）                              ║
║  5. 完成后回到终端按 Ctrl+C 停止                               ║
║                                                              ║
║  脚本会记录所有 API 请求，帮你找到题目数据的接口                ║
╚══════════════════════════════════════════════════════════════╝
""")

    playwright = sync_playwright().start()

    if STATE_FILE.exists():
        print("[INFO] 使用已保存的登录状态")
        context = playwright.chromium.launch_persistent_context(
            user_data_dir=str(BROWSER_DATA),
            headless=False,
            viewport={"width": 1280, "height": 800},
            args=["--disable-blink-features=AutomationControlled"],
        )
    else:
        print("[INFO] 新建浏览器会话")
        browser = playwright.chromium.launch(headless=False)
        context = browser.new_context(
            viewport={"width": 1280, "height": 800},
        )

    page = context.pages[0] if context.pages else context.new_page()

    # 注册网络拦截
    page.on("request", handle_request)
    page.on("response", handle_response)

    # 打开首页
    page.goto("https://www.huikao8.com", wait_until="networkidle")
    wait_seconds = 180  # 等待3分钟
    print(f"\n[INFO] 浏览器已打开，请立即登录并导航到刷题页面做题...")
    print(f"[INFO] 脚本将在 {wait_seconds} 秒后自动停止\n")

    for i in range(wait_seconds, 0, -1):
        try:
            import time
            time.sleep(1)
            if i % 30 == 0:
                print(f"  剩余 {i} 秒...")
        except KeyboardInterrupt:
            break

    print("\n[INFO] 正在分析捕获的 API 请求...")

    # 过滤出可能的 API 请求
    api_calls = [e for e in API_LOG if e.get("type") in ("xhr", "fetch")]

    # 找出疑似题目 API
    question_apis = []
    for entry in api_calls:
        url = entry["url"]
        parsed = urlparse(url)
        path = parsed.path.lower()
        score = 0

        # 打分
        keywords = ["question", "topic", "exam", "subject", "item", "problem",
                    "exercise", "practice", "answer", "option", "content"]
        for kw in keywords:
            if kw in path or kw in url.lower():
                score += 3

        if "response_sample_keys" in entry:
            sample_keys = entry["response_sample_keys"]
            for kw in ["question", "answer", "option", "title", "content", "stem", "correct"]:
                if any(kw in k.lower() for k in sample_keys):
                    score += 5

        if entry.get("response_item_count", 0) > 0:
            score += 2

        entry["relevance_score"] = score
        if score > 0:
            question_apis.append(entry)

    # 排序输出
    question_apis.sort(key=lambda x: x.get("relevance_score", 0), reverse=True)

    # 保存
    output = {
        "total_requests": len(API_LOG),
        "total_api_calls": len(api_calls),
        "question_api_candidates": question_apis[:30],
        "all_api_calls": api_calls[:100],
    }
    output_path = OUTPUT_DIR / "api_sniff.json"
    output_path.write_text(json.dumps(output, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"\n[SAVE] 结果已保存到: {output_path}")

    # 打印最有价值的发现
    print("\n" + "="*60)
    print("  最可能是题目 API 的请求 (按相关性排序):")
    print("="*60)
    for i, entry in enumerate(question_apis[:10]):
        print(f"\n  [{i+1}] score={entry['relevance_score']}")
        print(f"      {entry['method']} {entry['url'][:150]}")
        if "response_keys" in entry:
            print(f"      响应字段: {entry['response_keys']}")
        if "response_sample_keys" in entry:
            print(f"      数据字段: {entry['response_sample_keys']}")
        if "response_item_count" in entry.get("response_sample_keys", []):
            pass
        if entry.get("response_item_count"):
            print(f"      返回条数: {entry['response_item_count']}")

    # 保存登录状态
    if not STATE_FILE.exists():
        context.storage_state(path=str(STATE_FILE))
        print(f"\n[OK] 登录状态已保存到 {STATE_FILE}")

    context.close()
    if "browser" in dir():
        browser.close()
    playwright.stop()


if __name__ == "__main__":
    main()
