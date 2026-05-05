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

## Interactive Spark Playground

### Architecture

```text
Host IntelliJ
   |
   | edits repo files
   v
Mounted repo volume
   |
   v
spark-client-dev container
   |
   | mvn package + spark-submit
   v
spark://spark-master:7077
   |
   v
spark-worker
```

### Start persistent cluster

```bash
./scripts/start-playground.sh
```

This starts `spark-master`, `spark-worker`, and `spark-client-dev` and keeps them running until you stop them.

### Enter the dev container

```bash
./scripts/dev-shell.sh
```

### Submit jobs repeatedly (without restarting cluster)

```bash
./scripts/submit-java.sh scratch
./scripts/submit-java.sh wordcount
./scripts/submit-java.sh sales
./scripts/submit-java.sh sessions
./scripts/submit-java.sh joins
./scripts/submit-java.sh dedup
./scripts/submit-java.sh json-metrics
```

You can also pass custom job args:

```bash
./scripts/submit-java.sh scratch /opt/spark-data/wordcount/input.txt /opt/spark-output/scratch-alt
```

### IntelliJ + Docker workflow

1. Start playground once.
2. Edit Java code locally in IntelliJ.
3. Re-run `./scripts/submit-java.sh <job>` as often as needed.
4. Spark master/worker stay up between submits.

### Inspect output

```bash
find output -maxdepth 3 -type f | sort
```

Default data mount: `/opt/spark-data`  
Default output mount: `/opt/spark-output`

### Spark UI

- Spark master UI: <http://localhost:8080>
- Spark worker UI: <http://localhost:8081>

### Stop cluster

```bash
./scripts/stop-playground.sh
```

### Add a new Java Spark job

1. Add a class under `src/main/java/dev/cabinet/spark/jobs`.
2. Add a new mapping in `scripts/submit-java.sh` from job name to class.
3. Submit with `./scripts/submit-java.sh <new-job>`.

### Playground health check

```bash
./scripts/check-playground.sh
```

### Makefile shortcuts

```bash
make up
make shell
make check
make submit JOB=wordcount
make scratch
make down
```

### Troubleshooting

- **Docker daemon not running**
  - Start Docker Desktop or Docker Engine, then rerun `./scripts/start-playground.sh`.
- **Spark master UI not available**
  - Check services with `docker compose ps` and logs with `docker compose logs spark-master`.
- **Maven build failed**
  - Run `./scripts/dev-shell.sh`, then `mvn package` for detailed compiler output.
- **Unknown job name**
  - Use one of: `wordcount`, `sales`, `sessions`, `joins`, `dedup`, `json-metrics`, `scratch`.
- **Output path already exists**
  - Most jobs overwrite by default, but if you customize writes, remove path under `output/` or use a new output path.
- **Permission issues in output directory**
  - Fix ownership/permissions on `./output` so Docker can write files.
- **Ports 8080/8081 already in use**
  - Stop conflicting processes or change port mappings in `docker-compose.yml`.

## Spark UI and History Server

Spark exposes different UIs for different scopes:

- `http://localhost:8080` = **Spark Master UI** (cluster-level scheduling/executors summary).
- `http://localhost:8081` = **Spark Worker UI** (worker-level resources and executors).
- `http://localhost:4040` = **Live Spark application (driver) UI**.
  - This is available only while a Spark app is running.
- `http://localhost:18080` = **Spark History Server** for completed applications.
  - Completed jobs should be inspected here, not with sleep hacks.

### Observability workflow

```bash
./scripts/start-playground.sh
./scripts/submit-java.sh scratch
```

- While `scratch` is running, open `http://localhost:4040` to inspect live DAG/stages/jobs.
- After it finishes, open `http://localhost:18080` and inspect the completed app.

### Why 4040 may disappear quickly

Short jobs can complete before you switch tabs. That is expected. The persistent place to inspect finished runs is History Server on `18080` with event logs from `./spark-events`.

### Event log maintenance

If you want to reset local history:

```bash
./scripts/clean-spark-events.sh
```

This clears local Spark event logs and removes app history from History Server.
