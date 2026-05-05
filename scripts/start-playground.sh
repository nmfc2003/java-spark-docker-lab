#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Checking Docker daemon..."
docker info >/dev/null 2>&1 || {
  echo "Docker daemon is not reachable. Start Docker Desktop (or Docker service) and try again."
  exit 1
}

mkdir -p "$ROOT_DIR/spark-events"

echo "Starting persistent Spark playground services..."
docker compose up -d spark-master spark-worker spark-client-dev spark-history-server

echo

echo "Spark Master UI: http://localhost:8080"
echo "Spark Worker UI: http://localhost:8081"
echo "Live Spark App UI: http://localhost:4040"
echo "Spark History Server: http://localhost:18080"
echo
echo "Note: 4040 is available only while a Spark application is running."
echo "Note: 18080 is for completed applications."
