#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Warning: this will remove all files under ./spark-events and clear Spark History Server app history."
rm -rf "$ROOT_DIR/spark-events"/*
echo "spark-events cleaned."
