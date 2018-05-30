.PHONY: build
build:
	mvn package

.PHONY: clean
clean:
	mvn clean
	rm -f README.html audit.log

.PHONY: run-frontend
run-frontend:
	java -jar frontend/target/wfswarm-messaging-shared-work-queue-frontend-1-SNAPSHOT-swarm.jar

.PHONY: run-worker-1
run-worker-1:
	java -Dswarm.port.offset=1 -jar worker/target/wfswarm-messaging-shared-work-queue-worker-1-SNAPSHOT-swarm.jar

.PHONY: run-worker-2
run-worker-2:
	java -Dswarm.port.offset=2 -jar worker/target/wfswarm-messaging-shared-work-queue-worker-1-SNAPSHOT-swarm.jar

README.html: README.md
	pandoc $< -o $@
