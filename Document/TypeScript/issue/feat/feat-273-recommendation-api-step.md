---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Recommendation API (Step 22) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Recommendation API (Step 22) 프론트엔드 연동"
commit: "feat: (#273) recommendation API 연동 및 Recommendation API 테스트 페이지 추가"
branch: "feat/#273/recommendation-api-step"
assignees: ""
---

## ✨ 기능 요약

> Ranking API(21번) 다음 단계인 Recommendation API(22번)를 프론트엔드에 연동하고, 수동 검증용 Recommendation API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Recommendation 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `RecommendationItem`
  - `RecommendationResult`
- [x] Recommendation 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchRecommendationTrending` (`GET /recommendations/trending`)
  - `fetchRecommendationPersonal` (`GET /recommendations/personal`)
- [x] Recommendation API 테스트 페이지 추가 (`FrontEnd/src/pages/RecommendationApiPage.tsx`)
  - 트렌딩 추천 조회
  - 개인화 추천 조회
  - source/items 응답 확인
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/recommendation-api` 경로 추가
  - 상단 메뉴 `RecommendationAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세 문서는 `/recommendations/today`, `/recommendations/personalized`, `/admin/recommendations` 계열을 정의
- [x] 현재 서버 구현(`recommendation.controller.ts`)은 `/recommendations/trending`, `/recommendations/personal`만 제공
- [x] Admin 추천 CRUD endpoint는 현재 서버에 없어 이번 프론트 단계에서는 제외
