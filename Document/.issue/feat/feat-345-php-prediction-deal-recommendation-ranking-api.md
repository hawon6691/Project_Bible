---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Prediction Deal Recommendation Ranking API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Prediction Deal Recommendation Ranking API 구현"
commit: "feat: (#345) PHP Prediction Deal Recommendation Ranking API 구현"
branch: "feat/#345/php-prediction-deal-recommendation-ranking-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 가격 예측, 특가, 추천, 랭킹 API를 구현한다.

## 📋 요구사항

- [x] Deal / Recommendation 테이블 추가
  - [x] `deals` migration 추가
  - [x] `recommendations` migration 추가
- [x] 도메인 모델 추가
  - [x] `Deal`
  - [x] `Recommendation`
- [x] Prediction 서비스/컨트롤러 추가
  - [x] `PredictionService`
  - [x] `PredictionController`
- [x] Prediction API 구현
  - [x] `GET /api/v1/predictions/products/{productId}/price-trend`
  - [x] 기존 `price_entries` 기반 가격 추세 계산
  - [x] 추세/이동평균/권장 액션 응답 구성
- [x] Deal 요청 검증/서비스/컨트롤러 추가
  - [x] `StoreDealRequest`
  - [x] `UpdateDealRequest`
  - [x] `DealService`
  - [x] `DealController`
- [x] Deal API 구현
  - [x] `GET /api/v1/deals`
  - [x] `POST /api/v1/deals/admin`
  - [x] `PATCH /api/v1/deals/admin/{id}`
  - [x] `DELETE /api/v1/deals/admin/{id}`
  - [x] 관리자 권한 검증
- [x] Recommendation 요청 검증/서비스/컨트롤러 추가
  - [x] `StoreRecommendationRequest`
  - [x] `RecommendationService`
  - [x] `RecommendationController`
- [x] Recommendation API 구현
  - [x] `GET /api/v1/recommendations/trending`
  - [x] `GET /api/v1/recommendations/personal`
  - [x] `GET /api/v1/admin/recommendations`
  - [x] `POST /api/v1/admin/recommendations`
  - [x] `DELETE /api/v1/admin/recommendations/{id}`
  - [x] 저장형 추천 + 폴백 추천 흐름 구성
- [x] Ranking 서비스/컨트롤러 추가
  - [x] `RankingService`
  - [x] `RankingController`
- [x] Ranking API 구현
  - [x] `GET /api/v1/rankings/products/popular`
  - [x] `GET /api/v1/rankings/keywords/popular`
  - [x] `POST /api/v1/rankings/admin/recalculate`
  - [x] 최근 본 상품/검색 기록 기반 랭킹 계산
- [x] 라우트 파일 분리 및 등록
  - [x] `routes/api_v1/prediction.php`
  - [x] `routes/api_v1/deals.php`
  - [x] `routes/api_v1/recommendations.php`
  - [x] `routes/api_v1/rankings.php`
  - [x] `routes/api_v1.php`에 loader 연결
- [x] 통합 테스트 추가
  - [x] `tests/Feature/Api/PredictionDealRecommendationRankingApiTest.php` 추가
  - [x] 가격 예측/특가 생성 수정 삭제 검증
  - [x] 추천 조회/관리 검증
  - [x] 상품 랭킹/검색어 랭킹/관리자 재계산 검증
  - [x] `php artisan test tests/Feature/Api/PredictionDealRecommendationRankingApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list --path=api/v1/predictions` 통과
  - [x] `php artisan route:list --path=api/v1/deals` 통과
  - [x] `php artisan route:list | Select-String 'recommendations|rankings'` 검증
