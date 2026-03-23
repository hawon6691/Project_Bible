#!/usr/bin/env bash
set -euo pipefail

./gradlew dbInit
./gradlew dbSmoke
echo "Migration roundtrip check completed."
