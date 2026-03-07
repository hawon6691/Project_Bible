---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Admin Settings + Health API (Step 45-46) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Admin Settings + Health API (Step 45-46) 프론트엔드 연동"
commit: "feat: (#299) admin-settings+health API 연동 및 테스트 페이지 추가"
branch: "feat/#299/admin-settings-health-api-step"
assignees: ""
---

## ✨ 기능 요약

> 요청하신 2단계 묶음 방식으로 Admin Settings API(45번), Health API(46번)를 프론트엔드에 연동하고 수동 검증용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Admin Settings/Health 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `AdminAllowedExtensionsResult`
  - `AdminUploadLimitsResult`
  - `AdminReviewPolicyResult`
  - `HealthCheckResult`
- [x] Admin Settings 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchAllowedExtensionsAdmin` (`GET /admin/settings/extensions`)
  - `setAllowedExtensionsAdmin` (`POST /admin/settings/extensions`)
  - `fetchUploadLimitsAdmin` (`GET /admin/settings/upload-limits`)
  - `updateUploadLimitsAdmin` (`PATCH /admin/settings/upload-limits`)
  - `fetchReviewPolicyAdmin` (`GET /admin/settings/review-policy`)
  - `updateReviewPolicyAdmin` (`PATCH /admin/settings/review-policy`)
- [x] Health 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchHealthCheck` (`GET /health`)
- [x] Admin Settings/Health 테스트 페이지 추가
  - `FrontEnd/src/pages/AdminSettingsApiPage.tsx`
  - `FrontEnd/src/pages/HealthApiPage.tsx`
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/admin-settings-api`, `/health-api` 경로 추가
  - 상단 메뉴 `AdminSettingsAPI`, `HealthAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] Admin Settings API는 Admin 권한 전용이므로 관리자 토큰 기준으로 검증 필요
- [x] Health API는 공개 엔드포인트로 DB/Redis/Elasticsearch 상태를 `up | degraded | down`으로 반환하므로 결과 전체 표시 UI로 반영
