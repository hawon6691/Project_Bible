---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Ranking Recommendation Deal Slice"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Ranking Recommendation Deal Slice 문서 작성"
commit: "feat: (#447) JavaScript Express Prisma 랭킹·추천·특가 API 구현"
branch: "feature/#447/javascript-express-prisma-ranking-recommendation-deal-slice"
---

## ✨ 기능 요약

> JavaScript Express Prisma 구현체에 랭킹, 추천, 특가 세일 API를 추가한다.

## 📋 요구사항

- [x] Prisma schema에 `RecommendationType` enum 추가
- [x] Prisma schema에 `Recommendation` 모델 추가
- [x] Prisma schema에 `Deal` 모델 추가
- [x] Prisma schema에 `DealProduct` 모델 추가
- [x] Prisma schema에 `SearchLog` 모델 추가
- [x] 기존 `User`, `Category`, `Product` 관계에 ranking/recommendation/deal relation 보강
- [x] `rankings` feature 폴더 추가
- [x] `recommendations` feature 폴더 추가
- [x] `deals` feature 폴더 추가
- [x] `routes/rankings.js` 추가
- [x] `routes/recommendations.js` 추가
- [x] `routes/deals.js` 추가
- [x] `GET /api/v1/rankings/products/popular` 구현
- [x] `GET /api/v1/rankings/keywords/popular` 구현
- [x] `GET /api/v1/rankings/searches` 구현
- [x] `POST /api/v1/rankings/admin/recalculate` 구현
- [x] `GET /api/v1/recommendations/today` 구현
- [x] `GET /api/v1/recommendations/personalized` 구현
- [x] `GET /api/v1/admin/recommendations` 구현
- [x] `POST /api/v1/admin/recommendations` 구현
- [x] `DELETE /api/v1/admin/recommendations/:id` 구현
- [x] `GET /api/v1/deals` 구현
- [x] `GET /api/v1/deals/:id` 구현
- [x] `POST /api/v1/deals` 구현
- [x] `PATCH /api/v1/deals/:id` 구현
- [x] `DELETE /api/v1/deals/:id` 구현
- [x] Prisma client 재생성
- [x] 샘플 데이터 기준 ranking/recommendation/deal 주요 API 응답 검증

## ✅ 산출물

- `BackEnd/JavaScript/expressshop_prismaorm/prisma/schema.prisma`
- `BackEnd/JavaScript/expressshop_prismaorm/src/rankings/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/recommendations/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/deals/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/rankings.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/recommendations.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/deals.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/index.js`

## 검증 메모

- `GET /api/v1/rankings/products/popular` 성공
- `GET /api/v1/rankings/searches` 성공
- `GET /api/v1/recommendations/today` 성공
- `GET /api/v1/recommendations/personalized` 성공
- `GET /api/v1/deals` 성공
- `POST /api/v1/admin/recommendations` 성공
- `GET /api/v1/admin/recommendations` 성공
- `POST /api/v1/deals` 성공
- `POST /api/v1/rankings/admin/recalculate` 성공
- Prisma schema 변경 후 `npm run prisma:generate` 재실행 완료

## 메모

- 랭킹은 `products.popularityScore`와 `search_logs` 집계를 기반으로 구현했다.
- 추천과 특가 생성은 샘플 데이터 삽입 이후 시퀀스가 어긋나는 문제를 피하기 위해 repository에서 시퀀스 보정을 포함했다.
- 특가 상세는 공통 명세의 확장 필드 전체를 다 가지진 않지만, 현재 공통 SQL 스키마 기준의 실사용 가능한 최소 구조로 구현했다.
- 다음 단계는 체크리스트 순서대로 `friend`, `shortform`, `media`, `news`, `matching` 슬라이스로 이어가는 것이 자연스럽다.
