---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] k6 성능 테스트 시나리오 구축"
labels: feature
issue: "[FEAT] k6 성능 테스트 시나리오 구축"
commit: "feat: (#137) k6 기반 smoke/search-ranking/price 성능 시나리오 추가"
branch: "feat/#137/performance-scenarios"
assignees: ""
---

## ✨ 기능 요약

> 검색/랭킹/가격비교 중심의 k6 성능 테스트 시나리오와 실행 스크립트를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 성능 테스트 실행 스크립트 추가 (`test:perf:*`)
- [x] Smoke 시나리오 추가 (`test/performance/smoke.perf.js`)
- [x] Search/Ranking 부하 시나리오 추가 (`test/performance/search-ranking.perf.js`)
- [x] Price 비교/이력 부하 시나리오 추가 (`test/performance/price-compare.perf.js`)
- [x] k6 실행 가이드 문서 추가 (`test/performance/README.md`)
- [x] 시나리오 임계치(threshold) 설정 추가 (`http_req_failed`, `http_req_duration`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)


