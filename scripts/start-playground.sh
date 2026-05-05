#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Checking Docker daemon..."
docker info >/dev/null 2>&1 || {
  echo "Docker daemon is not reachable. Start Docker Desktop (or Docker service) and try again."
  exit 1
}

echo "Starting persistent Spark playground services..."
docker compose up -d spark-master spark-worker spark-client-dev

echo

echo "Spark master UI: http://localhost:8080"
echo "Spark worker UI: http://localhost:8081"
echo "Dev shell: ./scripts/dev-shell.sh"
