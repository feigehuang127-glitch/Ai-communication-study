"""
格式转换工具
============
将 questions.json 转换为 Java 项目可用的格式。
输出为适合导入 game 项目的 CSV 文件。

用法：
    python convert.py questions.json questions.csv
"""

import csv
import json
import sys


def convert(input_file, output_file):
    with open(input_file, "r", encoding="utf-8") as f:
        questions = json.load(f)

    with open(output_file, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["id", "question", "option_a", "option_b", "option_c", "option_d",
                          "answer", "analysis"])

        for q in questions:
            options = q.get("options", [])
            row = [
                q.get("id", ""),
                q.get("question", ""),
                options[0] if len(options) > 0 else "",
                options[1] if len(options) > 1 else "",
                options[2] if len(options) > 2 else "",
                options[3] if len(options) > 3 else "",
                q.get("answer", ""),
                q.get("analysis", ""),
            ]
            writer.writerow(row)

    print(f"[OK] 已转换 {len(questions)} 题 → {output_file}")


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("用法: python convert.py <input.json> <output.csv>")
        sys.exit(1)
    convert(sys.argv[1], sys.argv[2])
