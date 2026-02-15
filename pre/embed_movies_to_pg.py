import argparse
import os
import sys
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from http import HTTPStatus
from typing import Iterable, List, Tuple


def get_connection_dsn() -> str:
    host = "127.0.0.1"
    port = "5432"
    dbname = "moviemind"
    user = "moviemind"
    password = "moviemind_pwd"
    return f"host={host} port={port} dbname={dbname} user={user} password={password}"


def normalize_text(value: str) -> str:
    if value is None:
        return ""
    text = str(value).strip()
    return text


def build_combined_text(row) -> str:
    parts = [
        normalize_text(row[1]),
        normalize_text(row[2]),
        normalize_text(row[3]),
        normalize_text(row[4]),
        normalize_text(row[5]),
        normalize_text(row[6]),
        normalize_text(row[7]),
    ]
    parts = [p for p in parts if p]
    return " ".join(parts)


def chunked(items: List[Tuple], size: int) -> Iterable[List[Tuple]]:
    for i in range(0, len(items), size):
        yield items[i : i + size]


def expected_counts(rows: List[Tuple]) -> dict:
    counts = {"title": 0, "overview": 0, "combined": 0}
    for row in rows:
        title_text = normalize_text(row[1])
        overview_text = normalize_text(row[2])
        combined_text = build_combined_text(row)
        if title_text:
            counts["title"] += 1
        if overview_text:
            counts["overview"] += 1
        if combined_text:
            counts["combined"] += 1
    return counts


def embed_texts(model: str, texts: List[str]):
    import dashscope

    if len(texts) > 10:
        raise RuntimeError("Embedding batch size must be <= 10")

    resp = dashscope.TextEmbedding.call(model=model, input=texts)
    if resp.status_code != HTTPStatus.OK:
        raise RuntimeError(f"Embedding failed: {resp.code} {resp.message}")
    embeddings = resp.output["embeddings"]
    embeddings = sorted(embeddings, key=lambda x: x["text_index"])
    return [item["embedding"] for item in embeddings]


