---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 핵심 E2E 세트에 Ops Threshold 회귀 포함"
labels: feature
issue: "[FEAT] 핵심 E2E 세트에 Ops Threshold 회귀 포함"
commit: "feat: (#185) e2e critical 세트에 ops-dashboard-thresholds 시나리오 추가"
branch: "feat/#185/e2e-critical-thresholds"
assignees: ""
---

## ✨ 기능 요약

> Ops Dashboard 임계치 경보 회귀 테스트를 핵심 E2E 세트에 포함해 CI에서 상시 검증하도록 확장했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `test:e2e:critical` 스크립트에 `ops-dashboard-thresholds.e2e-spec.ts` 추가
- [x] Ops Dashboard 임계치 경보 회귀를 핵심 E2E 파이프라인에 편입
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] 핵심 E2E 통과 (`npm run test:e2e:critical`)


