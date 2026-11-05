#!/usr/bin/env bash
# Tue 10 Nov · hammer each function with 16 concurrent clients, then check the invariants.
# Needs Postgres from ~/dev/infra. Uses pgbench (inside the container if not on your Mac).
set -uo pipefail
cd "$(dirname "$0")"
URL="${PGURL:-postgres://dev:dev@localhost:5432/payments}"
if command -v psql >/dev/null && command -v pgbench >/dev/null; then
  PSQL=(psql "$URL" -qAt -v ON_ERROR_STOP=1); BENCH=(pgbench "$URL"); FILE() { echo "$1"; }
else
  DC=(docker compose -f "$HOME/dev/infra/docker-compose.yml" exec -T postgres)
  PSQL=("${DC[@]}" psql -U dev -d payments -qAt -v ON_ERROR_STOP=1); BENCH=("${DC[@]}" pgbench -U dev payments)
  FILE() { "${DC[@]}" sh -c "cat > /tmp/$(basename "$1")" < "$1"; echo "/tmp/$(basename "$1")"; }
fi
reset() { { echo "SET client_min_messages = warning;"; cat setup.sql mine.sql; } | "${PSQL[@]}" >/dev/null; }
bench() { local f; f=$(mktemp -t txbench.XXXX); echo "$1" > "$f"; "${BENCH[@]}" -n -c 16 -j 4 -t 150 -f "$(FILE "$f")" >/dev/null 2>&1; rm -f "$f"; }
check() { local name="$1" sql="$2" want="$3" expect_fail="${4:-}"; local got; got=$("${PSQL[@]}" -c "$sql")
  if [ "$got" = "$want" ]; then [ -n "$expect_fail" ] && echo "??    $name: no damage this run (try again)" || echo "PASS  $name"
  else [ -n "$expect_fail" ] && echo "BUG   $name: $got (expected $want) <- this is the lesson" || echo "FAIL  $name: got $got, expected $want"; fi; }
TOTAL="SELECT CASE WHEN count(*) FILTER (WHERE balance <> 10000) = 0 THEN 'nothing ran (still TODO?)' ELSE sum(balance)::text END FROM tx.balances"
SKEW="SELECT CASE WHEN (SELECT count(*) FROM tx.balances WHERE balance <> 10000) = 0 THEN 'nothing ran (still TODO?)' ELSE (SELECT count(*) FROM (SELECT owner FROM tx.balances GROUP BY owner HAVING sum(balance) < 0) x)::text END"
T='\set a random(1, 20)
\set b random(1, 20)
'
reset; bench "$T"'SELECT tx.transfer_naive(:a, :b, 500) WHERE :a <> :b;'
check "naive transfer keeps money constant" "$TOTAL" 200000 expect_fail
reset; bench "$T"'SELECT tx.transfer_locked(:a, :b, 500) WHERE :a <> :b;'
check "locked transfer keeps money constant" "$TOTAL" 200000
reset; bench "$T"'DO $$ BEGIN FOR i IN 1..50 LOOP EXIT WHEN tx.transfer_optimistic(:a, :b, 500); END LOOP; END $$;'
check "optimistic transfer keeps money constant" "$TOTAL" 200000
W='\set a random(1, 20)
'
reset; bench "$W"'BEGIN ISOLATION LEVEL REPEATABLE READ; SELECT tx.withdraw_naive(:a, 6000); COMMIT;'
check "naive withdraw never overdraws a customer" "$SKEW" 0 expect_fail
reset; bench "$W"'BEGIN ISOLATION LEVEL REPEATABLE READ; SELECT tx.withdraw_safe(:a, 6000); COMMIT;'
check "safe withdraw never overdraws a customer" "$SKEW" 0
