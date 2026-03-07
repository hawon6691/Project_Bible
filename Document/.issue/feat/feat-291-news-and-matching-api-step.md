---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] News + Product Matching API (Step 37-38) 프론트엔드 연동"
labels: feature
issue: "[FEAT] News + Product Matching API (Step 37-38) 프론트엔드 연동"
commit: "feat: (#291) news+matching API 연동 및 테스트 페이지 추가"
branch: "feat/#291/news-matching-api-step"
assignees: ""
---

## ✨ 기능 요약

> 요청하신 2단계 묶음 방식으로 News API(37번), Product Matching API(38번)를 프론트엔드에 연동하고 수동 검증용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] News/Matching 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `NewsCategoryItem`, `NewsSummaryItem`, `NewsDetailItem`
  - `ProductMappingItem`
- [x] News 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchNewsCategories`, `fetchNewsDetail`
  - `createNewsAdmin`, `updateNewsAdmin`, `removeNewsAdmin`
  - `createNewsCategoryAdmin`, `removeNewsCategoryAdmin`
- [x] Matching 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchPendingMappings`
  - `approveMapping`, `rejectMapping`
  - `autoMatchMappings`, `fetchMappingStats`
- [x] News/Matching 테스트 페이지 추가
  - `FrontEnd/src/pages/NewsApiPage.tsx`
  - `FrontEnd/src/pages/MatchingApiPage.tsx`
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/news-api`, `/matching-api` 경로 추가
  - 상단 메뉴 `NewsAPI`, `MatchingAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] News list는 pagination envelope를 반환하므로 기존 `fetchNews`와 함께 상세/카테고리/Admin API를 보강해 반영
- [x] Matching API는 Admin 전용(`@Roles(Admin)`) 경로이므로 테스트 페이지는 관리자 토큰 기준으로 검증