def main():
    parser = argparse.ArgumentParser(description="Embed movie fields into PostgreSQL.")
    parser.add_argument("--table", default="movie_embeddings")
    parser.add_argument("--batch-size", type=int, default=10)
    parser.add_argument("--sleep", type=float, default=0.2)
    parser.add_argument("--limit", type=int, default=0)
    parser.add_argument("--mode", choices=["replace", "skip"], default="replace")
    parser.add_argument("--check-only", action="store_true")
    parser.add_argument("--workers", type=int, default=4)
    args = parser.parse_args()

    api_key = os.environ.get("DASHSCOPE_API_KEY", "").strip()
    if not api_key:
        print("DASHSCOPE_API_KEY 未设置")
        sys.exit(1)

    try:
        import psycopg2
        from psycopg2.extras import execute_values
    except Exception:
        print("缺少 psycopg2，先执行: pip install psycopg2-binary")
        sys.exit(1)

    import dashscope

    dashscope.api_key = api_key

    dsn = get_connection_dsn()
    model_name = "text-embedding-v4"

    content_types = {
        "title": 1,
        "overview": 2,
        "combined": 3,
    }

    total_inserted = 0
    total_pending = 0

    with psycopg2.connect(dsn) as conn:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT id, title, overview, genres, keywords, cast_list, producers, tagline FROM movies ORDER BY id"
            )
            rows = cur.fetchall()

        pending = []
        print("开始读取电影数据")
        for row in rows:
            movie_id = row[0]
            title_text = normalize_text(row[1])
            overview_text = normalize_text(row[2])
            combined_text = build_combined_text(row)

            if title_text:
                pending.append((movie_id, "title", title_text))
            if overview_text:
                pending.append((movie_id, "overview", overview_text))
            if combined_text:
                pending.append((movie_id, "combined", combined_text))

            if args.limit and len(pending) >= args.limit:
                break
            if len(pending) % 1000 == 0:
                print(f"已准备待向量化文本: {len(pending)}")

        total_pending = len(pending)
        print(f"读取完成，待向量化文本总数: {total_pending}")

        if not args.check_only:
            with conn.cursor() as write_cur:
                batch_index = 0
                for batch in chunked(pending, args.batch_size):
                    batch_index += 1
                    type_to_items = {}
                    for movie_id, kind, text in batch:
                        type_to_items.setdefault(kind, []).append((movie_id, text))

                    rows_to_insert = []
                    tasks = []
                    for kind, items in type_to_items.items():
                        for sub_items in chunked(items, 10):
                            texts = [t for _, t in sub_items]
                            tasks.append((kind, sub_items, texts))

                    with ThreadPoolExecutor(max_workers=args.workers) as executor:
                        future_map = {
                            executor.submit(embed_texts, model_name, texts): (kind, sub_items)
                            for kind, sub_items, texts in tasks
                        }
                        for future in as_completed(future_map):
                            kind, sub_items = future_map[future]
                            vectors = future.result()
                            for (movie_id, _), vector in zip(sub_items, vectors):
                                vector_text = "[" + ",".join(str(v) for v in vector) + "]"
                                rows_to_insert.append(
                                    (
                                        movie_id,
                                        content_types[kind],
                                        None,
                                        vector_text,
                                        model_name,
                                    )
                                )

                    if not rows_to_insert:
                        continue

                    pairs = list({(row[0], row[1]) for row in rows_to_insert})

                    if args.mode == "replace":
                        execute_values(
                            write_cur,
                            f"DELETE FROM {args.table} WHERE (movie_id, content_type) IN (VALUES %s)",
                            pairs,
                        )
                    else:
                        execute_values(
                            write_cur,
                            f"SELECT movie_id, content_type FROM {args.table} WHERE (movie_id, content_type) IN (VALUES %s)",
                            pairs,
                        )
                        existed = {(row[0], row[1]) for row in write_cur.fetchall()}
                        before = len(rows_to_insert)
                        rows_to_insert = [
                            row for row in rows_to_insert if (row[0], row[1]) not in existed
                        ]
                        skipped = before - len(rows_to_insert)
                        if skipped:
                            print(f"批次 {batch_index} 跳过已存在 {skipped} 条")

                    insert_sql = (
                        f"INSERT INTO {args.table} (movie_id, content_type, content_id, embedding, model_name) VALUES %s"
                    )
                    if rows_to_insert:
                        execute_values(write_cur, insert_sql, rows_to_insert, page_size=len(rows_to_insert))
                        total_inserted += len(rows_to_insert)
                        conn.commit()
                        print(
                            f"批次 {batch_index} 提交完成，累计写入 {total_inserted}/{total_pending}"
                        )
                        time.sleep(args.sleep)
                    else:
                        print(f"批次 {batch_index} 无需写入")

        exp = expected_counts(rows)
        print(
            f"期望数量 title={exp['title']} overview={exp['overview']} combined={exp['combined']} total={exp['title'] + exp['overview'] + exp['combined']}"
        )
        with conn.cursor() as cur:
            cur.execute(
                f"SELECT content_type, COUNT(*) FROM {args.table} WHERE model_name = %s GROUP BY content_type ORDER BY content_type",
                (model_name,),
            )
            actual = {row[0]: row[1] for row in cur.fetchall()}
            total_actual = sum(actual.values())
            print(
                f"实际数量 total={total_actual} title={actual.get(1, 0)} overview={actual.get(2, 0)} combined={actual.get(3, 0)}"
            )

            cur.execute(
                f"""
                SELECT COUNT(*)
                FROM movies m
                LEFT JOIN {args.table} e
                  ON e.movie_id = m.id AND e.content_type = 1 AND e.model_name = %s
                WHERE COALESCE(TRIM(m.title), '') <> '' AND e.movie_id IS NULL
                """,
                (model_name,),
            )
            missing_title = cur.fetchone()[0]
            cur.execute(
                f"""
                SELECT COUNT(*)
                FROM movies m
                LEFT JOIN {args.table} e
                  ON e.movie_id = m.id AND e.content_type = 2 AND e.model_name = %s
                WHERE COALESCE(TRIM(m.overview), '') <> '' AND e.movie_id IS NULL
                """,
                (model_name,),
            )
            missing_overview = cur.fetchone()[0]
            cur.execute(
                f"""
                SELECT COUNT(*)
                FROM movies m
                LEFT JOIN {args.table} e
                  ON e.movie_id = m.id AND e.content_type = 3 AND e.model_name = %s
                WHERE COALESCE(NULLIF(TRIM(m.title), ''),
                              NULLIF(TRIM(m.overview), ''),
                              NULLIF(TRIM(m.genres), ''),
                              NULLIF(TRIM(m.keywords), ''),
                              NULLIF(TRIM(m.cast_list), ''),
                              NULLIF(TRIM(m.producers), ''),
                              NULLIF(TRIM(m.tagline), '')) IS NOT NULL
                  AND e.movie_id IS NULL
                """,
                (model_name,),
            )
            missing_combined = cur.fetchone()[0]
            print(
                f"缺失数量 title={missing_title} overview={missing_overview} combined={missing_combined}"
            )

    print(f"Done. Total embeddings inserted: {total_inserted}")


if __name__ == "__main__":
    main()
