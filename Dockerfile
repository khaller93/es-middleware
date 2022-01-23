# Builder Image
FROM maven:3-jdk-11 AS compiler

COPY . /esm
WORKDIR esm
RUN mvn package -DskipTests=true
RUN mkdir /binaries && mv appcore/target/esm.jar /binaries/

# Main Image
FROM openjdk:11-jdk

VOLUME /tmp

ARG ESM_VERSION

LABEL maintainer="Kevin Haller <keivn.haller@tuwien.ac.at>"
LABEL version="${ESM_VERSION}"
LABEL description="Image for the exploratory search microservice."

COPY --from=compiler /binaries/esm.jar /esm.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar", "/esm.jar"]