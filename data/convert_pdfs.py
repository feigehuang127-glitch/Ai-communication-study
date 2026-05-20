"""Convert all PDF question files in this directory to JSON format.

Handles PDFs with embedded CID fonts where some Chinese characters may have
corrupted Unicode mappings. Parses by answer pattern + question numbering.
"""
import PyPDF2
import json
import re
import os
import glob


def clean_text(text: str) -> str:
    """Remove watermark footers and normalize whitespace."""
    # Remove watermark blocks
    text = re.sub(
        r'专业高校课程[^\n]*\n[^\n]*慧考网学员[^\n]*www\.huikao8\.com[^\n]*版权所有[^\n]*\d*',
        '', text
    )
    text = re.sub(r'www\.huikao8\.com.*?\d+', '', text)
    text = re.sub(r'慧考网学员.*?版权所有.*?\d*', '', text)
    text = re.sub(r'专业高校课程.*?一体化职业教育学习平台', '', text)
    text = text.replace('\r\n', '\n').replace('\r', '\n')
    text = re.sub(r'\n{3,}', '\n\n', text)
    return text.strip()


def extract_questions_from_text(full_text: str) -> list:
    """
    Parse all questions from full text.
    Uses question-number positions as block boundaries, then pairs each
    question with its answer. Analysis is the text between answer and the
    next question number.
    """
    # Find all question number positions: "\nNN．" or "\nNN."
    qnum_pattern = re.compile(r'\n\s*(\d+)[．.]\s*')
    qnum_matches = list(qnum_pattern.finditer(full_text))

    # Find all answer positions
    answer_pattern = re.compile(r'参考答案[：:]\s*(.+?)(?:\n|$)')
    answer_matches = list(answer_pattern.finditer(full_text))

    if not qnum_matches or not answer_matches:
        return []

    questions = []

    for idx, qnum_m in enumerate(qnum_matches):
        qnum_str = qnum_m.group(1)

        # Find the answer that belongs to this question.
        # The answer should be after the question number but before the next
        # question number (or end of text).
        q_block_start = qnum_m.start()
        next_q_start = qnum_matches[idx + 1].start() if idx + 1 < len(qnum_matches) else len(full_text)

        # Find answer within [q_block_start, next_q_start]
        ans_match = None
        for a in answer_matches:
            if q_block_start <= a.start() < next_q_start:
                ans_match = a
                break

        if not ans_match:
            continue  # orphan question number, skip

        answer_raw = ans_match.group(1).strip()

        # Question block: from question number line to the answer
        block = full_text[qnum_m.start():ans_match.start()]
        # Remove watermark text in all known forms
        block = re.sub(r'专业高校课程[^\n]*', '', block)
        block = re.sub(r'慧考网学员[^\n]*', '', block)
        block = re.sub(r'www\.huikao8\.com[^\n]*版权所有[^\n]*\d*', '', block)
        block = re.sub(r'全方位一体化职业教育学习平台[^\n]*', '', block)
        block = block.strip()

        # Analysis: from answer end to next question number
        analysis = ''
        analysis_start = ans_match.end()
        analysis_end = next_q_start
        analysis_text = full_text[analysis_start:analysis_end]

        # Clean analysis text - remove watermark lines first (before collapsing).
        # Many watermark characters are garbled, so we target intact fragments.
        analysis_text = re.sub(r'[^\n]*一体化[^\n]*学习平台[^\n]*\n?', '', analysis_text)
        analysis_text = re.sub(r'[^\n]*专业高校[^\n]*\n?', '', analysis_text)
        analysis_text = re.sub(r'[^\n]*慧考网[^\n]*\n?', '', analysis_text)
        analysis_text = re.sub(r'[^\n]*www\.[^\n]*版权所有[^\n]*\n?', '', analysis_text)
        analysis_text = re.sub(r'[^\n]*版权所有[^\n]*\d+[^\n]*\n?', '', analysis_text)
        # Now collapse whitespace
        # Collapse whitespace
        analysis_text = re.sub(r'\s+', ' ', analysis_text).strip()

        # Strip garbled prefix: remove everything before the first real Chinese
        # or alphanumeric content. The garbled "参考解析详见" prefix often maps to
        # replacement characters in the BMP that are not valid CJK.
        analysis_text = re.sub(r'^[^一-鿿a-zA-Z0-9]+', '', analysis_text)
        # Also try to remove a partially-readable "参考解析" prefix
        analysis_text = re.sub(r'^(?:参考解析[详见]*[：:]\s*)+', '', analysis_text)
        # Remove trailing section header spill
        analysis_text = re.sub(r'\s*(?:单选题|多选题|判断题|多项选择题)\s*$', '', analysis_text)
        analysis_text = analysis_text.strip()

        # Only keep if it has real Chinese content (≥5 chars)
        if analysis_text:
            chinese_count = len(re.findall(r'[一-鿿]', analysis_text))
            if chinese_count >= 5:
                analysis = analysis_text

        q = parse_question_block(block, answer_raw)
        if q:
            q['analysis'] = analysis
            questions.append(q)

    # Re-number sequentially
    for i, q in enumerate(questions):
        q['id'] = i + 1

    return questions


