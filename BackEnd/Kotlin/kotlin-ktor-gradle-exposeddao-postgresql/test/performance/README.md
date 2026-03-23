# PBShop Kotlin Performance Assets

이 폴더는 Kotlin 기준 구현체의 성능 smoke/extended 자산을 보관합니다.

## Files

- `smoke.perf.js`
- `soak.perf.js`
- `spike-search.perf.js`
- `price-compare.perf.js`
- `search-ranking.perf.js`
- `assert-summary.js`
- `mock-perf-server.js`

각 스크립트는 요약 JSON 파일 경로를 첫 번째 인자로 받을 수 있습니다.

예시:

```bash
node test/performance/smoke.perf.js test-results/perf-smoke-summary.json
node test/performance/assert-summary.js test-results/perf-smoke-summary.json
```
