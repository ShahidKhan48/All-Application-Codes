FROM maven:3.8.6-openjdk-8 as builder
ADD . .
RUN mvn clean install

FROM openjdk:8-jre-alpine
COPY --from=builder /target/*.jar app.jar
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.18.0/opentelemetry-javaagent.jar
ADD start-app.sh start-app.sh
CMD exec sh start-app.sh

