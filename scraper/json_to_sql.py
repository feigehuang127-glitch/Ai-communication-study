"""
JSON 题库 → SQL INSERT 生成器
==============================
将 parse_pdf.py 生成的 JSON 文件转为 MySQL INSERT 语句，
同时生成 ALTER TABLE 语句扩展 category ENUM。

用法：
    python json_to_sql.py [data_dir] [output.sql]
"""

import json
import re
import sys
from pathlib import Path

TYPE_MAP = {
    'single_choice': '单选',
    'multi_choice': '多选',
    'true_false': '判断',
}


def escape_sql(s: str) -> str:
    """转义 SQL 字符串"""
    return s.replace('\\', '\\\\').replace("'", "\\'")


def generate_sql(json_files: list[Path]) -> tuple[list[str], list[str], dict]:
    """生成 ALTER 和 INSERT 语句"""
    alter_statements = []
    insert_statements = []
    stats = {}

    for json_file in json_files:
        # 从文件名提取分类名
        category = json_file.stem  # e.g., "通信原理", "数据通信网"

        with open(json_file, 'r', encoding='utf-8') as f:
            questions = json.load(f)

        count = 0
        for q in questions:
            qtype = TYPE_MAP.get(q['type'], '单选')
            content = escape_sql(q['question'])
            explanation = escape_sql(q.get('analysis', '') or '')

            # 选项映射
            options = q.get('options', [])
            opt_map = {}
            for opt in options:
                opt_map[opt['label']] = escape_sql(opt['text'])

            if qtype == '判断':
                option_a = '正确'
                option_b = '错误'
                option_c = 'NULL'
                option_d = 'NULL'
            else:
                option_a = f"'{opt_map.get('A', '')}'"
                option_b = f"'{opt_map.get('B', '')}'"
                option_c = f"'{opt_map.get('C', '')}'" if opt_map.get('C') else 'NULL'
                option_d = f"'{opt_map.get('D', '')}'" if opt_map.get('D') else 'NULL'

            answer = q['answer']
            difficulty = 1  # PDF 没有难度数据，默认 1

            sql = (
                f"INSERT INTO questions "
                f"(category, type, content, option_a, option_b, option_c, option_d, answer, explanation, difficulty) VALUES "
                f"('{escape_sql(category)}', '{qtype}', '{content}', "
                f"{option_a}, {option_b}, {option_c}, {option_d}, "
                f"'{answer}', '{explanation}', {difficulty});"
            )
            insert_statements.append(sql)
            count += 1

        stats[category] = count

    # 收集所有分类，生成 ALTER TABLE
    all_categories = set(stats.keys())
    cats_str = ", ".join(f"'{c}'" for c in sorted(all_categories))
    alter_statements.append(
        f"ALTER TABLE questions MODIFY COLUMN category "
        f"ENUM({cats_str}) NOT NULL;"
    )

    return alter_statements, insert_statements, stats


def main():
    data_dir = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).parent.parent / 'data'
    output_file = sys.argv[2] if len(sys.argv) > 2 else str(Path(__file__).parent.parent / 'sql' / 'import_questions.sql')

    json_files = sorted(data_dir.glob('*.json'))
    if not json_files:
        print(f"未找到 JSON 文件: {data_dir}")
        sys.exit(1)

    alter_stmts, insert_stmts, stats = generate_sql(json_files)

    # 生成 SQL 文件
    lines = [
        "-- ============================================",
        "-- 题库导入 SQL（自动生成）",
        f"-- 生成时间: 由 json_to_sql.py 生成",
        f"-- 题目总数: {sum(stats.values())}",
        "-- ============================================",
        "",
        "SET NAMES utf8mb4;",
        "",
        "-- 扩展 category ENUM（如需要）",
    ]
    lines.extend(alter_stmts)
    lines.append("")
    lines.append("-- 插入题目数据")
    lines.append("")

    # 按分类分组
    current_cat = None
    for sql in insert_stmts:
        # 提取分类名来分组
        m = re.search(r"category, type, content.*?'([^']+)'", sql)
        cat = m.group(1) if m else ''
        if cat != current_cat:
            current_cat = cat
            lines.append(f"-- {cat}")
        lines.append(sql)

    lines.append("")
    lines.append(f"-- 总计导入 {sum(stats.values())} 道题目")

    Path(output_file).parent.mkdir(parents=True, exist_ok=True)
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(lines))

    print(f"已生成: {output_file}")
    print(f"共 {sum(stats.values())} 道题目:")
    for cat, cnt in stats.items():
        print(f"  {cat}: {cnt} 题")


if __name__ == '__main__':
    main()
