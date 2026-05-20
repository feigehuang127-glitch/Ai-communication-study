"""
会考吧(huikao8.com) 题库爬虫
=============================
使用 Playwright 浏览器自动化，支持登录态保持、断点续爬。

首次运行：
    python scraper.py --login

后续运行（复用登录态）：
    python scraper.py

查看页面结构（调试用）：
    python inspect.py
"""

import json
import os
import re
import sys
import time
import random
import hashlib
from pathlib import Path
from urllib.parse import urljoin, urlparse

from playwright.sync_api import sync_playwright, TimeoutError as PlaywrightTimeout

# ---------- 加载配置 ----------
try:
    from config import (
        BASE_URL, LOGIN_URL, START_URL, SELECTORS,
        MIN_DELAY, MAX_DELAY, OUTPUT_FILE, USE_SAVED_STATE, STATE_FILE
    )
except ImportError:
    print("[ERROR] 找不到 config.py，请确保在 scraper/ 目录下运行")
    sys.exit(1)

# ---------- 工具函数 ----------

def random_delay():
    """随机等待，降低被 ban 风险"""
    time.sleep(random.randint(MIN_DELAY, MAX_DELAY) / 1000.0)


def safe_text(page, selector, default=""):
    """安全获取元素文本"""
    try:
        el = page.query_selector(selector)
        return el.inner_text().strip() if el else default
    except Exception:
        return default


def safe_texts(page, selector):
    """安全获取多个元素文本列表"""
    try:
        return [el.inner_text().strip() for el in page.query_selector_all(selector)]
    except Exception:
        return []


def url_to_id(url):
    """将 URL 哈希为短 ID，用于去重"""
    return hashlib.md5(url.encode()).hexdigest()[:8]


def normalize_answer(text):
    """规范化答案文本：去除多余空白、统一大写"""
    return re.sub(r'\s+', '', text).upper()


# ---------- 核心逻辑 ----------

