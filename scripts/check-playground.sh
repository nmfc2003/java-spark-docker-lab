#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Checking Docker daemon..."
docker info >/dev/null

echo "Checking docker compose..."
docker compose version >/dev/null

for service in spark-master spark-worker spark-client-dev; do
  if ! docker compose ps --status running --services | grep -qx "$service"; then
    echo "Service is not running: $service"
    exit 1
  fi
  echo "Running: $service"
done

echo "Checking tools in spark-client-dev..."
docker compose exec -T spark-client-dev bash -lc 'java -version && mvn -version && /opt/spark/bin/spark-submit --version'

echo "Playground check passed."
