#!/usr/bin/env sh
# POSIX-compliant shim to delegate any 'mvn' invocation to the Maven Wrapper.
# Using 'sh' avoids execute-bit issues on mvnw in some environments.
exec sh ./mvnw "$@"
