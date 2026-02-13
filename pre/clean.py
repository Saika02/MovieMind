from pathlib import Path
import pandas as pd
import json
import ast

base_dir = Path(__file__).resolve().parent
movies_path = base_dir / "tmdb_5000_movies.csv"
credits_path = base_dir / "tmdb_5000_credits.csv"
output_path = base_dir / "tmdb_movies_processed.csv"

def parse_list(value):
    if pd.isna(value) or value == "":
        return []
    if isinstance(value, list):
        return value
    try:
        return json.loads(value)
    except Exception:
        try:
            return ast.literal_eval(value)
        except Exception:
            return []

def extract_names(items, limit=None, predicate=None, sort_key=None):
    if not items:
        return []
    if sort_key is not None:
        items = sorted(items, key=sort_key)
    result = []
    for item in items:
        if predicate is not None and not predicate(item):
            continue
        name = item.get("name")
        if name:
            result.append(name)
        if limit is not None and len(result) >= limit:
            break
    return result

movies = pd.read_csv(movies_path)
credits = pd.read_csv(credits_path)

movies["genres"] = movies["genres"].apply(parse_list)
movies["keywords"] = movies["keywords"].apply(parse_list)
movies["production_companies"] = movies["production_companies"].apply(parse_list)
movies["production_countries"] = movies["production_countries"].apply(parse_list)
movies["spoken_languages"] = movies["spoken_languages"].apply(parse_list)

credits["cast"] = credits["cast"].apply(parse_list)
credits["crew"] = credits["crew"].apply(parse_list)

df = movies.merge(credits, left_on="id", right_on="movie_id", how="left")

producer_jobs = {"Producer", "Executive Producer", "Co-Producer", "Associate Producer"}

df["genres"] = df["genres"].apply(lambda x: "|".join(extract_names(x)))
df["keywords"] = df["keywords"].apply(lambda x: "|".join(extract_names(x)))
df["production_companies"] = df["production_companies"].apply(lambda x: "|".join(extract_names(x)))
df["production_countries"] = df["production_countries"].apply(lambda x: "|".join(extract_names(x)))
df["spoken_languages"] = df["spoken_languages"].apply(lambda x: "|".join(extract_names(x)))

df["cast"] = df["cast"].apply(lambda x: "|".join(extract_names(x, limit=10, sort_key=lambda i: i.get("order", 0))))
df["producers"] = df["crew"].apply(
    lambda x: "|".join(
        extract_names(
            x,
            predicate=lambda i: i.get("job") in producer_jobs
        )
    )
)

columns = [
    "id",
    "title",
    "original_title",
    "overview",
    "genres",
    "keywords",
    "cast",
    "producers",
    "release_date",
    "runtime",
    "production_companies",
    "production_countries",
    "spoken_languages",
    "vote_average",
    "vote_count",
    "popularity",
    "tagline",
    "homepage",
    "budget",
    "revenue"
]

for col in columns:
    if col not in df.columns:
        df[col] = ""

df_out = df[columns].fillna("")
df_out.to_csv(output_path, index=False, encoding="utf-8-sig")
print(f"Saved: {output_path}")