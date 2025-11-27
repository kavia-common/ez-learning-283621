#!/usr/bin/env sh
set -eu

PROFILE="${SPRING_PROFILES_ACTIVE:-dev}"
PORT="${PORT:-8080}"

echo "Starting app with profile=${PROFILE} on port=${PORT} using Maven Wrapper..."
sh ./mvnw spring-boot:run \
  -Dspring-boot.run.profiles="${PROFILE}" \
  -Dspring-boot.run.arguments=--server.port="${PORT}"
