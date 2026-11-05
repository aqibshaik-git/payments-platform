#!/usr/bin/env bash
# Sun 8 Nov lab tests. Needs Postgres from ~/dev/infra (docker compose up -d postgres).
# Override with PGURL=postgres://user:pass@host:port/db
set -euo pipefail
cd "$(dirname "$0")"
PGURL="${PGURL:-postgres://dev:dev@localhost:5432/payments}"
# No psql on the Mac? Use the one inside the Postgres container.
if command -v psql >/dev/null; then PSQL=(psql "$PGURL")
else PSQL=(docker compose -f "$HOME/dev/infra/docker-compose.yml" exec -T postgres psql -U dev -d payments); fi
strip() { sed 's/--.*$//' "queries/$1.sql" | tr '\n' ' ' | sed 's/;[[:space:]]*$//'; }
{
  cat schema.sql fixture.sql test.sql
  cat <<SQL
SELECT pg_temp.same(\$q\$$(strip 01_running_balance)\$q\$, \$e\$VALUES (1::bigint,1::bigint,timestamptz '2026-03-01 09:00',-5000::bigint,-5000::bigint),(1,3,'2026-03-02 09:00',-2500,-7500),(1,6,'2026-03-03 09:00',1000,-6500),(1,7,'2026-03-05 09:00',-700,-7200),(1,12,'2026-04-01 09:00',400,-6800),(2,2,'2026-03-01 09:00',5000,5000),(2,5,'2026-03-03 09:00',-1000,4000),(2,10,'2026-03-06 09:00',3000,7000),(2,11,'2026-04-01 09:00',-400,6600),(3,4,'2026-03-02 09:00',2500,2500),(3,8,'2026-03-05 09:00',700,3200),(3,9,'2026-03-06 09:00',-3000,200)\$e\$, 'lab 01: running balance');
SELECT pg_temp.same(\$q\$$(strip 02_top_spenders)\$q\$, \$e\$VALUES (1::bigint,8200::bigint),(3,3000),(2,1000)\$e\$, 'lab 02: top spenders in March');
SELECT pg_temp.same(\$q\$$(strip 03_latest_per_account)\$q\$, \$e\$VALUES (1::bigint,12::bigint,timestamptz '2026-04-01 09:00',400::bigint),(2,11,'2026-04-01 09:00',-400),(3,9,'2026-03-06 09:00',-3000)\$e\$, 'drill 1: latest posting per account');
SELECT pg_temp.same(\$q\$$(strip 04_day_over_day)\$q\$, \$e\$VALUES (date '2026-03-01',5000::bigint,NULL::bigint),('2026-03-02',2500,-2500),('2026-03-03',1000,-1500),('2026-03-05',700,-300),('2026-03-06',3000,2300),('2026-04-01',400,-2600)\$e\$, 'drill 2: day-over-day volume');
SELECT pg_temp.same(\$q\$$(strip 05_activity_streaks)\$q\$, \$e\$VALUES (1::bigint,date '2026-03-01',date '2026-03-03',3),(1,'2026-03-05','2026-03-05',1),(1,'2026-04-01','2026-04-01',1),(2,'2026-03-01','2026-03-01',1),(2,'2026-03-03','2026-03-03',1),(2,'2026-03-06','2026-03-06',1),(2,'2026-04-01','2026-04-01',1),(3,'2026-03-02','2026-03-02',1),(3,'2026-03-05','2026-03-06',2)\$e\$, 'drill 3: activity streaks');
SQL
} | { echo "SET timezone = 'UTC';"; cat; } | "${PSQL[@]}" -q -v ON_ERROR_STOP=1 2>&1 | grep -E 'PASS|FAIL|ERROR' | sed 's/^.*NOTICE:  //; s/^psql:[^ ]* //'
