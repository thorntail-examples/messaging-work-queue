.PHONY: build
build:
	mvn package

.PHONY: clean
clean:
	mvn clean
	rm -f audit.log

.PHONY: run
run:
	java -jar worker/target/wfswarm-messaging-shared-work-queue-worker-1-SNAPSHOT-swarm.jar
