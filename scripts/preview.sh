#!/usr/bin/env sh
set -eu

echo "Starting app on http://0.0.0.0:3001 using Maven Wrapper..."
# Use quotes to ensure both args are passed correctly to Spring Boot
# Skip tests to speed up preview and avoid test failures blocking startup
exec sh mvnw spring-boot:run -DskipTests=true -Dspring-boot.run.arguments="--server.port=3001 --server.address=0.0.0.0"
