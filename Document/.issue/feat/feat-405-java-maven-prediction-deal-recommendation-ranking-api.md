---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Java Maven Prediction Deal Recommendation Ranking API 구현"
labels: feature
assignees: ""
issue: "[FEAT] Java Maven Prediction Deal Recommendation Ranking API 구현"
commit: "feat: (#405) Java Maven Prediction Deal Recommendation Ranking API 구현"
branch: "feat/#405/java-maven-prediction-deal-recommendation-ranking-api"
---

## ✨ 기능 요약

> Java Maven 트랙에서 가격 예측, 특가, 추천, 랭킹 API를 구현한다.

## 📋 요구사항

- [x] `V10__prediction_deal_recommendation_ranking_support.sql` 마이그레이션 추가
- [x] Deal 도메인 추가
  - [x] `Deal`
  - [x] `DealRepository`
  - [x] `DealService`
  - [x] `DealController`
  - [x] `DealDtos`
- [x] Recommendation 도메인 추가
  - [x] `Recommendation`
  - [x] `RecommendationRepository`
  - [x] `RecommendationService`
  - [x] `RecommendationController`
  - [x] `RecommendationDtos`
- [x] Prediction API 추가
  - [x] `PredictionService`
  - [x] `PredictionController`
  - [x] `GET /api/v1/predictions/products/{productId}/price-trend`
- [x] Deal API 추가
  - [x] `GET /api/v1/deals`
  - [x] `GET /api/v1/deals/{id}`
  - [x] `GET /api/v1/admin/deals`
  - [x] `POST /api/v1/deals/admin`
  - [x] `PATCH /api/v1/deals/admin/{id}`
  - [x] `DELETE /api/v1/deals/admin/{id}`
- [x] Recommendation API 추가
  - [x] `GET /api/v1/recommendations/trending`
  - [x] `GET /api/v1/recommendations/personal`
  - [x] `GET /api/v1/admin/recommendations`
  - [x] `POST /api/v1/admin/recommendations`
  - [x] `DELETE /api/v1/admin/recommendations/{id}`
- [x] Ranking API 추가
  - [x] `GET /api/v1/rankings/products/popular`
  - [x] `GET /api/v1/rankings/keywords/popular`
  - [x] `POST /api/v1/rankings/admin/recalculate`
- [x] `SecurityConfig` 공개 경로 반영
- [x] `FlywayMigrationTest`를 v10 기준으로 갱신
- [x] 통합 테스트 추가
  - [x] `PredictionDealRecommendationRankingApiTest`
  - [x] 예측/특가 흐름 검증
  - [x] 추천/랭킹 흐름 검증
- [x] 전체 회귀 검증 통과
  - [x] `cmd /c mvnw.cmd -Dtest=PredictionDealRecommendationRankingApiTest test`
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- `PredictionService`는 기존 `price_entries` 이력을 이용해 추세, 이동평균, 추천 액션을 계산한다.
- `RankingService`는 `recent_product_views`, `search_histories`를 기반으로 인기 상품/검색어를 계산한다.
- 테스트 중 `Map.of` 제한, H2 스키마 검증, 람다 final 변수 문제를 수정해 안정화했다.
