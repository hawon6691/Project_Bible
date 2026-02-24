---
name: "🧪 Feature Request"
about: 새로운 기능 제안
title: "[TEST] 수동 테스트 확장 스위트(계약/안정성/확장부하) 추가"
labels: test
issue: "[TEST] 수동 테스트 확장 스위트(계약/안정성/확장부하) 추가"
commit: "test: (#207) workflow_dispatch 기반 계약/안정성/확장부하 검증 경로 추가"
branch: "test/#207/manual-test-expansion-suite"
assignees: ""
---

## 🧪 테스트 요약

> workflow_dispatch 수동 실행 경로에 계약 테스트, critical 안정성 체크, 확장 부하(soak/spike) 검증을 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 계약 테스트 E2E 추가 (`test/e2e/contract-public-api.e2e-spec.ts`)
- [x] 계약 테스트 실행 스크립트 추가 (`test:e2e:contract`)
- [x] critical 2회 연속 실행 안정성 스크립트 추가 (`test:e2e:critical:stability`)
- [x] 수동 계약 테스트 잡 추가 (`contract-e2e-manual`)
- [x] 수동 안정성 체크 잡 추가 (`stability-check-manual`)
- [x] soak/spike 성능 시나리오 추가 (`test/performance/soak.perf.js`, `test/performance/spike-search.perf.js`)
- [x] 확장 부하 실행 스크립트 추가 (`test:perf:soak`, `test:perf:spike`)
- [x] 수동 확장 부하 잡 추가 (`perf-extended-manual`)
- [x] 릴리스 체크리스트 아티팩트 항목 확장 (`contract/stability/perf-extended`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] 계약 테스트 통과 (`npm run test:e2e:contract -- --runInBand`)
