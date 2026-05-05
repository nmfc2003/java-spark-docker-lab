FROM spark:3.5.8

USER root

RUN apt-get update \
    && apt-get install -y --no-install-recommends maven curl ca-certificates coreutils bash \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace/java-spark-docker-lab

CMD ["tail", "-f", "/dev/null"]
