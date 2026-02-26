---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Queue Admin 큐 상태 통계 API 추가"
labels: feature
issue: "[FEAT] Queue Admin 큐 상태 통계 API 추가"
commit: "feat: (#157) queue-admin 큐별 상태 카운트 통계 API 구현"
branch: "feat/#157/queue-admin-stats-api"
assignees: ""
---

## ✨ 기능 요약

> 운영자가 관리 대상 Bull 큐의 적체/실패 현황을 한 번에 확인할 수 있도록 큐 상태 통계 API를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Queue Admin 라우트 상수 확장 (`QUEUE_ADMIN.STATS`)
- [x] 큐 상태 통계 서비스 메서드 구현 (`getQueueStats`)
- [x] 큐 상태 통계 API 구현 (`GET /admin/queues/stats`)
- [x] Queue Admin E2E 테스트에 큐 통계 시나리오 추가
- [x] API 명세 문서 반영 (`02_api-specification.md`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] Queue Admin E2E 통과 (`npm run test:e2e -- queue-admin.e2e-spec.ts --runInBand`)

