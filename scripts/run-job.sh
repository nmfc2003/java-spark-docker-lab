#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
JOB_NAME="${1:-}"

if [ -z "$JOB_NAME" ]; then
    echo "Usage: ./scripts/run-job.sh <wordcount|sales|sessions|joins|dedup|json-metrics> [job args...]"
    exit 1
fi

shift || true

if [ "$JOB_NAME" = "list" ]; then
    cat <<'EOF'
wordcount
sales
sessions
joins
dedup
json-metrics
EOF
    exit 0
fi

docker compose -f "$COMPOSE_FILE" up -d spark-master spark-worker
sleep 5

if [ "${SKIP_BUILD:-0}" != "1" ]; then
    docker compose -f "$COMPOSE_FILE" build spark-client
fi

docker compose -f "$COMPOSE_FILE" run --rm spark-client "$JOB_NAME" "$@"
