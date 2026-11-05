#!/usr/bin/env bash
# Load your schema plus 200,000 seeded postings, then open psql to explore.
set -euo pipefail
cd "$(dirname "$0")"
if command -v psql >/dev/null; then PSQL=(psql "${PGURL:-postgres://dev:dev@localhost:5432/payments}")
else PSQL=(docker compose -f "$HOME/dev/infra/docker-compose.yml" exec -T postgres psql -U dev -d payments); fi
cat schema.sql seed.sql | "${PSQL[@]}" -q -v ON_ERROR_STOP=1 && echo "Loaded. Try: \\i queries/01_running_balance.sql (or paste it)"
