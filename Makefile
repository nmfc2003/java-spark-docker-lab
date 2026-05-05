up:
	./scripts/start-playground.sh

shell:
	./scripts/dev-shell.sh

submit:
	./scripts/submit-java.sh $(JOB)

scratch:
	./scripts/submit-java.sh scratch

check:
	./scripts/check-playground.sh

down:
	./scripts/stop-playground.sh
