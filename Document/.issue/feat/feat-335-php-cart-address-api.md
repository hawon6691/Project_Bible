---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Cart Address API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Cart Address API 구현"
commit: "feat: (#335) PHP Cart Address API 구현"
branch: "feat/#335/php-cart-address-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 장바구니와 배송지 API를 구현한다.

## 📋 요구사항

- [x] 장바구니/배송지 테이블 추가
  - [x] `addresses` migration 추가
  - [x] `cart_items` migration 추가
- [x] 도메인 모델 추가
  - [x] `Address` 모델 추가
  - [x] `CartItem` 모델 추가
- [x] Address 요청 검증/서비스/컨트롤러 추가
  - [x] `StoreAddressRequest`
  - [x] `UpdateAddressRequest`
  - [x] `AddressService`
  - [x] `AddressController`
- [x] Address API 구현
  - [x] `GET /api/v1/addresses`
  - [x] `POST /api/v1/addresses`
  - [x] `PATCH /api/v1/addresses/{id}`
  - [x] `DELETE /api/v1/addresses/{id}`
  - [x] 기본 배송지 전환 처리
- [x] Cart 요청 검증/서비스/컨트롤러 추가
  - [x] `StoreCartItemRequest`
  - [x] `UpdateCartItemRequest`
  - [x] `CartService`
  - [x] `CartController`
- [x] Cart API 구현
  - [x] `GET /api/v1/cart`
  - [x] `POST /api/v1/cart`
  - [x] `PATCH /api/v1/cart/{itemId}`
  - [x] `DELETE /api/v1/cart/{itemId}`
  - [x] `DELETE /api/v1/cart`
  - [x] 동일 상품/판매처/옵션 조합 병합 처리
- [x] 라우트 파일 분리 및 등록
  - [x] `routes/api_v1/cart.php`
  - [x] `routes/api_v1/addresses.php`
  - [x] `routes/api_v1.php`에 loader 연결
- [x] 통합 테스트 추가
  - [x] `tests/Feature/Api/CartAddressApiTest.php` 추가
  - [x] 배송지 생성/조회/수정/삭제 검증
  - [x] 장바구니 추가/병합/조회/수량 변경/삭제/전체 비우기 검증
  - [x] `php artisan test tests/Feature/Api/CartAddressApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list --path=api/v1/cart` 통과
  - [x] `php artisan route:list --path=api/v1/addresses` 통과
