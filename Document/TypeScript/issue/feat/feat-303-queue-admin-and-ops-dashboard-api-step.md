---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Queue Admin + Ops Dashboard API (Step 49-50) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Queue Admin + Ops Dashboard API (Step 49-50) 프론트엔드 연동"
commit: "feat: (#303) queue-admin+ops-dashboard API 연동 및 테스트 페이지 추가"
branch: "feat/#303/queue-admin-ops-dashboard-api-step"
assignees: ""
---

## ✨ 기능 요약

> 요청하신 2단계 묶음 방식으로 Queue Admin API(49번), Ops Dashboard API(50번)를 프론트엔드에 연동하고 수동 검증용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Queue Admin/Ops Dashboard 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `QueueJobCounts`, `QueueStatsItem`, `QueueStatsResult`
  - `QueueFailedJobItem`, `QueueFailedJobsResult`
  - `QueueRetryFailedResult`, `QueueAutoRetryResult`
  - `OpsDashboardSummary`
- [x] Queue Admin 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchSupportedQueuesAdmin` (`GET /admin/queues/supported`)
  - `fetchQueueStatsAdmin` (`GET /admin/queues/stats`)
  - `fetchQueueFailedJobsAdmin` (`GET /admin/queues/:queueName/failed`)
  - `retryQueueFailedJobsAdmin` (`POST /admin/queues/:queueName/failed/retry`)
  - `autoRetryQueuesAdmin` (`POST /admin/queues/auto-retry`)
  - `retryQueueJobAdmin` (`POST /admin/queues/:queueName/jobs/:jobId/retry`)
  - `removeQueueJobAdmin` (`DELETE /admin/queues/:queueName/jobs/:jobId`)
- [x] Ops Dashboard 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchOpsDashboardSummaryAdmin` (`GET /admin/ops-dashboard/summary`)
- [x] Queue Admin/Ops Dashboard 테스트 페이지 추가
  - `FrontEnd/src/pages/QueueAdminApiPage.tsx`
  - `FrontEnd/src/pages/OpsDashboardApiPage.tsx`
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/queue-admin-api`, `/ops-dashboard-api` 경로 추가
  - 상단 메뉴 `QueueAdminAPI`, `OpsDashboardAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] Queue 실패 Job 조회 응답은 `PaginationResponseDto` 형태(`items`, `meta`)이므로 해당 구조로 타입/렌더링 반영
- [x] Queue Admin/Ops Dashboard API 모두 Admin 권한 전용으로 관리자 토큰 기준 검증 필요
- [x] Ops Dashboard는 하위 지표 실패 시에도 200 응답 + `overallStatus: degraded`를 반환하므로 전체 JSON 표시 기반으로 검증 UI 구성
