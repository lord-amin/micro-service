FROM maven:3.6.3-jdk-11 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package -DskipTests

FROM openjdk:13-alpine3.10
ENV JAVA_OPTS ""
COPY --from=build /usr/src/app/target/discovery-service.jar /usr/app/discovery-service.jar
CMD java $JAVA_OPTS -jar /usr/app/discovery-service.jar $0 $@