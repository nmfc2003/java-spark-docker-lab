package dev.cabinet.spark.jobs;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.concat_ws;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.lag;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.min;
import static org.apache.spark.sql.functions.sum;
import static org.apache.spark.sql.functions.to_timestamp;
import static org.apache.spark.sql.functions.unix_timestamp;
import static org.apache.spark.sql.functions.when;

public final class UserSessionJob {
    private UserSessionJob() {
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected arguments: <inputPath> <outputPath> [gapMinutes]");
        }

        String inputPath = args[0];
        String outputPath = args[1];
        int gapMinutes = args.length >= 3 ? Integer.parseInt(args[2]) : 30;

        SparkSession spark = SparkSession.builder()
            .appName("UserSessionJob")
            .getOrCreate();

        Dataset<Row> events = spark.read()
            .option("header", true)
            .csv(inputPath)
            .withColumn("event_ts", to_timestamp(col("event_time"), "yyyy-MM-dd'T'HH:mm:ss"));

        WindowSpec orderedByUser = Window.partitionBy("user_id").orderBy("event_ts");
        WindowSpec cumulativeByUser = orderedByUser.rowsBetween(Window.unboundedPreceding(), Window.currentRow());

        Dataset<Row> sessionized = events
            .withColumn("prev_ts", lag("event_ts", 1).over(orderedByUser))
            .withColumn(
                "is_new_session",
                when(
                    col("prev_ts").isNull().or(
                        unix_timestamp(col("event_ts")).minus(unix_timestamp(col("prev_ts")))
                            .gt(lit(gapMinutes * 60))
                    ),
                    lit(1)
                ).otherwise(lit(0))
            )
            .withColumn("session_number", sum("is_new_session").over(cumulativeByUser))
            .withColumn(
                "session_id",
                concat_ws("-", col("user_id"), col("session_number"))
            );

        Dataset<Row> sessions = sessionized.groupBy("user_id", "session_id")
            .agg(
                min("event_ts").alias("session_start"),
                max("event_ts").alias("session_end"),
                count(lit(1)).alias("event_count")
            )
            .orderBy(col("user_id"), col("session_start"));

        sessions.write()
            .mode(SaveMode.Overwrite)
            .option("header", true)
            .csv(outputPath);

        spark.stop();
    }
}