def parse_question_block(block: str, answer_raw: str) -> dict | None:
    """Parse a single question's text block into structured data."""
    if not block.strip():
        return None

    lines = block.strip().split('\n')

    # Remove garbled chapter/section header remnants
    # Pattern: anything before the first question number
    cleaned_lines = []
    found_start = False
    for line in lines:
        stripped = line.strip()
        if not stripped:
            cleaned_lines.append(stripped)
            continue

        # Skip chapter headers (garbled text with 章 or standalone numbers)
        if re.match(r'^[������]*\d+[����������������]*$', stripped):
            continue
        # Skip section header remnants (孤立的选择题类型文字)
        if stripped in ('单选题', '多选题', '判断题', '多项选择题',
                         '选择题', '填空题', '简答题'):
            continue

        if not found_start:
            # A valid question line starts with number + dot + text
            if re.match(r'^\d+[．.]\s*\S', stripped):
                found_start = True
                # Strip the question number prefix for the question text
                qtext = re.sub(r'^\d+[．.]\s*', '', stripped)
                cleaned_lines.append(qtext)
                continue
            # Skip lines before the first question number
            continue

        cleaned_lines.append(stripped)

    if not cleaned_lines:
        return None

    return parse_cleaned_lines(cleaned_lines, answer_raw)


def parse_cleaned_lines(lines: list, answer_raw: str) -> dict | None:
    """Parse already-cleaned lines into question dict."""
    question_lines = []
    options = []
    in_options = False
    current_label = None
    current_opt_lines = []

    for stripped in lines:
        if not stripped:
            continue

        # Option detection: A., B., C., D.
        opt_match = re.match(r'([A-D])[．.]\s*(.*)', stripped)
        if opt_match:
            if current_label:
                options.append({
                    'label': current_label,
                    'text': ' '.join(current_opt_lines).strip()
                })
            current_label = opt_match.group(1)
            text_part = opt_match.group(2).strip()
            current_opt_lines = [text_part] if text_part else []
            in_options = True
            continue

        if in_options and current_label:
            current_opt_lines.append(stripped)
        else:
            question_lines.append(stripped)

    # Save last option
    if current_label:
        options.append({
            'label': current_label,
            'text': ' '.join(current_opt_lines).strip()
        })

    question_text = ' '.join(question_lines).strip()
    # Remove any watermark remnants that slipped into question text
    question_text = re.sub(r'专业高校课程[^，。；]*', '', question_text)
    question_text = re.sub(r'慧考网学员[^，。；]*', '', question_text)
    question_text = re.sub(r'www\.huikao8\.com[^，。；]*版权所有[^，。；]*\d*', '', question_text)
    question_text = re.sub(r'全方位一体化职业教育学习平台[^，。；]*', '', question_text)
    question_text = question_text.strip()
    if not question_text:
        return None

    # Determine type from answer value
    answer_stripped = answer_raw.strip()
    answer_norm = answer_stripped.replace(' ', '').upper()

    # Judge questions
    if answer_stripped in ('对', '正确', '√'):
        return {
            'type': 'judge',
            'question': question_text,
            'options': [
                {'label': 'A', 'text': '正确'},
                {'label': 'B', 'text': '错误'}
            ],
            'answer': 'A',
        }
    if answer_stripped in ('错', '错误', '×'):
        return {
            'type': 'judge',
            'question': question_text,
            'options': [
                {'label': 'A', 'text': '正确'},
                {'label': 'B', 'text': '错误'}
            ],
            'answer': 'B',
        }

    # Multi-choice: 2+ letters
    if len(answer_norm) >= 2 and all(c in 'ABCD' for c in answer_norm):
        qtype = 'multi_choice'
    elif len(answer_norm) == 1 and answer_norm in 'ABCD':
        qtype = 'single_choice'
    else:
        qtype = 'single_choice'

    return {
        'type': qtype,
        'question': question_text,
        'options': options,
        'answer': answer_norm,
    }


