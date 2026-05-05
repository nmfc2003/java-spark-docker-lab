---
title: Java Spark Docker Lab
created: '2026-05-05T00:00:00.000Z'
modified: '2026-05-05T16:45:00.000Z'
tags:
  - java
  - spark
  - docker
  - hands-on
---
# Java Spark Docker Lab

This folder is a local Java + Apache Spark practice repo that runs through Docker.

It uses the official Apache Spark Docker image. Older `bitnami/spark` tags used by outdated examples are no longer available on Docker Hub.

## What is inside

- `docker-compose.yml` spins up a local Spark master and worker
- `Dockerfile` builds the jar in Docker, so local Maven is optional
- `scripts/run-job.sh` builds the client image and submits a job
- `scripts/run-all-jobs.sh` runs every practice job in one pass
- `src/main/java/...` contains sample Spark jobs for common patterns
- `data/` contains starter datasets across text, CSV, and JSON
- `output/` receives results from each run
- `README.md` is the repo-style entrypoint if you want to use this folder outside Cabinet

## Local setup

The repo is already local inside this Cabinet at:

```text
/Users/nmarianne/Library/Application Support/cabinet/cabinet-data/java-algo-interview-easy/java-spark-docker-lab
```

You do not need to install Maven. The jar is built inside Docker.

### 1. Install Docker Desktop

Install Docker Desktop for Mac, then open it and wait until Docker shows as running.

### 2. Verify Docker from the terminal

```bash
docker --version
docker compose version
docker ps
```

If `docker ps` fails, Docker Desktop is not fully started yet.

### 3. Move into the repo

```bash
cd "/Users/nmarianne/Library/Application Support/cabinet/cabinet-data/java-algo-interview-easy/java-spark-docker-lab"
```

### 4. See available jobs

```bash
./scripts/run-job.sh list
```

### 5. Run one job

Start with the smallest one:

```bash
./scripts/run-job.sh wordcount
```

What this command does:

- starts a local Spark master and worker with `docker compose`
- builds the `spark-client` image if needed
- builds the Java jar inside Docker
- submits the selected Spark job

### 6. Check the output

Outputs are written to the local `output/` folder:

```bash
find output -maxdepth 2 -type f | sort
```

For `wordcount`, Spark writes CSV part files under:

```text
output/wordcount
```

### 7. Run other jobs

```bash
./scripts/run-job.sh sales
./scripts/run-job.sh sessions
./scripts/run-job.sh joins
./scripts/run-job.sh dedup
./scripts/run-job.sh json-metrics
```

### 8. Run the whole practice pack

```bash
./scripts/run-all-jobs.sh
```

The first job builds the image. The remaining jobs reuse it through `SKIP_BUILD=1`.

### 9. Stop the cluster when finished

```bash
docker compose down
```

If you also want to remove old containers and networks:

```bash
docker compose down --remove-orphans
```

## Quick troubleshooting

- `Cannot connect to the Docker daemon`: start Docker Desktop first.
- Port `8080`, `8081`, or `7077` already in use: stop the other process or change ports in `docker-compose.yml`.
- Job output looks stale: delete the target folder under `output/` and run the job again.
- Pull error for `bitnami/spark`: this lab was updated to `spark:3.5.8`; rerun after pulling the latest file changes.

Spark UI:

- Master: `http://localhost:8080`
- Worker: `http://localhost:8081`

## Default jobs

### `wordcount`

- Input: `data/wordcount/input.txt`
- Output: `output/wordcount`
- Pattern: text processing with `Dataset<String>`

### `sales`

- Input: `data/orders/orders.csv`
- Output: `output/sales`
- Pattern: CSV + Spark SQL aggregation

### `sessions`

- Input: `data/events/user-events.csv`
- Output: `output/sessions`
- Logic: starts a new session when the gap between events is more than 30 minutes
- Pattern: window functions and sessionization

### `joins`

- Inputs:
  - `data/retail/orders.csv`
  - `data/retail/customers.csv`
  - `data/retail/products.csv`
- Output: `output/joins`
- Pattern: fact + dimension joins, enrichment, grouped KPIs

### `dedup`

- Input: `data/profiles/profile-updates.csv`
- Output: `output/dedup`
- Pattern: normalization, latest-record selection, deduplication

### `json-metrics`

- Input: `data/json/app-events.json`
- Output: `output/json-metrics`
- Pattern: semi-structured JSON, array explode, grouped metrics

## Repo flow

If you want a full smoke run:

```bash
./scripts/run-all-jobs.sh
```

If you want to treat this as a repo outside Cabinet, start with `README.md`.

## Custom runs

You can pass explicit job arguments after the job name. Examples:

```bash
./scripts/run-job.sh wordcount /opt/spark-data/wordcount/input.txt /opt/spark-output/wordcount-custom
./scripts/run-job.sh sessions /opt/spark-data/events/user-events.csv /opt/spark-output/sessions-15m 15
./scripts/run-job.sh joins /opt/spark-data/retail/orders.csv /opt/spark-data/retail/customers.csv /opt/spark-data/retail/products.csv /opt/spark-output/joins-custom
```

The client container expects input under `/opt/spark-data` and writes output under `/opt/spark-output`.
