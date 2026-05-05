package dev.cabinet.spark.jobs;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.coalesce;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.countDistinct;
import static org.apache.spark.sql.functions.explode_outer;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.to_date;
import static org.apache.spark.sql.functions.to_timestamp;

public final class JsonEventMetricsJob {
    private JsonEventMetricsJob() {
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected arguments: <inputPath> <outputPath>");
        }

        String inputPath = args[0];
        String outputPath = args[1];

        SparkSession spark = SparkSession.builder()
            .appName("JsonEventMetricsJob")
            .getOrCreate();

        Dataset<Row> metrics = spark.read()
            .json(inputPath)
            .withColumn("event_ts", to_timestamp(col("event_time"), "yyyy-MM-dd'T'HH:mm:ss"))
            .withColumn("event_date", to_date(col("event_ts")))
            .withColumn("tag", explode_outer(col("tags")))
            .withColumn("tag", coalesce(col("tag"), lit("untagged")))
            .withColumn("plan", coalesce(col("metadata.plan"), lit("free")))
            .groupBy("event_date", "device", "plan", "event_type", "tag")
            .agg(
                count(lit(1)).alias("event_count"),
                countDistinct("user_id").alias("unique_users")
            )
            .orderBy(col("event_date"), col("device"), col("event_type"), col("tag"));

        metrics.write()
            .mode(SaveMode.Overwrite)
            .option("header", true)
            .csv(outputPath);

        spark.stop();
    }
}
