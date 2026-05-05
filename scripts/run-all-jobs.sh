#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_JOB_SCRIPT="$ROOT_DIR/scripts/run-job.sh"

jobs=(
    wordcount
    sales
    sessions
    joins
    dedup
    json-metrics
)

"$RUN_JOB_SCRIPT" "${jobs[0]}"

for job in "${jobs[@]:1}"; do
    SKIP_BUILD=1 "$RUN_JOB_SCRIPT" "$job"
done
