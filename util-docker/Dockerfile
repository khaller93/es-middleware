FROM openjdk:8-jdk-alpine

ARG ESM_VERSION
ARG JAR_FILE

LABEL maintainer="Kevin Haller <keivn.haller@tuwien.ac.at,kevin.haller@outofbits.com>"
LABEL version="${ESM_VERSION}"
LABEL description="Image for the exploratory search microservice of version '${ESM_VERSION}'."

VOLUME /tmp

COPY ${JAR_FILE} /app.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar", "/app.jar"]