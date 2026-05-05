#!/bin/bash
set -euo pipefail

JOB_NAME="${1:-}"

if [ -z "$JOB_NAME" ]; then
    echo "Usage: wordcount | sales | sessions | joins | dedup | json-metrics [job args...]"
    exit 1
fi

shift || true

case "$JOB_NAME" in
    wordcount)
        MAIN_CLASS="dev.cabinet.spark.jobs.WordCountJob"
        DEFAULT_ARGS=("/opt/spark-data/wordcount/input.txt" "/opt/spark-output/wordcount")
        ;;
    sales)
        MAIN_CLASS="dev.cabinet.spark.jobs.SalesByCategoryJob"
        DEFAULT_ARGS=("/opt/spark-data/orders/orders.csv" "/opt/spark-output/sales")
        ;;
    sessions)
        MAIN_CLASS="dev.cabinet.spark.jobs.UserSessionJob"
        DEFAULT_ARGS=("/opt/spark-data/events/user-events.csv" "/opt/spark-output/sessions" "30")
        ;;
    joins)
        MAIN_CLASS="dev.cabinet.spark.jobs.CustomerSpendJoinJob"
        DEFAULT_ARGS=(
            "/opt/spark-data/retail/orders.csv"
            "/opt/spark-data/retail/customers.csv"
            "/opt/spark-data/retail/products.csv"
            "/opt/spark-output/joins"
        )
        ;;
    dedup)
        MAIN_CLASS="dev.cabinet.spark.jobs.UserProfileDedupJob"
        DEFAULT_ARGS=("/opt/spark-data/profiles/profile-updates.csv" "/opt/spark-output/dedup")
        ;;
    json-metrics)
        MAIN_CLASS="dev.cabinet.spark.jobs.JsonEventMetricsJob"
        DEFAULT_ARGS=("/opt/spark-data/json/app-events.json" "/opt/spark-output/json-metrics")
        ;;
    *)
        echo "Unknown job: $JOB_NAME"
        exit 1
        ;;
esac

ARGS=("$@")

if [ ${#ARGS[@]} -eq 0 ]; then
    ARGS=("${DEFAULT_ARGS[@]}")
fi

/opt/spark/bin/spark-submit \
    --master "${SPARK_MASTER_URL:-spark://spark-master:7077}" \
    --class "$MAIN_CLASS" \
    /opt/spark-lab/spark-jobs.jar \
    "${ARGS[@]}"
