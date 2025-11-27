#!/usr/bin/env sh
set -eu

echo "Starting app on http://0.0.0.0:3001 using Maven Wrapper..."
exec sh mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=3001 --server.address=0.0.0.0
