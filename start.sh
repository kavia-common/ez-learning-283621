#!/usr/bin/env bash
# Helper script to run the Spring Boot app via Maven Wrapper on the expected port/address for previews.

set -euo pipefail

# Ensure we are in the project root (where mvnw exists)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# PUBLIC_INTERFACE
# start.sh entrypoint
# This script is intended for preview environments and local usage.
# It runs the Spring Boot application using the Maven Wrapper, binding to port 3001 and 0.0.0.0.
# Usage:
#   ./start.sh
# Behavior:
#   - Uses the dev profile by default (as configured in application.properties via APP_PROFILE env or default).
#   - Binds server to 0.0.0.0 so external preview can reach it.
#   - Uses port 3001 as required by the environment.
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=3001 --server.address=0.0.0.0"
