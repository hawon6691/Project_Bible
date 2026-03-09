---
name: "✨ Feature Request"
about: "새 기능 추가 및 구현 작업"
title: "[FEAT] PHP Analytics Used Market Auto Auction Compare API 구현"
labels: ["feature"]
issue: "[FEAT] PHP Analytics Used Market Auto Auction Compare API 구현"
commit: "feat: (#351) PHP Analytics Used Market Auto Auction Compare API 구현"
branch: "feat/#351/php-analytics-used-market-auto-auction-compare-api"
---

## ✨ 기능 요약
PHP Laravel 백엔드에 가격 분석, 중고 시세, 자동차 견적, 역경매, 비교함 API를 추가해 PBShop 공통 API 계약을 확장한다.

## 📋 요구사항
- [x] Analytics API 구현
  - [x] `GET /api/v1/analytics/products/{id}/lowest-ever`
  - [x] `GET /api/v1/analytics/products/{id}/unit-price`
- [x] Used Market API 구현
  - [x] `GET /api/v1/used-market/products/{id}/price`
  - [x] `GET /api/v1/used-market/categories/{id}/prices`
  - [x] `POST /api/v1/used-market/pc-builds/{buildId}/estimate`
- [x] Auto API 구현
  - [x] `GET /api/v1/auto/models`
  - [x] `GET /api/v1/auto/models/{id}/trims`
  - [x] `POST /api/v1/auto/estimate`
  - [x] `GET /api/v1/auto/models/{id}/lease-offers`
- [x] Auction API 구현
  - [x] 역경매 등록/목록/상세
  - [x] 입찰 등록/수정/삭제
  - [x] 낙찰 선택/역경매 취소
- [x] Compare API 구현
  - [x] 상품 추가/삭제
  - [x] 비교함 목록
  - [x] 비교 상세
- [x] 라우트 등록 (`routes/api_v1.php`)
- [x] Feature 테스트 추가 (`tests/Feature/Api/AnalyticsUsedMarketAutoAuctionCompareApiTest.php`)
- [x] 라우트/테스트 검증 통과
