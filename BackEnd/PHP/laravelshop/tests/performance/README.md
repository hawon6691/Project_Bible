# PHP Performance Assets

이 디렉터리는 `PBShop` PHP 구현체의 성능/스모크 검증 자산을 보관한다.

포함 자산:

- `mock-perf-server.php`
- `smoke.perf.js`
- `soak.perf.js`
- `spike-search.perf.js`
- `search-ranking.perf.js`
- `price-compare.perf.js`
- `assert-summary.php`

기본 실행 예시:

```bash
php -S 127.0.0.1:3310 tests/performance/mock-perf-server.php

docker run --rm --network host \
  -e BASE_URL=http://127.0.0.1:3310 \
  -v "$PWD:/work" \
  -w /work \
  grafana/k6:0.49.0 run \
  --summary-export /work/test-results/perf-smoke-summary.json \
  tests/performance/smoke.perf.js

php tests/performance/assert-summary.php test-results/perf-smoke-summary.json default
```
