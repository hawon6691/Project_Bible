---
name: "🧪 Feature Request"
about: 새로운 기능 제안
title: "[TEST] 수동 테스트 확장 스위트(계약/안정성/확장부하) 추가"
labels: test
issue: "[TEST] 수동 테스트 확장 스위트(계약/안정성/확장부하) 추가"
commit: "test: (#209) workflow_dispatch 기반 계약/안정성/확장부하 검증 경로 추가"
branch: "test/#209/manual-test-expansion-suite"
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
- [x] 성능 요약 임계치 검증 스크립트 추가 (`test/performance/assert-summary.js`)
- [x] 수동 확장 부하 잡 추가 (`perf-extended-manual`)
- [x] 수동 확장 부하 잡에 임계치 자동 검증 단계 추가 (soak/spike)
- [x] 의존성 장애주입 E2E 추가 (`test/e2e/ops-dashboard-dependency-failures.e2e-spec.ts`)
- [x] 장애주입 E2E 실행 스크립트 추가 (`test:e2e:dependency-failure`)
- [x] 수동 장애주입 E2E 잡 추가 (`dependency-failure-manual`)
- [x] 관리자 권한 경계 E2E 추가 (`test/e2e/admin-authorization-boundary.e2e-spec.ts`)
- [x] 관리자 권한 경계 실행 스크립트 추가 (`test:e2e:admin-boundary`)
- [x] 수동 관리자 권한 경계 잡 추가 (`admin-boundary-manual`)
- [x] 보안 회귀 테스트 추가 (`test/e2e/security-regression.e2e-spec.ts`)
- [x] 보안 회귀 실행 스크립트 추가 (`test:security:regression`)
- [x] 수동 보안 회귀 잡 추가 (`security-regression-manual`)
- [x] 마이그레이션 정합성 검증 스크립트 추가 (`test/scripts/validate-migrations.js`)
- [x] 마이그레이션 검증 실행 스크립트 추가 (`test:migration:validate`)
- [x] 수동 마이그레이션 검증 잡 추가 (`migration-validation-manual`)
- [x] 실DB 마이그레이션 왕복 테스트 스크립트 추가 (`test/scripts/migration-roundtrip.js`)
- [x] 실DB 마이그레이션 왕복 실행 스크립트 추가 (`test:migration:roundtrip`)
- [x] 수동 마이그레이션 왕복 잡 추가 (`migration-roundtrip-manual`)
- [x] 안정성 분석 스크립트 추가 (`test/scripts/analyze-stability.js`)
- [x] 안정성 체크 스크립트에 flaky diff 자동 분석 연동 (`test:e2e:critical:stability`)
- [x] 릴리스 체크리스트 아티팩트 항목 확장 (`contract/stability/perf-extended`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] 계약 테스트 통과 (`npm run test:e2e:contract -- --runInBand`)
