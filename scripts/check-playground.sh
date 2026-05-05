#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Checking Docker daemon..."
docker info >/dev/null

echo "Checking docker compose..."
docker compose version >/dev/null

for service in spark-master spark-worker spark-client-dev spark-history-server; do
  if ! docker compose ps --status running --services | grep -qx "$service"; then
    echo "Service is not running: $service"
    exit 1
  fi
  echo "Running: $service"
done

echo "Checking Spark UIs (8080, 8081, 18080)..."
curl -fsS http://localhost:8080 >/dev/null
curl -fsS http://localhost:8081 >/dev/null
curl -fsS http://localhost:18080 >/dev/null

echo "Checking tools and spark-events in spark-client-dev..."
docker compose exec -T spark-client-dev bash -lc 'java -version && mvn -version && /opt/spark/bin/spark-submit --version && mkdir -p /opt/spark-events && test -w /opt/spark-events'

echo "Checking spark-events writable in spark-history-server..."
docker compose exec -T spark-history-server bash -lc 'mkdir -p /opt/spark-events && test -w /opt/spark-events'

echo "Playground check passed."
