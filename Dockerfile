FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests package

FROM spark:3.5.8

USER root
WORKDIR /opt/spark-lab

COPY --from=build /workspace/target/spark-jobs.jar /opt/spark-lab/spark-jobs.jar
COPY docker/submit-job.sh /opt/spark-lab/bin/submit-job.sh

RUN chmod +x /opt/spark-lab/bin/submit-job.sh \
    && mkdir -p /opt/spark-data /opt/spark-output \
    && chown -R spark:spark /opt/spark-lab /opt/spark-data /opt/spark-output

USER spark
ENTRYPOINT ["/opt/spark-lab/bin/submit-job.sh"]
