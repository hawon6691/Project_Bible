#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://127.0.0.1:8000}"
curl -fsS "$BASE_URL/health" >/dev/null
curl -fsS "$BASE_URL/api/v1/docs-status" >/dev/null
curl -fsS "$BASE_URL/docs/openapi" >/dev/null
echo "Live smoke passed for $BASE_URL"
