#!/usr/bin/env bash
set -euo pipefail

./gradlew dbBootstrap
./gradlew dbSmoke
echo "Migration validation completed."
