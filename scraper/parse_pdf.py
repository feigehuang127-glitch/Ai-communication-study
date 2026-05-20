"""
PDF 题库解析器
==============
将慧考智学导出的 PDF 题库解析为结构化 JSON。

支持的题型：单项选择题、多项选择题、判断题

用法：
    python parse_pdf.py <pdf_path> [output.json]
"""

import json
import re
import sys
from pathlib import Path

import fitz  # PyMuPDF


FOOTER_PATTERNS = [
    r'专业网校课程、题库软件、考试用书、资讯信息全方位一体化职业考试学习平台',
    r'慧考智学官网 www\.huikao8\.com 版权所有',
    r'^\d+$',  # 孤立的页码数字
]


def clean_text(text: str) -> str:
    """去除 PDF 页眉页脚和页码"""
    for pat in FOOTER_PATTERNS:
        text = re.sub(pat, '', text, flags=re.MULTILINE)
    # 合并多个空行
    text = re.sub(r'\n\s*\n\s*\n+', '\n\n', text)
    # 去除行首行尾空白
    text = '\n'.join(line.strip() for line in text.split('\n'))
    return text.strip()


def parse_questions(text: str) -> list[dict]:
    """解析全部题目"""
    questions = []

    # 按题型分段
    # 题型标记: "单项选择题", "多项选择题", "判断题"
    sections = re.split(r'(单项选择题|多项选择题|判断题)\s*\n', text)

    current_type = None
    for i, chunk in enumerate(sections):
        chunk = chunk.strip()
        if chunk == '单项选择题':
            current_type = 'single_choice'
        elif chunk == '多项选择题':
            current_type = 'multi_choice'
        elif chunk == '判断题':
            current_type = 'true_false'
        elif current_type and chunk:
            questions.extend(parse_section(chunk, current_type))

    return questions


def parse_section(text: str, qtype: str) -> list[dict]:
    """解析一个题型区域下的所有题目"""
    # 按题号分割
    # 匹配形如 "1." "15." 的数字开头行
    parts = re.split(r'(?=^\d+\.)', text, flags=re.MULTILINE)

    questions = []
    for part in parts:
        part = part.strip()
        if not part:
            continue

        q = parse_one_question(part, qtype)
        if q:
            questions.append(q)

    return questions


def parse_one_question(text: str, qtype: str) -> dict | None:
    """解析单道题目"""
    lines = text.split('\n')
    lines = [l.strip() for l in lines if l.strip()]

    if not lines:
        return None

    # 提取题号
    m = re.match(r'(\d+)\.\s*(.*)', lines[0])
    if not m:
        return None

    number = int(m.group(1))
    question_text = m.group(2).strip()

    # 判断题直接从编号后提取题目文本直到"参考答案"
    answer = ''
    analysis = ''
    options = []

    if qtype == 'true_false':
        # 判断题没有选项，题目文本延续到"参考答案"
        body_parts = []
        for line in lines[1:]:
            if line.startswith('参考答案'):
                answer_match = re.match(r'参考答案[：:]\s*(.*)', line)
                if answer_match:
                    answer = answer_match.group(1).strip()
                break
            body_parts.append(line)
        # 题目可能跨行
        if body_parts:
            question_text = question_text + ''.join(body_parts)

    elif qtype in ('single_choice', 'multi_choice'):
        # 收集选项和题干
        option_started = False
        body_parts = []
        option_lines = []

        for line in lines[1:]:
            if re.match(r'^[A-F][.．、]', line):
                option_started = True
                option_lines.append(line)
            elif line.startswith('参考答案'):
                answer_match = re.match(r'参考答案[：:]\s*(.*)', line)
                if answer_match:
                    answer = answer_match.group(1).strip()
                break
            elif line.startswith('【慧考解析】'):
                analysis = line[len('【慧考解析】'):].strip()
                break
            elif not option_started:
                body_parts.append(line)
            elif option_started and option_lines:
                # 选项可能跨行，追加到最后一条选项
                option_lines[-1] += line

        # 合并题干（选项之前的部分）
        if body_parts:
            question_text = question_text + ''.join(body_parts)

        # 解析选项
        for ol in option_lines:
            m = re.match(r'^([A-F])[.．、]\s*(.*)', ol)
            if m:
                options.append({"label": m.group(1), "text": m.group(2).strip()})

    # 提取【慧考解析】（如果还没提取到）
    if not analysis:
        analysis_match = re.search(r'【慧考解析】\s*(.*)', text)
        if analysis_match:
            analysis = analysis_match.group(1).strip()

    # 规范化答案
    answer = answer.replace(' ', '').upper()

    # 判断题答案统一
    if qtype == 'true_false':
        if answer in ('对', '正确', '√'):
            answer = 'A'
        elif answer in ('错', '错误', '×'):
            answer = 'B'

    # 清理题干中的（　）占位符
    question_text = re.sub(r'[（\(]\s*[　\s]*[）\)]', '（ ）', question_text)

    return {
        "id": number,
        "type": qtype,
        "question": question_text,
        "options": options,
        "answer": answer,
        "analysis": analysis,
    }


def main():
    if len(sys.argv) < 2:
        print("用法: python parse_pdf.py <pdf_path> [output.json]")
        sys.exit(1)

    pdf_path = sys.argv[1]
    output_path = sys.argv[2] if len(sys.argv) > 2 else Path(pdf_path).stem + '.json'

    # 读取 PDF
    doc = fitz.open(pdf_path)
    full_text = ''
    for page in doc:
        full_text += page.get_text() + '\n'

    # 清理
    cleaned = clean_text(full_text)

    # 解析
    questions = parse_questions(cleaned)

    # 统计
    type_counts = {}
    for q in questions:
        t = q['type']
        type_counts[t] = type_counts.get(t, 0) + 1

    print(f"共解析 {len(questions)} 道题目:")
    for t, c in type_counts.items():
        type_names = {'single_choice': '单选题', 'multi_choice': '多选题', 'true_false': '判断题'}
        print(f"  {type_names.get(t, t)}: {c} 题")

    # 保存
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(questions, f, ensure_ascii=False, indent=2)
    print(f"\n已保存到: {output_path}")


if __name__ == '__main__':
    main()
