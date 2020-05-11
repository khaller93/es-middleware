# Exploratory Search Microservice

The exploratory search microservice is sitting on top of a knowledge graph and provides services to
the web application enabling exploratory search on mentioned knowledge graph. This microservice 
expects three interfaces to the knowledge graph, namely SPARQL, Gremlin and a full-text search index.
Itself offers a REST interface to the web application, allowing clients to execute exploration flows.

![alt text](./doc/graphics/exploratory-search-concept-high-level.png)

## Build

The microservice is a Spring Boot application implemented in Java.

Requirements:
* Java JDK (>= 1.8)
* Maven

The application can be build with the following command. Its suggested to skip the tests, because it
takes a while to compute the tests.

```
mvn package -DskipTests=true
```

The build results in a JAR package named `esm.jar`, which can be executed.

### Docker Image

This repository also provides a Dockerfile for building a container image for this microservice. The
build requires a running Docker daemon (alternatively Red Hat buildah could be used).

```
docker build . -t yyyy/esm:latest
```

## Run

The execution is as simple as the following line. However, in order to use the service a correct
configuration has to be passed, which is described in the subsequent section.

```
java -jar esm.jar
```

You can see the Swagger documentation of the REST API by visiting the endpoint with a browser, 
per default `http://localhost:8080`.

### Configuration

#### Logging

The general logging level can be set as following. Per default the logging level is `INFO`.

```
logging.level.at.ac.tuwien=INFO
```

Advanced logging of the execution of exploration flows is disabled per default and can be enabled 
with the following line. The start as well as end of each step will then be logged.

```
esm.flow.execution.logging=true
```

Moreover, a stopwatch for the execution of steps of an exploration flow can be enabled. The result
of the stopwatch is then added to the meta section of the JSON response for an exploration 
flow.

```
esm.flow.stopwatch=true
```

However, it is also possible to log the stopwatch results by setting following line.

```
esm.flow.stopwatch.logging=true
```

#### Backend Storage Solution

The RDF data is most commonly stored in a triplestore. In the sections below, a number of
supported popular stores are listed. It is required to select the storage solution with the
following property:

```
esm.db.choice=GraphDB
```

Possible choices are `GraphDB`, `Stardog`, `Blazegraph` and `Virtuoso`.

##### GraphDB

Ontotext [GraphDB](http://graphdb.ontotext.com/documentation/standard/) is a commercial triplestore
with in-built reasoning engines. Ontotext provides a free community edition of the database with 
the limitation that only two SPARQL queries can be computed in parallel.

| Parameter | Description | Required? |
|---|----|---|
| graphdb.repository.id | The ID of the repository | Yes |
| graphdb.address | The host address of the GraphDB server | Yes |
| graphdb.username | Username of account having access to the GraphDB server | No |
| graphdb.password | Password of the account with specified username | No |

##### Stardog

[Stardog](https://www.stardog.com/) is a commercial triplestore with in-built reasoning engines
and some other features. Stardog offers a free license of this product with some limitations such
as limit to the total number of triples.

| Parameter | Description | Required? |
|----|---|----|
| stardog.db.name | The ID of the database/repository in Stardog | Yes |
| stardog.address | The host address of the Stardog server | Yes |
| stardog.username | Username of account having access to the Stardog server | No |
| stardog.password | Password of the account with specified username | No |

##### Blazegraph

[Blazegraph](https://blazegraph.com/) is an open-source triplestore, which had a stale development
for some years, but seems to be developed on again.

| Parameter | Description | Required? |
|----|---|----|
| blazegraph.address | The address of the SPARQL endpoint | Yes |

##### Virtuoso

OpenLink [Virtuoso](https://virtuoso.openlinksw.com/) is a commercial triplestore that is used among others by DBPedia. OpenLink
as well provides a free community edition of its triplestore ([here](http://vos.openlinksw.com/owiki/wiki/VOS)).

| Parameter | Description | Required? |
|----|---|----|
| virtuoso.address | The address of the SPARQL endpoint | Yes |
