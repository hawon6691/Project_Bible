#!/usr/bin/env bash
set -euo pipefail

./mvnw -B -Dtest=ValidateMigrationsScriptTest test
