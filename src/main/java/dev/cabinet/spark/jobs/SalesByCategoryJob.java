package dev.cabinet.spark.jobs;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

public final class SalesByCategoryJob {
    private SalesByCategoryJob() {
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected arguments: <inputPath> <outputPath>");
        }

        String inputPath = args[0];
        String outputPath = args[1];

        SparkSession spark = SparkSession.builder()
            .appName("SalesByCategoryJob")
            .getOrCreate();

        Dataset<Row> orders = spark.read()
            .option("header", true)
            .option("inferSchema", true)
            .csv(inputPath);

        orders.createOrReplaceTempView("orders");

        Dataset<Row> totals = spark.sql(
            "SELECT " +
                "category, " +
                "ROUND(SUM(quantity * unit_price), 2) AS gross_revenue, " +
                "SUM(quantity) AS units_sold, " +
                "ROUND(AVG(unit_price), 2) AS average_unit_price " +
            "FROM orders " +
            "GROUP BY category " +
            "ORDER BY gross_revenue DESC, category ASC"
        );

        totals.write()
            .mode(SaveMode.Overwrite)
            .option("header", true)
            .csv(outputPath);

        spark.stop();
    }
}
