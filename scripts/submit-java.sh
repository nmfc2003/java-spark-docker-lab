#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

JOB_NAME="${1:-}"
if [ -z "$JOB_NAME" ]; then
  echo "Usage: ./scripts/submit-java.sh <job-name> [args...]"
  echo "Available jobs: wordcount sales sessions joins dedup json-metrics scratch"
  exit 1
fi
shift || true

case "$JOB_NAME" in
  wordcount) MAIN_CLASS="dev.cabinet.spark.jobs.WordCountJob" ;;
  sales) MAIN_CLASS="dev.cabinet.spark.jobs.SalesByCategoryJob" ;;
  sessions) MAIN_CLASS="dev.cabinet.spark.jobs.UserSessionJob" ;;
  joins) MAIN_CLASS="dev.cabinet.spark.jobs.CustomerSpendJoinJob" ;;
  dedup) MAIN_CLASS="dev.cabinet.spark.jobs.UserProfileDedupJob" ;;
  json-metrics) MAIN_CLASS="dev.cabinet.spark.jobs.JsonEventMetricsJob" ;;
  scratch) MAIN_CLASS="dev.cabinet.spark.jobs.ScratchJob" ;;
  *)
    echo "Unknown job name: $JOB_NAME"
    echo "Available jobs: wordcount sales sessions joins dedup json-metrics scratch"
    exit 1
    ;;
esac

EXTRA_ARGS=("$@")

echo "Building project inside spark-client-dev..."
docker compose exec -T spark-client-dev bash -lc 'mkdir -p /opt/spark-events && mvn -q -DskipTests package'

echo "Resolving built jar..."
JAR_PATH="$(docker compose exec -T spark-client-dev bash -lc 'ls -1 target/*.jar | grep -v "/original-" | grep -v -- "-sources\.jar" | grep -v -- "-javadoc\.jar" | head -n 1' | tr -d '\r')"

if [ -z "$JAR_PATH" ]; then
  echo "Could not find built jar in target/*.jar"
  exit 1
fi

echo "Live UI while job is running: http://localhost:4040"
echo "Completed app history after finish: http://localhost:18080"

echo "Submitting $JOB_NAME ($MAIN_CLASS) using $JAR_PATH"
docker compose exec -T spark-client-dev bash -lc "/opt/spark/bin/spark-submit \
  --master spark://spark-master:7077 \
  --class $MAIN_CLASS \
  --conf spark.ui.enabled=true \
  --conf spark.ui.port=4040 \
  --conf spark.ui.host=0.0.0.0 \
  --conf spark.driver.bindAddress=0.0.0.0 \
  --conf spark.eventLog.enabled=true \
  --conf spark.eventLog.dir=file:/opt/spark-events \
  --conf spark.history.fs.logDirectory=file:/opt/spark-events \
  $JAR_PATH $(printf '%q ' "${EXTRA_ARGS[@]}")"
