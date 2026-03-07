---
name: "🐛 Bug Report"
about: 버그 신고
title: "[BUG] Queue Admin 재시도 상태 검증 누락 수정"
labels: bug
issue: "[BUG] Queue Admin 재시도 상태 검증 누락 수정"
commit: "bug: (#161) queue-admin retryJob 실패 상태 검증 추가"
branch: "bug/#161/queue-admin-retry-state-guard"
assignees: ""
---

## 🐛 버그 요약

> Queue Admin `retryJob` API가 실패 상태가 아닌 Job도 재시도하도록 허용하던 문제를 수정했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `retryJob`에서 Job 상태 조회 로직 추가 (`job.getState()`)
- [x] `failed` 상태가 아니면 재시도 차단 및 `400 VALIDATION_FAILED` 반환
- [x] Queue Admin E2E에 비실패 상태 재시도 차단 시나리오 추가
- [x] Queue Admin E2E에 실패 상태 재시도 성공 시나리오 유지
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] E2E 테스트 통과 (`npm run test:e2e -- queue-admin.e2e-spec.ts --runInBand`)
