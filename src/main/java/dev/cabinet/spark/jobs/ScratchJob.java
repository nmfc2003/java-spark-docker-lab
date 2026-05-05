package dev.cabinet.spark.jobs;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.desc;
import static org.apache.spark.sql.functions.explode;
import static org.apache.spark.sql.functions.lower;
import static org.apache.spark.sql.functions.split;
import static org.apache.spark.sql.functions.trim;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public final class ScratchJob {
    private ScratchJob() {}

    public static void main(String[] args) {
        String inputPath = args.length > 0 ? args[0] : "/opt/spark-data/wordcount/input.txt";
        String outputPath = args.length > 1 ? args[1] : "/opt/spark-output/scratch";

        SparkSession spark = SparkSession.builder()
                .appName("ScratchJob")
                .getOrCreate();

        Dataset<Row> wordCounts = spark.read()
                .text(inputPath)
                .withColumn("word", explode(split(col("value"), "\\\\W+")))
                .withColumn("word", lower(trim(col("word"))))
                .filter(col("word").notEqual(""))
                .groupBy("word")
                .count()
                .orderBy(desc("count"));

        wordCounts.printSchema();
        wordCounts.show(50, false);

        wordCounts.write()
                .mode("overwrite")
                .option("header", "true")
                .csv(outputPath);

        spark.stop();
    }
}
