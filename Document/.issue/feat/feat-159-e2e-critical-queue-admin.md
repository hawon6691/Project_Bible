---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 핵심 E2E 세트에 Queue Admin 시나리오 포함"
labels: feature
issue: "[FEAT] 핵심 E2E 세트에 Queue Admin 시나리오 포함"
commit: "feat: (#159) e2e critical 세트에 queue-admin 시나리오 추가"
branch: "feat/#159/e2e-critical-queue-admin"
assignees: ""
---

## ✨ 기능 요약

> CI 핵심 E2E 세트에 Queue Admin 시나리오를 포함해 운영 복구 API 회귀를 상시 검증하도록 변경했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `test:e2e:critical` 스크립트에 `queue-admin.e2e-spec.ts` 추가
- [x] 기존 핵심 E2E(인증/공개API/운영API)와 함께 단일 runInBand 세트로 실행 유지
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] 핵심 E2E 통과 (`npm run test:e2e:critical`)


