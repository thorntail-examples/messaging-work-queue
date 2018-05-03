# Upstate worker - WildFly Swarm

## Modules

The `qpid-jms-ra` module serves as the JMS resource adapter for an
external AMQP message server.  It consists of two files.

* [qpid-jms-ra/src/main/rar/META-INF/ra.xml](qpid-jms-ra/src/main/rar/META-INF/ra.xml) -
  This is taken unaltered from the
  [generic JMS RA](https://github.com/jms-ra/generic-jms-ra) RAR
  module.

* [qpid-jms-ra/pom.xml](qpid-jms-ra/pom.xml) - This adds the
  dependencies necessary to use
  [Qpid JMS](http://qpid.apache.org/components/jms/index.html).

The `container` module is the primary vessel for the worker service.

* [container/src/main/java/org/amqphub/upstate/swarm/SwarmWorker.java](container/src/main/java/org/amqphub/upstate/swarm/SwarmWorker.java) -
  The main worker code.  This also has some of the interesting
  annotation-based configuration.

* [container/src/main/resources/project-defaults.yml](container/src/main/resources/project-defaults.yml) -
  The main Swarm configuration.  This deploys and configures the
  resource adapter.

* [container/pom.xml](container/pom.xml) - This adds the necessary
  Swarm fractions and the dependency on the Qpid JMS resource adapter.
