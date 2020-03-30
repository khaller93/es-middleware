FROM maven:3-jdk-14 AS compiler



FROM openjdk:14-jdk-alpine

VOLUME /tmp

ARG JAR_FILE
ARG ESM_VERSION

LABEL maintainer="Kevin Haller <keivn.haller@tuwien.ac.at,kevin.haller@outofbits.com>"
LABEL version="${ESM_VERSION}"
LABEL description="Image for the exploratory search microservice."

COPY ${JAR_FILE} /app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar", "/app.jar"]