class QuizScraper:
    def __init__(self):
        self.playwright = None
        self.browser = None
        self.context = None
        self.page = None
        self.seen_ids = set()      # 已爬取的题目 ID
        self.questions = []        # 已爬取的题目数据
        self.total_pages = 0

    # --- 浏览器生命周期 ---

    def start(self):
        self.playwright = sync_playwright().start()
        state_path = Path(__file__).parent / STATE_FILE

        if USE_SAVED_STATE and state_path.exists():
            print(f"[INFO] 加载已保存的登录状态: {state_path}")
            self.context = self.playwright.chromium.launch_persistent_context(
                user_data_dir=str(Path(__file__).parent / "browser_data"),
                headless=False,
                viewport={"width": 1280, "height": 800},
            )
            self.page = self.context.pages[0] if self.context.pages else self.context.new_page()
        else:
            self.browser = self.playwright.chromium.launch(headless=False)
            self.context = self.browser.new_context(viewport={"width": 1280, "height": 800})
            self.page = self.context.new_page()

    def login(self):
        """手动登录：打开登录页，等待用户完成登录"""
        print(f"\n{'='*60}")
        print(f"  请在浏览器中手动登录")
        print(f"  登录完成后，回到终端按 Enter 继续")
        print(f"{'='*60}\n")

        self.page.goto(LOGIN_URL, wait_until="networkidle")

        # 尝试验证码提示
        print("[TIP] 如果有验证码，请在浏览器中手动完成验证")

        input("\n按 Enter 继续（确认已登录）...")

        # 保存登录状态
        state_path = Path(__file__).parent / STATE_FILE
        self.context.storage_state(path=str(state_path))
        print(f"[OK] 登录状态已保存到 {state_path}")

    # --- 题目解析 ---

    def parse_question_page(self, url):
        """解析单个题目页面，返回题目数据字典"""
        print(f"  [FETCH] {url}")
        try:
            self.page.goto(url, wait_until="domcontentloaded", timeout=15000)
        except PlaywrightTimeout:
            print(f"  [WARN] 页面加载超时: {url}")
            return None
        random_delay()

        # 提取题目正文
        question_text = safe_text(self.page, SELECTORS["question_body"])
        if not question_text:
            # 尝试其他常见选择器
            for sel in [".question", ".topic", ".content", "article", ".main-content"]:
                question_text = safe_text(self.page, sel)
                if question_text:
                    break

        # 提取选项
        options = safe_texts(self.page, SELECTORS["options"])
        if not options:
            for sel in ["li", ".option-item", ".choice"]:
                options = safe_texts(self.page, sel)
                if len(options) >= 2:
                    break

        # 提取正确答案
        answer = safe_text(self.page, SELECTORS["correct_answer"])
        if not answer:
            for sel in [".answer", ".key", ".right", "[data-answer]"]:
                answer = safe_text(self.page, sel)
                if answer:
                    break

        # 提取解析
        analysis = safe_text(self.page, SELECTORS["analysis"])

        return {
            "id": url_to_id(url),
            "url": url,
            "question": question_text,
            "options": options,
            "answer": normalize_answer(answer),
            "analysis": analysis,
        }

    # --- 列表页：收集题目链接 ---

    def collect_links_from_page(self):
        """从当前列表页收集所有题目详情链接"""
        links = set()
        selector = SELECTORS["question_links"]
        for el in self.page.query_selector_all(selector):
            href = el.get_attribute("href")
            if href:
                full_url = urljoin(self.page.url, href)
                parsed = urlparse(full_url)
                # 只保留同域名的链接
                if parsed.netloc == urlparse(BASE_URL).netloc:
                    links.add(full_url)
        return links

    def has_next_page(self):
        """检查列表页是否有下一页"""
        try:
            next_btn = self.page.query_selector(SELECTORS["next_page"])
            return next_btn is not None and next_btn.is_enabled()
        except Exception:
            return False

    def go_next_page(self):
        """点击下一页"""
        try:
            self.page.click(SELECTORS["next_page"])
            self.page.wait_for_load_state("domcontentloaded")
            return True
        except Exception:
            return False

    # --- 主爬取循环 ---

    def crawl(self, start_url=START_URL):
        """主入口：遍历列表页 → 题目详情页 → 提取数据"""
        print(f"\n[START] 从 {start_url} 开始爬取")
        self.page.goto(start_url, wait_until="networkidle")
        random_delay()

        while True:
            self.total_pages += 1
            current_url = self.page.url
            print(f"\n--- 第 {self.total_pages} 个列表页: {current_url} ---")

            # 收集当前列表页的所有题目链接
            links = self.collect_links_from_page()
            print(f"[INFO] 找到 {len(links)} 个题目链接")

            new_count = 0
            for link in sorted(links):
                qid = url_to_id(link)
                if qid in self.seen_ids:
                    continue

                data = self.parse_question_page(link)
                if data and data["question"]:
                    self.seen_ids.add(qid)
                    self.questions.append(data)
                    new_count += 1
                    print(f"    [{len(self.questions)}] {data['question'][:50]}...")
                else:
                    print(f"    [SKIP] 未能解析题目内容: {link}")

                # 每 50 题自动保存一次
                if len(self.questions) % 50 == 0:
                    self.save()

            print(f"[INFO] 本页新增 {new_count} 题，累计 {len(self.questions)} 题")

            # 检查是否有下一页
            if not self.has_next_page():
                print("\n[DONE] 没有更多页面，爬取完成！")
                break

            self.go_next_page()
            random_delay()

    def save(self, filename=None):
        """保存结果到 JSON 文件"""
        path = Path(__file__).parent / (filename or OUTPUT_FILE)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(self.questions, f, ensure_ascii=False, indent=2)
        print(f"[SAVE] 已保存 {len(self.questions)} 题 → {path}")

    def close(self):
        if self.context:
            self.context.close()
        if self.browser:
            self.browser.close()
        if self.playwright:
            self.playwright.stop()


# ============================================================
# CLI
# ============================================================

def main():
    scraper = QuizScraper()

    try:
        scraper.start()

        # 如果需要登录
        if "--login" in sys.argv:
            scraper.login()

        # 开始爬取
        scraper.crawl(START_URL)

        # 最终保存
        scraper.save()

    except KeyboardInterrupt:
        print("\n\n[STOP] 用户中断，保存已爬取的数据...")
        scraper.save()
    finally:
        scraper.close()
        print(f"\n[SUMMARY] 共爬取 {len(scraper.questions)} 道题目")
        print(f"[SUMMARY] 浏览了 {scraper.total_pages} 个列表页")


if __name__ == "__main__":
    main()
