#!/usr/bin/env bash
# Sun 15 Nov · row store vs column store, measured. Exports postings to CSV and Parquet, then runs the
# same aggregate in DuckDB on each. Needs uv (DuckDB is fetched on the fly, nothing to install).
set -euo pipefail
cd "$(dirname "$0")"
if command -v psql >/dev/null; then PSQL=(psql "${PGURL:-postgres://dev:dev@localhost:5432/payments}")
else PSQL=(docker compose -f "$HOME/dev/infra/docker-compose.yml" exec -T postgres psql -U dev -d payments); fi
"${PSQL[@]}" -q -c "\copy (SELECT * FROM ledger.postings) TO STDOUT WITH CSV HEADER" > postings.csv
uv run --quiet --with duckdb python - <<'PY'
import duckdb, os, time
con = duckdb.connect()
con.execute("COPY (SELECT * FROM 'postings.csv') TO 'postings.parquet' (FORMAT parquet)")
for f in ("postings.csv", "postings.parquet"):
    t = time.perf_counter()
    con.execute(f"SELECT account_id, sum(amount_minor) FROM '{f}' GROUP BY account_id").fetchall()
    print(f"{f:18} {os.path.getsize(f)/1e6:7.1f} MB   group-by in {1000*(time.perf_counter()-t):6.1f} ms")
print(con.execute("SELECT path_in_schema, compression, total_compressed_size FROM parquet_metadata('postings.parquet') LIMIT 6").fetchall())
PY
