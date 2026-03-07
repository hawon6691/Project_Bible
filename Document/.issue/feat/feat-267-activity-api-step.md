---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Activity API (Step 19) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Activity API (Step 19) 프론트엔드 연동"
commit: "feat: (#267) activity API 연동 및 Activity API 테스트 페이지 추가"
branch: "feat/#267/activity-api-step"
assignees: ""
---

## ✨ 기능 요약

> Help API(18번) 다음 단계인 Activity API(19번)를 프론트엔드에 연동하고, 수동 검증용 Activity API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Activity 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `ActivityRecentProductItem`
  - `ActivitySearchItem`
  - `ActivitySummary`
- [x] Activity 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchActivitySummary` (`GET /activities`) - 서버 구현 기준
  - `fetchRecentProducts` (`GET /activities/recent-products`) - 서버 구현 기준
  - `trackRecentProduct` (`POST /activities/recent-products/:productId`) - 서버 구현 기준
  - `fetchSearchHistory` (`GET /activities/searches`) - 서버 구현 기준
  - `addSearchHistory` (`POST /activities/searches`) - 서버 구현 기준
  - `removeSearchHistory` (`DELETE /activities/searches/:id`) - 서버 구현 기준
  - `clearSearchHistory` (`DELETE /activities/searches`) - 서버 구현 기준
- [x] Activity API 테스트 페이지 추가 (`FrontEnd/src/pages/ActivityApiPage.tsx`)
  - 활동 요약 조회
  - 최근 본 상품 조회/기록
  - 검색 기록 조회/추가/개별삭제/전체삭제
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/activity-api` 경로 추가
  - 상단 메뉴 `ActivityAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세 문서는 `/activity/views`, `/activity/searches` 경로를 정의
- [x] 현재 서버 구현은 `/activities/recent-products`, `/activities/searches`, `/activities` 요약 경로를 제공
- [x] 이번 프론트 단계는 서버 구현 기준으로 우선 반영
