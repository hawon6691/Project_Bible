---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Product API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Product API 구현"
commit: "feat: (#331) PHP Product API 구현"
branch: "feat/#331/php-product-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 상품 목록/상세 조회와 관리자 상품 관리 API를 구현한다.

## 📋 요구사항

- [x] Product 도메인 모델 추가
  - [x] `Product` 모델 생성
  - [x] `ProductSpec` 모델 생성
  - [x] `PriceEntry` 모델 생성
  - [x] `Seller` 모델 생성
  - [x] `category`, `specs`, `priceEntries`, `seller` 관계 정의
- [x] Product 요청 검증 클래스 추가
  - [x] `ListProductsRequest`
  - [x] `StoreProductRequest`
  - [x] `UpdateProductRequest`
- [x] Product 서비스 구현
  - [x] 상품 목록 조회
  - [x] 상품 상세 조회
  - [x] 관리자 상품 등록
  - [x] 관리자 상품 수정
  - [x] 관리자 상품 삭제
  - [x] 카테고리/검색어/가격 범위 필터 처리
  - [x] 정렬 처리(`newest`, `popularity`, `price_asc`, `price_desc`, `rating*`)
  - [x] `lowestPrice`, `sellerCount`, `specs`, `priceEntries` 직렬화
- [x] 관리자 권한 검증 추가
  - [x] 비관리자 접근 시 `FORBIDDEN` 에러 반환
- [x] Product 컨트롤러/라우트 연결
  - [x] `GET /api/v1/products`
  - [x] `GET /api/v1/products/{id}`
  - [x] `POST /api/v1/products`
  - [x] `PATCH /api/v1/products/{id}`
  - [x] `DELETE /api/v1/products/{id}`
- [x] 응답 구조 정리
  - [x] 목록 응답에 `items`, `pagination` 포함
  - [x] 상세 응답에 `category`, `specs`, `priceEntries` 포함
  - [x] 카멜 케이스 응답 필드(`lowestPrice`, `averagePrice`, `thumbnailUrl`) 사용
- [x] Product API 테스트 추가
  - [x] `tests/Feature/Api/ProductApiTest.php` 추가
  - [x] 공개 목록/상세 조회 검증
  - [x] 관리자 등록/수정/삭제 검증
  - [x] 비관리자 접근 차단 검증
  - [x] `php artisan test tests/Feature/Api/ProductApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list --path=api/v1/products` 통과
