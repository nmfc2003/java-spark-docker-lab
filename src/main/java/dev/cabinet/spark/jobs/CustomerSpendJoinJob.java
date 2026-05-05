package dev.cabinet.spark.jobs;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.avg;
import static org.apache.spark.sql.functions.coalesce;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.countDistinct;
import static org.apache.spark.sql.functions.round;
import static org.apache.spark.sql.functions.sum;

public final class CustomerSpendJoinJob {
    private CustomerSpendJoinJob() {
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException(
                "Expected arguments: <ordersPath> <customersPath> <productsPath> <outputPath>"
            );
        }

        String ordersPath = args[0];
        String customersPath = args[1];
        String productsPath = args[2];
        String outputPath = args[3];

        SparkSession spark = SparkSession.builder()
            .appName("CustomerSpendJoinJob")
            .getOrCreate();

        Dataset<Row> orders = spark.read()
            .option("header", true)
            .option("inferSchema", true)
            .csv(ordersPath);

        Dataset<Row> customers = spark.read()
            .option("header", true)
            .option("inferSchema", true)
            .csv(customersPath);

        Dataset<Row> products = spark.read()
            .option("header", true)
            .option("inferSchema", true)
            .csv(productsPath);

        Dataset<Row> enriched = orders.alias("o")
            .join(customers.alias("c"), col("o.customer_id").equalTo(col("c.customer_id")), "left")
            .join(products.alias("p"), col("o.product_id").equalTo(col("p.product_id")), "left")
            .withColumn("line_revenue", round(col("o.quantity").multiply(col("o.unit_price")), 2))
            .select(
                col("o.order_id"),
                col("o.order_date"),
                col("o.customer_id"),
                col("c.customer_name"),
                col("c.region"),
                col("c.segment"),
                coalesce(col("p.category"), col("o.product_id")).alias("category"),
                coalesce(col("p.product_name"), col("o.product_id")).alias("product_name"),
                col("o.quantity"),
                col("o.unit_price"),
                col("line_revenue")
            );

        Dataset<Row> summary = enriched.groupBy("region", "segment", "category")
            .agg(
                round(sum("line_revenue"), 2).alias("gross_revenue"),
                sum("quantity").alias("units_sold"),
                countDistinct("customer_id").alias("unique_customers"),
                round(avg("line_revenue"), 2).alias("avg_order_line")
            )
            .orderBy(col("gross_revenue").desc(), col("region"), col("category"));

        summary.write()
            .mode(SaveMode.Overwrite)
            .option("header", true)
            .csv(outputPath);

        spark.stop();
    }
}
