---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Observability API (Step 51) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Observability API (Step 51) 프론트엔드 연동"
commit: "feat: (#305) observability API 연동 및 테스트 페이지 추가"
branch: "feat/#305/observability-api-step"
assignees: ""
---

## ✨ 기능 요약

> API 명세서 마지막 단계(51번)인 Observability API를 프론트엔드에 연동하고 수동 검증용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Observability 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `ObservabilityMetricsSummary`
  - `ObservabilityTraceItem`
  - `ObservabilityDashboard`
- [x] Observability 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchObservabilityMetricsAdmin` (`GET /admin/observability/metrics`)
  - `fetchObservabilityTracesAdmin` (`GET /admin/observability/traces`)
  - `fetchObservabilityDashboardAdmin` (`GET /admin/observability/dashboard`)
- [x] Observability API 테스트 페이지 추가 (`FrontEnd/src/pages/ObservabilityApiPage.tsx`)
  - 메트릭 요약 조회
  - 트레이스 조회(`limit`, `pathContains`)
  - 통합 대시보드 조회
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/observability-api` 경로 추가
  - 상단 메뉴 `ObservabilityAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세서는 51단계가 마지막이며 52단계 API는 별도 정의가 없어 단일 단계로 진행
- [x] Observability API는 Admin 전용이므로 관리자 토큰 기준으로 검증 필요
- [x] 트레이스 조회는 서버 DTO 기준으로 `limit(1~200)`, `pathContains` 쿼리를 사용
