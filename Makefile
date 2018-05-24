.PHONY: build
build:
	mvn package

.PHONY: clean
clean:
	mvn clean
	rm -f audit.log

.PHONY: run-frontend
run-frontend:
	java -jar frontend/target/wfswarm-messaging-shared-work-queue-frontend-1-SNAPSHOT-swarm.jar

.PHONY: run-worker
run-worker:
	java -jar worker/target/wfswarm-messaging-shared-work-queue-worker-1-SNAPSHOT-swarm.jar
