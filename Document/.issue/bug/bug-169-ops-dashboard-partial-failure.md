---
name: "🐛 Bug Report"
about: 버그 신고
title: "[BUG] Ops Dashboard 부분 장애 시 전체 실패 문제 수정"
labels: bug
issue: "[BUG] Ops Dashboard 부분 장애 시 전체 실패 문제 수정"
commit: "bug: (#169) ops-dashboard 부분 실패 허용 및 degraded 상태 응답 적용"
branch: "bug/#169/ops-dashboard-partial-failure"
assignees: ""
---

## 🐛 버그 요약

> Ops Dashboard가 하위 지표 중 하나만 실패해도 전체 API가 500으로 실패하던 문제를 수정했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `OpsDashboardService` 집계 로직을 `Promise.all`에서 `Promise.allSettled`로 변경
- [x] 부분 실패 시 `overallStatus: degraded` 반환 로직 추가
- [x] 실패 지표 `null` 처리 및 `errors` 필드에 원인 메시지 수집
- [x] 부분 실패 회복력 E2E 테스트 추가 (`ops-dashboard-resilience.e2e-spec.ts`)
- [x] API 명세서 응답/운영 규칙 갱신 (`02_api-specification.md`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] E2E 테스트 통과 (`npm run test:e2e -- ops-dashboard*.e2e-spec.ts --runInBand`)

