# FederatedBus
Federated bus to interconnect multiple event-driven frameworks.


## Maven Goals

The following Maven goals can be used to work with the project.

Clean and build with running tests:
```
$ mvn clean install
```

Find code bugs using Findbugs static analysis:
```
$ mvn package findbugs:findbugs
$ mvn findbugs:gui
```

Measure JaCoCo Code Coverage:
```
$ mvn test jacoco:report
```