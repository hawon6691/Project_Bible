---
name: "🧪 Feature Request"
about: 새로운 기능 제안
title: "[TEST] 핵심 E2E 세트에 Ops Dashboard 회귀 포함"
labels: test
issue: "[TEST] 핵심 E2E 세트에 Ops Dashboard 회귀 포함"
commit: "test: (#171) e2e critical 세트에 ops-dashboard 정상/부분실패 시나리오 추가"
branch: "test/#171/e2e-critical-ops-dashboard"
assignees: ""
---

## 🧪 테스트 요약

> 운영 통합 대시보드 API의 정상/부분실패 회귀를 CI 핵심 E2E 세트에서 상시 검증하도록 확장했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `test:e2e:critical` 스크립트에 `ops-dashboard.e2e-spec.ts` 추가
- [x] `test:e2e:critical` 스크립트에 `ops-dashboard-resilience.e2e-spec.ts` 추가
- [x] 운영 API 정상 응답 회귀 시나리오를 핵심 E2E 세트에 포함
- [x] 운영 API 부분 실패(`degraded`) 응답 회귀 시나리오를 핵심 E2E 세트에 포함
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] 핵심 E2E 통과 (`npm run test:e2e:critical`)
