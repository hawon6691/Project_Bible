---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Order Payment API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Order Payment API 구현"
commit: "feat: (#337) PHP Order Payment API 구현"
branch: "feat/#337/php-order-payment-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 주문 생성/조회/취소 및 결제 요청/조회/환불 API를 구현한다.

## 📋 요구사항

- [x] 주문/결제 테이블 추가
  - [x] `orders` migration 추가
  - [x] `order_items` migration 추가
  - [x] `payments` migration 추가
- [x] 도메인 모델 추가
  - [x] `Order` 모델 추가
  - [x] `OrderItem` 모델 추가
  - [x] `Payment` 모델 추가
- [x] Order 요청 검증/서비스/컨트롤러 추가
  - [x] `CreateOrderRequest`
  - [x] `ListOrdersRequest`
  - [x] `UpdateOrderStatusRequest`
  - [x] `OrderService`
  - [x] `OrderController`
- [x] Order API 구현
  - [x] `POST /api/v1/orders`
  - [x] `GET /api/v1/orders`
  - [x] `GET /api/v1/orders/{id}`
  - [x] `POST /api/v1/orders/{id}/cancel`
  - [x] `GET /api/v1/admin/orders`
  - [x] `PATCH /api/v1/admin/orders/{id}/status`
- [x] 주문 생성 로직 구현
  - [x] 배송지 스냅샷 저장
  - [x] 직접 아이템 주문 지원
  - [x] 카트 기반 주문(`fromCart`, `cartItemIds`) 지원
  - [x] `totalAmount`, `pointUsed`, `finalAmount` 계산
  - [x] 카트 주문 완료 후 선택 항목 삭제
- [x] Payment 요청 검증/서비스/컨트롤러 추가
  - [x] `CreatePaymentRequest`
  - [x] `PaymentService`
  - [x] `PaymentController`
- [x] Payment API 구현
  - [x] `POST /api/v1/payments`
  - [x] `GET /api/v1/payments/{id}`
  - [x] `POST /api/v1/payments/{id}/refund`
- [x] 결제 처리 로직 구현
  - [x] mock 결제 번호/PG 참조 번호 생성
  - [x] 결제 생성 시 `PAID` 상태 반영
  - [x] 환불 시 `REFUNDED` 상태 및 환불 시각 반영
  - [x] 환불 시 주문 상태도 `REFUNDED`로 동기화
- [x] 라우트 파일 분리 및 등록
  - [x] `routes/api_v1/orders.php`
  - [x] `routes/api_v1/payments.php`
  - [x] `routes/api_v1.php`에 loader 연결
- [x] 통합 테스트 추가
  - [x] `tests/Feature/Api/OrderPaymentApiTest.php` 추가
  - [x] 카트 기반 주문 생성 검증
  - [x] 내 주문 목록/상세 조회 검증
  - [x] 관리자 주문 목록/상태 변경 검증
  - [x] 결제 생성/조회/환불 검증
  - [x] 주문 취소 검증
  - [x] `php artisan test tests/Feature/Api/OrderPaymentApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list --path=api/v1/orders` 통과
  - [x] `php artisan route:list --path=api/v1/admin/orders` 통과
  - [x] `php artisan route:list --path=api/v1/payments` 통과
