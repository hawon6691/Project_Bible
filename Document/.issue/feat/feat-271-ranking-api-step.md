---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Ranking API (Step 21) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Ranking API (Step 21) 프론트엔드 연동"
commit: "feat: (#271) ranking API 연동 및 Ranking API 테스트 페이지 추가"
branch: "feat/#271/ranking-api-step"
assignees: ""
---

## ✨ 기능 요약

> Chat API(20번) 다음 단계인 Ranking API(21번)를 프론트엔드에 연동하고, 수동 검증용 Ranking API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Ranking 엔드포인트 함수 추가/보강 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchRankingProducts` (`GET /rankings/products/popular`)
  - `fetchRankingKeywords` (`GET /rankings/keywords/popular`)
  - `recalculateRankingAdmin` (`POST /rankings/admin/recalculate`)
- [x] Ranking API 테스트 페이지 추가 (`FrontEnd/src/pages/RankingApiPage.tsx`)
  - 인기 상품 랭킹 조회
  - 인기 검색어 랭킹 조회
  - 인기 점수 재계산(Admin)
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/ranking-api` 경로 추가
  - 상단 메뉴 `RankingAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세 문서는 `/rankings/products`, `/rankings/searches`, `/rankings/price-drops` 형태를 정의
- [x] 현재 서버 구현은 `/rankings/products/popular`, `/rankings/keywords/popular`, `/rankings/admin/recalculate`를 제공
- [x] 이번 프론트 단계는 서버 구현 기준으로 우선 반영
