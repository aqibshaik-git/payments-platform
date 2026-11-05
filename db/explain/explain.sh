#!/usr/bin/env bash
# Sun 15 Nov · show the three plans with real timings. Run once before indexes.sql and once after.
#   ./explain.sh            plans only
#   ./explain.sh --apply    apply indexes.sql first, then show the plans
# Needs the seeded ledger from Sun 8: ../run.sh
set -euo pipefail
cd "$(dirname "$0")"
if command -v psql >/dev/null; then PSQL=(psql "${PGURL:-postgres://dev:dev@localhost:5432/payments}")
else PSQL=(docker compose -f "$HOME/dev/infra/docker-compose.yml" exec -T postgres psql -U dev -d payments); fi
[ "${1:-}" = "--apply" ] && "${PSQL[@]}" -q -v ON_ERROR_STOP=1 < indexes.sql
grep -v '^--' hot_queries.sql | awk 'BEGIN{RS=";"} NF{print "EXPLAIN (ANALYZE, BUFFERS, COSTS OFF) " $0 ";"}' | "${PSQL[@]}" -q
