#!/usr/bin/env bash
set -euo pipefail

./mvnw -B -Dtest=AnalyzeStabilityScriptTest test
