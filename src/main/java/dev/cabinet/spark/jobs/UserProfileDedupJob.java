package dev.cabinet.spark.jobs;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

import static org.apache.spark.sql.functions.coalesce;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.date_format;
import static org.apache.spark.sql.functions.expr;
import static org.apache.spark.sql.functions.initcap;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.lower;
import static org.apache.spark.sql.functions.row_number;
import static org.apache.spark.sql.functions.to_timestamp;
import static org.apache.spark.sql.functions.trim;

public final class UserProfileDedupJob {
    private UserProfileDedupJob() {
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected arguments: <inputPath> <outputPath>");
        }

        String inputPath = args[0];
        String outputPath = args[1];

        SparkSession spark = SparkSession.builder()
            .appName("UserProfileDedupJob")
            .getOrCreate();

        WindowSpec latestPerUser = Window.partitionBy("user_id")
            .orderBy(col("updated_ts").desc(), col("source").desc());

        Dataset<Row> deduped = spark.read()
            .option("header", true)
            .csv(inputPath)
            .withColumn("updated_ts", to_timestamp(col("updated_at"), "yyyy-MM-dd HH:mm:ss"))
            .withColumn("email", expr("nullif(lower(trim(email)), '')"))
            .withColumn("city", initcap(lower(trim(col("city")))))
            .withColumn("status", coalesce(expr("nullif(lower(trim(status)), '')"), lit("unknown")))
            .withColumn("source", lower(trim(col("source"))))
            .withColumn("record_rank", row_number().over(latestPerUser))
            .filter(col("record_rank").equalTo(1))
            .select(
                col("user_id"),
                col("email"),
                col("city"),
                col("status"),
                col("source"),
                date_format(col("updated_ts"), "yyyy-MM-dd HH:mm:ss").alias("updated_at")
            )
            .orderBy(col("user_id"));

        deduped.write()
            .mode(SaveMode.Overwrite)
            .option("header", true)
            .csv(outputPath);

        spark.stop();
    }
}
