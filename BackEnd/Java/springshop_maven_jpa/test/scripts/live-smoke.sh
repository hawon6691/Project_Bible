#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${LIVE_SMOKE_BASE_URL:-http://127.0.0.1:8000}"

curl -fsS "${BASE_URL}/api/v1/health" >/dev/null
curl -fsS "${BASE_URL}/api/v1/products" >/dev/null
curl -fsS "${BASE_URL}/docs/openapi" >/dev/null
