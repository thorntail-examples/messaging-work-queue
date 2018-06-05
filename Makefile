.PHONY: help
help:
	@echo "build           Build the code"
	@echo "run             Run all the components using a test broker"
	@echo "clean           Removes build and test artifacts"

.PHONY: build
build:
	mvn package

.PHONY: clean
clean:
	mvn clean
	rm -f README.html audit.log

.PHONY: test
test:
	mvn install -Popenshift-it

.PHONY: run
run:
	scripts/run-all

.PHONY: run-frontend
run-frontend:
	java -jar frontend/target/wfswarm-messaging-work-queue-frontend-1-SNAPSHOT-swarm.jar

.PHONY: run-worker-1
run-worker-1:
	java -Dswarm.port.offset=1 -jar worker/target/wfswarm-messaging-work-queue-worker-1-SNAPSHOT-swarm.jar

.PHONY: run-worker-2
run-worker-2:
	java -Dswarm.port.offset=2 -jar worker/target/wfswarm-messaging-work-queue-worker-1-SNAPSHOT-swarm.jar

README.html: README.md
	pandoc $< -o $@
