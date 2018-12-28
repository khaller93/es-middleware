FROM openjdk:8-jdk-alpine

VOLUME /tmp

ARG JAR_FILE
ARG LIB_GRAPHDB
ARG LIB_STARDOG
ARG LIB_BLAZEGRAPH
ARG LIB_JANUSGRAPH

# add app jar file
COPY ${JAR_FILE} app.jar
# add libs
#RUN  mkdir -p lib
#COPY ${LIB_GRAPHDB} lib/graphdb-lib.jar
#COPY ${LIB_STARDOG} lib/stardog-lib.jar
#COPY ${LIB_BLAZEGRAPH} lib/blazegraph-lib.jar
#COPY ${LIB_JANUSGRAPH} lib/janusgraph-lib.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]