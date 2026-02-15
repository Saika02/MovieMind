import argparse
import csv
import sys
from typing import Iterable, List, Tuple


def get_connection_dsn() -> str:
    host = "127.0.0.1"
    port = "5432"
    dbname = "moviemind"
    user = "moviemind"
    password = "moviemind_pwd"
    return f"host={host} port={port} dbname={dbname} user={user} password={password}"


def normalize_value(value: str):
    if value is None:
        return None
    text = str(value).strip()
    if text == "":
        return None
    return text


def iter_rows(csv_path: str) -> Iterable[Tuple]:
    with open(csv_path, "r", encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            yield (
                normalize_value(row.get("id")),
                normalize_value(row.get("title")),
                normalize_value(row.get("overview")),
                normalize_value(row.get("genres")),
                normalize_value(row.get("keywords")),
                normalize_value(row.get("cast")),
                normalize_value(row.get("producers")),
                normalize_value(row.get("release_date")),
                normalize_value(row.get("runtime")),
                normalize_value(row.get("production_companies")),
                normalize_value(row.get("vote_average")),
                normalize_value(row.get("vote_count")),
                normalize_value(row.get("tagline")),
                normalize_value(row.get("poster_file")),
            )


def chunked(iterable: Iterable[Tuple], size: int) -> Iterable[List[Tuple]]:
    batch: List[Tuple] = []
    for item in iterable:
        batch.append(item)
        if len(batch) >= size:
            yield batch
            batch = []
    if batch:
        yield batch


def main():
    parser = argparse.ArgumentParser(description="Import movies CSV into PostgreSQL.")
    parser.add_argument(
        "--csv",
        default=r"d:\JavaTotal\MovieMind\pre\tmdb_movies_processed_translated_with_posters.csv",
    )
    parser.add_argument("--table", default="movies")
    parser.add_argument("--batch-size", type=int, default=500)
    parser.add_argument("--on-conflict", choices=["update", "skip"], default="update")
    args = parser.parse_args()

    try:
        import psycopg2
        from psycopg2.extras import execute_values
    except Exception:
        print("缺少 psycopg2，先执行: pip install psycopg2-binary")
        sys.exit(1)

    dsn = get_connection_dsn()

    columns = [
        "tmdb_id",
        "title",
        "overview",
        "genres",
        "keywords",
        "cast_list",
        "producers",
        "release_date",
        "runtime",
        "production_companies",
        "tmdb_vote_average",
        "tmdb_vote_count",
        "tagline",
        "poster_file",
    ]

    conflict_sql = ""
    if args.on_conflict == "update":
        update_cols = [c for c in columns if c not in {"tmdb_id"}]
        sets = ", ".join([f"{c} = EXCLUDED.{c}" for c in update_cols])
        conflict_sql = f" ON CONFLICT (tmdb_id) DO UPDATE SET {sets}"
    else:
        conflict_sql = " ON CONFLICT (tmdb_id) DO NOTHING"

    insert_sql = (
        f"INSERT INTO {args.table} ({', '.join(columns)}) VALUES %s{conflict_sql}"
    )

    total = 0
    with psycopg2.connect(dsn) as conn:
        with conn.cursor() as cur:
            for batch in chunked(iter_rows(args.csv), args.batch_size):
                execute_values(cur, insert_sql, batch, page_size=len(batch))
                total += len(batch)
                print(f"Inserted: {total}")
        conn.commit()

    print(f"Done. Total rows processed: {total}")


if __name__ == "__main__":
    main()
