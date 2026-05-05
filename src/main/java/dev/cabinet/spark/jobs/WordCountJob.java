package dev.cabinet.spark.jobs;

import java.util.Arrays;
import java.util.Locale;

import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.asc;
import static org.apache.spark.sql.functions.desc;

public final class WordCountJob {
    private WordCountJob() {
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected arguments: <inputPath> <outputPath>");
        }

        String inputPath = args[0];
        String outputPath = args[1];

        SparkSession spark = SparkSession.builder()
            .appName("WordCountJob")
            .getOrCreate();

        Dataset<String> words = spark.read()
            .textFile(inputPath)
            .flatMap(
                (FlatMapFunction<String, String>) line ->
                    Arrays.stream(line.toLowerCase(Locale.ROOT).split("\\W+"))
                        .iterator(),
                Encoders.STRING()
            )
            .filter((FilterFunction<String>) word -> !word.isBlank());

        Dataset<Row> counts = words.groupBy("value")
            .count()
            .orderBy(desc("count"), asc("value"));

        counts.write()
            .mode(SaveMode.Overwrite)
            .option("header", true)
            .csv(outputPath);

        spark.stop();
    }
}
