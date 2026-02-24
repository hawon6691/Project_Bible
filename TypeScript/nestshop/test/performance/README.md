# Performance Test Scenarios

`k6` 기반 성능 테스트 시나리오입니다.

## Prerequisites

- API 서버 실행
- `k6` 설치: https://k6.io/docs/get-started/installation/

## Run

```bash
# smoke
BASE_URL=http://localhost:3000/api/v1 npm run test:perf:smoke

# search/ranking
BASE_URL=http://localhost:3000/api/v1 npm run test:perf:search-ranking

# price compare/history
BASE_URL=http://localhost:3000/api/v1 PRODUCT_ID_MIN=1 PRODUCT_ID_MAX=100 npm run test:perf:price

# soak
BASE_URL=http://localhost:3000/api/v1 SOAK_DURATION=10m npm run test:perf:soak

# spike
BASE_URL=http://localhost:3000/api/v1 npm run test:perf:spike
```

## Scenarios

- `smoke.perf.js`
- `search-ranking.perf.js`
- `price-compare.perf.js`
- `soak.perf.js`
- `spike-search.perf.js`
