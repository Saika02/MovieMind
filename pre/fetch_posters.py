import csv
import json
import os
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent
DEFAULT_INPUT = BASE_DIR / "tmdb_movies_processed_translated.csv"
DEFAULT_OUTPUT = BASE_DIR / "tmdb_movies_processed_translated_with_posters.csv"
DEFAULT_POSTER_DIR = BASE_DIR / "posters"
IMAGE_BASE = "https://image.tmdb.org/t/p"
IMAGE_SIZE = "w500"
API_BASE = "https://api.themoviedb.org/3/movie"


def fetch_json(url):
    with urllib.request.urlopen(url, timeout=20) as resp:
        return json.loads(resp.read().decode("utf-8"))


def download_file(url, target_path):
    with urllib.request.urlopen(url, timeout=30) as resp:
        data = resp.read()
    target_path.write_bytes(data)


def build_movie_url(movie_id, api_key):
    params = urllib.parse.urlencode({"api_key": api_key})
    return f"{API_BASE}/{movie_id}?{params}"


def build_poster_url(poster_path):
    if not poster_path:
        return ""
    return f"{IMAGE_BASE}/{IMAGE_SIZE}{poster_path}"


def main():
    api_key = os.environ.get("TMDB_API_KEY", "").strip()
    if not api_key:
        print("TMDB_API_KEY 未设置")
        sys.exit(1)

    input_path = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_INPUT
    output_path = Path(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_OUTPUT
    poster_dir = Path(sys.argv[3]) if len(sys.argv) > 3 else DEFAULT_POSTER_DIR
    poster_dir.mkdir(parents=True, exist_ok=True)

    with input_path.open("r", encoding="utf-8-sig", newline="") as f_in:
        reader = csv.DictReader(f_in)
        fieldnames = list(reader.fieldnames or [])
        for extra in ["poster_path", "poster_url", "poster_file"]:
            if extra not in fieldnames:
                fieldnames.append(extra)

        rows = list(reader)

    updated = []
    for row in rows:
        movie_id = (row.get("id") or "").strip()
        if not movie_id:
            row["poster_path"] = ""
            row["poster_url"] = ""
            row["poster_file"] = ""
            updated.append(row)
            continue

        try:
            data = fetch_json(build_movie_url(movie_id, api_key))
            poster_path = data.get("poster_path") or ""
        except Exception:
            poster_path = ""

        poster_url = build_poster_url(poster_path)
        poster_file = ""

        if poster_url:
            suffix = Path(poster_path).suffix or ".jpg"
            poster_file = str((poster_dir / f"{movie_id}{suffix}").resolve())
            try:
                download_file(poster_url, Path(poster_file))
            except Exception:
                poster_file = ""
                poster_url = ""
                poster_path = ""

        row["poster_path"] = poster_path
        row["poster_url"] = poster_url
        row["poster_file"] = poster_file
        updated.append(row)
        time.sleep(0.2)

    with output_path.open("w", encoding="utf-8-sig", newline="") as f_out:
        writer = csv.DictWriter(f_out, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(updated)

    print(f"Saved CSV: {output_path}")
    print(f"Posters dir: {poster_dir}")


if __name__ == "__main__":
    main()
