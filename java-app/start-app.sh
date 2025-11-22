#!/usr/bin/env sh
set -e

: "${SERVICE_NAME:=nc-platform-scheduler}"
: "${SPRING_PROFILE:=default}"
: "${JAVA_HEAP:=-Xmx2G}"
: "${TIMEZONE:=UTC}"

# Compose JAVA_OPTS (allow external JAVA_OPTS to append/override)
JAVA_OPTS="${JAVA_OPTS} \
  -javaagent:/opentelemetry-javaagent.jar \
  -Dotel.resource.attributes=service.name=${SERVICE_NAME} \
  -Duser.timezone=${TIMEZONE} \
  ${JAVA_HEAP} \
  -Dspring.profiles.active=${SPRING_PROFILE}"

exec java ${JAVA_OPTS} -jar /app.jar


