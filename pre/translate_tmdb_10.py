from pathlib import Path
import os
import json
import re
import time
import pandas as pd
import dashscope
from dashscope import Generation

base_dir = Path(__file__).resolve().parent
input_path = base_dir / "tmdb_movies_processed.csv"
output_path = base_dir / "tmdb_movies_processed_translated.csv"
limit = None

translate_columns = [
    "title",
    "overview",
    "tagline",
    "genres",
    "keywords",
    "production_countries",
    "spoken_languages"
]

def is_cjk(text):
    return re.search(r"[\u4e00-\u9fff]", text) is not None

def is_url(text):
    return re.search(r"https?://", text, re.IGNORECASE) is not None

def is_numeric_like(text):
    return re.fullmatch(r"[\s\d\.\-:]+", text) is not None

def should_translate(text):
    if text is None:
        return False
    value = str(text).strip()
    if value == "":
        return False
    if is_url(value):
        return False
    if is_cjk(value):
        return False
    if is_numeric_like(value):
        return False
    return True

def extract_json(text):
    try:
        return json.loads(text)
    except Exception:
        match = re.search(r"\{[\s\S]*\}", text)
        if not match:
            raise
        return json.loads(match.group(0))

def get_message_content(response):
    output = None
    if hasattr(response, "output"):
        output = response.output
    elif isinstance(response, dict):
        output = response.get("output")
    if isinstance(output, dict) and "choices" in output:
        choices = output.get("choices") or []
        if choices:
            message = choices[0].get("message") or {}
            return message.get("content", "")
    if isinstance(response, dict):
        if "output_text" in response:
            return response.get("output_text", "")
    return ""

def translate_payload(payload, field_names):
    system_prompt = (
        "你是翻译引擎。仅翻译以下字段的值为中文，其他字段即使出现也原样返回："
        f"{', '.join(field_names)}。只返回JSON对象，保持键名完全不变。"
        "若值不需要翻译或无法翻译，原样返回。"
        "如果值中包含'|'分隔符，逐项翻译并保留'|'分隔符。"
    )
    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": json.dumps(payload, ensure_ascii=False)}
    ]
    response = Generation.call(
        model="qwen-plus",
        messages=messages,
        result_format="message"
    )
    content = get_message_content(response).strip()
    if not content:
        raise ValueError("Empty response from model")
    return extract_json(content)

def main():
    api_key = os.getenv("DASHSCOPE_API_KEY")
    if not api_key:
        raise RuntimeError("DASHSCOPE_API_KEY is not set")
    dashscope.api_key = api_key

    print(f"Input: {input_path}")
    print(f"Output: {output_path}")
    print(f"Limit: {limit if limit is not None else 'ALL'}")
    print(f"Translate columns: {', '.join(translate_columns)}")
    df = pd.read_csv(input_path).fillna("")
    df_head = df.head(limit).copy() if limit is not None else df.copy()

    total = len(df_head)
    translated_rows = 0
    for idx, row in df_head.iterrows():
        print(f"Processing row {idx + 1}/{total} (id={row.get('id', '')})")
        payload = {}
        for col in translate_columns:
            if col not in df_head.columns:
                continue
            value = row[col]
            if should_translate(value):
                payload[col] = str(value)
        if not payload:
            print("No translatable fields, skip")
            continue
        print(f"Translating fields: {', '.join(payload.keys())}")
        translated = translate_payload(payload, translate_columns)
        for col, value in translated.items():
            if col in df_head.columns and isinstance(value, str):
                df_head.at[idx, col] = value
        translated_rows += 1
        time.sleep(0.2)

    df_head.to_csv(output_path, index=False, encoding="utf-8-sig")
    print(f"Saved: {output_path}")
    print(f"Translated rows: {translated_rows}/{total}")

if __name__ == "__main__":
    main()
