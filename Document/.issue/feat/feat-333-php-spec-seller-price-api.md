---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Spec Seller Price API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Spec Seller Price API 구현"
commit: "feat: (#333) PHP Spec Seller Price API 구현"
branch: "feat/#333/php-spec-seller-price-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 스펙 정의/상품 스펙, 판매처, 가격 비교 및 가격 알림 API를 구현한다.

## 📋 요구사항

- [x] 스펙/가격 보조 테이블 추가
  - [x] `spec_definitions` migration 추가
  - [x] `price_alerts` migration 추가
- [x] 도메인 모델 추가
  - [x] `SpecDefinition` 모델 추가
  - [x] `PriceAlert` 모델 추가
- [x] Spec 요청 검증/서비스/컨트롤러 추가
  - [x] `ListSpecDefinitionsRequest`
  - [x] `StoreSpecDefinitionRequest`
  - [x] `UpdateSpecDefinitionRequest`
  - [x] `SetProductSpecsRequest`
  - [x] `SpecService`
  - [x] `SpecController`
- [x] Spec API 구현
  - [x] `GET /api/v1/specs/definitions`
  - [x] `POST /api/v1/specs/definitions`
  - [x] `PATCH /api/v1/specs/definitions/{id}`
  - [x] `DELETE /api/v1/specs/definitions/{id}`
  - [x] `GET /api/v1/products/{id}/specs`
  - [x] `PUT /api/v1/products/{id}/specs`
- [x] Seller 요청 검증/서비스/컨트롤러 추가
  - [x] `ListSellersRequest`
  - [x] `StoreSellerRequest`
  - [x] `UpdateSellerRequest`
  - [x] `SellerService`
  - [x] `SellerController`
- [x] Seller API 구현
  - [x] `GET /api/v1/sellers`
  - [x] `GET /api/v1/sellers/{id}`
  - [x] `POST /api/v1/sellers`
  - [x] `PATCH /api/v1/sellers/{id}`
  - [x] `DELETE /api/v1/sellers/{id}`
- [x] Price 요청 검증/서비스/컨트롤러 추가
  - [x] `StorePriceEntryRequest`
  - [x] `UpdatePriceEntryRequest`
  - [x] `CreatePriceAlertRequest`
  - [x] `PriceHistoryRequest`
  - [x] `PriceService`
  - [x] `PriceController`
- [x] Price API 구현
  - [x] `GET /api/v1/products/{id}/prices`
  - [x] `POST /api/v1/products/{id}/prices`
  - [x] `PATCH /api/v1/prices/{id}`
  - [x] `DELETE /api/v1/prices/{id}`
  - [x] `GET /api/v1/products/{id}/price-history`
  - [x] `GET /api/v1/price-alerts`
  - [x] `POST /api/v1/price-alerts`
  - [x] `DELETE /api/v1/price-alerts/{id}`
- [x] 권한 검증 추가
  - [x] 스펙 정의/판매처/가격 삭제는 관리자 권한 검증
  - [x] 가격 등록/수정은 판매자 또는 관리자 권한 검증
  - [x] 가격 알림은 사용자 본인 기준 처리
- [x] 라우트 파일 분리 및 등록
  - [x] `routes/api_v1/specs.php`
  - [x] `routes/api_v1/sellers.php`
  - [x] `routes/api_v1/prices.php`
  - [x] `routes/api_v1.php`에 loader 연결
- [x] 통합 테스트 추가
  - [x] `tests/Feature/Api/SpecSellerPriceApiTest.php` 추가
  - [x] 스펙 정의/상품 스펙 설정 검증
  - [x] 판매처 생성/조회/삭제 검증
  - [x] 가격 등록/조회/수정/삭제 검증
  - [x] 가격 알림 생성/조회/삭제 검증
  - [x] 비관리자 가격 삭제 차단 검증
  - [x] `php artisan test tests/Feature/Api/SpecSellerPriceApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list --path=api/v1/specs` 통과
  - [x] `php artisan route:list --path=api/v1/sellers` 통과
  - [x] `php artisan route:list --path=api/v1/price` 통과
