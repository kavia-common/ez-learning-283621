#!/usr/bin/env sh
set -eu

PROFILE="${SPRING_PROFILES_ACTIVE:-dev}"
PORT="${PORT:-3001}"
ADDRESS="0.0.0.0"

echo "Starting app with profile=${PROFILE} on http://${ADDRESS}:${PORT} using Maven Wrapper..."
exec sh mvnw spring-boot:run \
  -Dspring-boot.run.profiles="${PROFILE}" \
  -Dspring-boot.run.arguments="--server.port=${PORT} --server.address=${ADDRESS}"