def extract_category(text: str, filename: str) -> str:
    """Derive category from filename (most reliable) with text fallback."""
    base = os.path.splitext(filename)[0]
    # Remove trailing digits and separators
    base_clean = re.sub(r'[\d\-_]+$', '', base).strip()

    # Known category mapping from filename prefix
    filename_map = [
        ('通信原理', '通信原理'),
        ('数据通信网', '数据通信网'),
        ('光纤传输', '光纤传输'),
        ('宽带接入技术', '宽带接入技术'),
        ('现代交换技术', '现代交换技术'),
        ('信息通信新技术', '信息通信新技术'),
    ]

    for prefix, cat_name in filename_map:
        if base_clean.startswith(prefix):
            return cat_name

    # Fallback: try to detect from readable text
    first_part = text[:300]
    for pattern, cat_name in filename_map:
        if pattern in first_part:
            return cat_name

    return base_clean if base_clean else '未分类'


def process_pdf(pdf_path: str) -> list:
    """Process one PDF file, return list of question dicts."""
    fname = os.path.basename(pdf_path)
    print(f'  Reading: {fname}')
    reader = PyPDF2.PdfReader(pdf_path)

    all_text = ''
    for page in reader.pages:
        all_text += page.extract_text() + '\n'

    all_text = clean_text(all_text)
    category = extract_category(all_text, fname)

    questions = extract_questions_from_text(all_text)

    for q in questions:
        q['category'] = category

    return questions


def main():
    data_dir = os.path.dirname(os.path.abspath(__file__))
    pdf_files = sorted(glob.glob(os.path.join(data_dir, '*.pdf')))

    if not pdf_files:
        print('No PDF files found!')
        return

    print(f'Found {len(pdf_files)} PDF files to process.\n')

    total_questions = 0
    for pdf_path in pdf_files:
        try:
            questions = process_pdf(pdf_path)

            if not questions:
                print(f'    WARNING: No questions extracted!')
                continue

            base = os.path.splitext(pdf_path)[0]
            json_path = base + '.json'

            with open(json_path, 'w', encoding='utf-8') as f:
                json.dump(questions, f, ensure_ascii=False, indent=2)

            types = {}
            for q in questions:
                t = q['type']
                types[t] = types.get(t, 0) + 1

            type_summary = ', '.join(f'{k}: {v}' for k, v in sorted(types.items()))
            total_questions += len(questions)
            print(f'    → {os.path.basename(json_path)} ({len(questions)}: {type_summary})')

        except Exception as e:
            print(f'    ERROR processing {os.path.basename(pdf_path)}: {e}')
            import traceback
            traceback.print_exc()

    print(f'\nDone! {len(pdf_files)} PDFs → {total_questions} total questions extracted.')


if __name__ == '__main__':
    main()
