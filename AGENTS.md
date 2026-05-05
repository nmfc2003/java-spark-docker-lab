You are working on a Java Spark Docker lab.

Goals:
- Enable interactive Spark development
- Prefer docker-compose based solutions
- Keep cluster persistent between runs
- Optimize for fast dev loop (edit → compile → submit)

Commands:
- Build: mvn package
- Run job: ./scripts/run-job.sh <job>
- Data path: /opt/spark-data
- Output path: /opt/spark-output

Constraints:
- Do not break existing scripts
- Prefer adding new scripts over replacing old ones
