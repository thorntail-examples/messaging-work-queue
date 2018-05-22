# Messaging Shared Work Queue Mission for WildFly Swarm

## Build and Deploy the Application

#### With Source to Image Build (S2I)

Run the following commands to apply and execute the OpenShift
templates that will configure and deploy the applications.

```bash
find . | grep openshiftio | grep application | xargs -n 1 oc apply -f

oc new-app --template=wfswarm-messaging-shared-work-queue-worker -p SOURCE_REPOSITORY_URL=https://github.com/ssorj/wfswarm-messaging-shared-work-queue -p SOURCE_REPOSITORY_REF=master -p SOURCE_REPOSITORY_DIR=worker
```

## Modules

The `resource-adapter` module serves as the JMS resource adapter for an
external AMQP message server.  It consists of two files.

* [resource-adapter/src/main/rar/META-INF/ra.xml](resource-adapter/src/main/rar/META-INF/ra.xml) -
  This is taken unaltered from the
  [generic JMS RA](https://github.com/jms-ra/generic-jms-ra) RAR
  module.

* [resource-adapter/pom.xml](resource-adapter/pom.xml) - This adds the
  dependencies necessary to use
  [Qpid JMS](http://qpid.apache.org/components/jms/index.html).

The `worker` module implements the worker service.

* [worker/src/main/java/io/openshift/booster/messaging/Worker.java](worker/src/main/java/io/openshift/booster/messaging/Worker.java) -
  The main worker code.  This also has some of the interesting
  annotation-based configuration.

* [worker/src/main/resources/project-defaults.yml](worker/src/main/resources/project-defaults.yml) -
  The main Swarm configuration.  This deploys and configures the
  resource adapter.

* [worker/pom.xml](worker/pom.xml) - This adds the necessary
  Swarm fractions and the dependency on the Qpid JMS resource adapter.
