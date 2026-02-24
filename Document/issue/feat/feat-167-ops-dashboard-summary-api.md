---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 운영 통합 대시보드 요약 API 추가"
labels: feature
issue: "[FEAT] 운영 통합 대시보드 요약 API 추가"
commit: "feat: (#167) health/searchSync/crawler/queue 통합 요약 API 구현"
branch: "feat/#167/ops-dashboard-summary-api"
assignees: ""
---

## ✨ 기능 요약

> 운영자가 상태를 한 번에 점검할 수 있도록 Health, Search Outbox, Crawler, Queue 지표를 통합 조회하는 Admin API를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Ops Dashboard 모듈/서비스/컨트롤러 추가 (`src/ops-dashboard/*`)
- [x] 통합 요약 API 구현 (`GET /admin/ops-dashboard/summary`)
- [x] Health 지표 집계 연동 (`HealthService`)
- [x] Search Outbox 지표 집계 연동 (`SearchSyncService`)
- [x] Crawler 모니터링 지표 집계 연동 (`CrawlerService`)
- [x] Queue 통계 지표 집계 연동 (`QueueAdminService`)
- [x] 라우트 상수 확장 (`OPS_DASHBOARD`)
- [x] 앱 모듈 등록 (`OpsDashboardModule`)
- [x] Ops Dashboard E2E 테스트 추가 (`test/e2e/ops-dashboard.e2e-spec.ts`)
- [x] API 명세 문서 반영 (`03_api-specification.md`)
