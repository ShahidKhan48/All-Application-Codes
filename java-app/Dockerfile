# ---------- Stage 1: Build ----------
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn -B -ntp -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -B -ntp -DskipTests clean package


# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /

# Copy the built jar
COPY --from=builder /build/target/nc-platform-scheduler-*.jar /app.jar

# Copy the start script
COPY start-app.sh /start-app.sh
RUN chmod +x /start-app.sh

# Optional: OpenTelemetry Java agent
RUN wget -q https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.26.0/opentelemetry-javaagent.jar -O /opentelemetry-javaagent.jar

# Default ports
EXPOSE 8080 8000

# Use start-app.sh to launch the service
CMD ["/start-app.sh"]


