# Java Spark Docker Lab

Small Apache Spark practice repo for Java that runs locally through Docker.

## What this repo covers

- text processing
- CSV aggregation
- Spark SQL
- joins and enrichment
- window functions and sessionization
- deduplication and latest-record selection
- JSON ingestion and array explosion

## Prerequisites

- Docker
- Docker Compose v2

You do not need Maven installed locally. The jar is built in Docker.

This repo uses the official Apache Spark Docker image. Older `bitnami/spark` tags used in older examples are no longer available on Docker Hub.

## Local install and first run

This repo is already local if you are using Cabinet. From a normal terminal, move into it:

```bash
cd "/Users/nmarianne/Library/Application Support/cabinet/cabinet-data/java-algo-interview-easy/java-spark-docker-lab"
```

Make sure Docker Desktop is running, then verify:

```bash
docker --version
docker compose version
docker ps
```

List the available jobs:

```bash
./scripts/run-job.sh list
```

Run the smallest example first:

```bash
./scripts/run-job.sh wordcount
```

That command:

- starts the Spark master and worker
- builds the client image if needed
- compiles the Java jar inside Docker
- submits the Spark job

Inspect generated files:

```bash
find output -maxdepth 2 -type f | sort
```

Run the other jobs:

```bash
./scripts/run-job.sh sales
./scripts/run-job.sh sessions
./scripts/run-job.sh joins
./scripts/run-job.sh dedup
./scripts/run-job.sh json-metrics
```

Run the full practice pack:

```bash
./scripts/run-all-jobs.sh
```

Spark UI:

- master: `http://localhost:8080`
- worker: `http://localhost:8081`

Stop the cluster:

```bash
docker compose down
```

If you see `Cannot connect to the Docker daemon`, Docker Desktop is not running yet.
If you see a pull error for `bitnami/spark`, you are on an older copy of the repo; this copy has already been updated to `spark:3.5.8`.

## Repo layout

```text
.
├── data
│   ├── events
│   ├── json
│   ├── orders
│   ├── profiles
│   ├── retail
│   └── wordcount
├── docker
├── output
├── scripts
└── src/main/java/dev/cabinet/spark/jobs
```

## Practice jobs

### `wordcount`

- input: `data/wordcount/input.txt`
- output: `output/wordcount`
- focus: tokenization, filtering, counts

### `sales`

- input: `data/orders/orders.csv`
- output: `output/sales`
- focus: reading CSV and writing a SQL-style aggregate report

### `sessions`

- input: `data/events/user-events.csv`
- output: `output/sessions`
- focus: `lag`, cumulative windows, session boundaries

### `joins`

- inputs:
  - `data/retail/orders.csv`
  - `data/retail/customers.csv`
  - `data/retail/products.csv`
- output: `output/joins`
- focus: joining fact and dimension tables, grouped KPIs

### `dedup`

- input: `data/profiles/profile-updates.csv`
- output: `output/dedup`
- focus: normalization, `row_number`, latest valid state

### `json-metrics`

- input: `data/json/app-events.json`
- output: `output/json-metrics`
- focus: nested JSON, `explode_outer`, per-tag metrics

## Passing custom arguments

Each job accepts explicit input and output paths after the job name.

```bash
./scripts/run-job.sh sessions /opt/spark-data/events/user-events.csv /opt/spark-output/sessions-10m 10
./scripts/run-job.sh joins /opt/spark-data/retail/orders.csv /opt/spark-data/retail/customers.csv /opt/spark-data/retail/products.csv /opt/spark-output/joins-alt
```

## Suggested practice order

1. `wordcount`
2. `sales`
3. `joins`
4. `sessions`
5. `dedup`
6. `json-metrics`

## Extensions to try

- add a new worker in `docker-compose.yml`
- repartition large outputs by date or category
- swap CSV output for Parquet
- add a new SQL report with temp views
- add unit tests with local Spark
