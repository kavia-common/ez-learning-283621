#!/usr/bin/env sh
# Small PATH-setup wrapper to ensure our mvn shim is found even if system Maven is absent.
# Prepends ./bin to PATH and executes the passed command.
# This wrapper also ensures 'mvn' runs even if the shim is not executable by delegating via 'sh'.

set -eu

# Prepend repo-local bin directory to PATH
PATH="$(pwd)/bin:$PATH"
export PATH

# If the first argument is 'mvn' and it is not executable, delegate via 'sh'
if [ "${1:-}" = "mvn" ]; then
  MVN_PATH="$(command -v mvn || true)"
  if [ -n "${MVN_PATH}" ] && [ ! -x "${MVN_PATH}" ]; then
    shift
    exec sh "${MVN_PATH}" "$@"
  fi
fi

# Execute the original command
exec "$@"
