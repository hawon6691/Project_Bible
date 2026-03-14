#!/usr/bin/env bash
set -euo pipefail

./mvnw -B -Dtest=MigrationRoundtripScriptTest test
