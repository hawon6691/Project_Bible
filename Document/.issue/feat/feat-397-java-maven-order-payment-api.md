---
name: "Java Maven Order Payment API"
about: "Java Spring Boot Maven 주문/결제 API 구현"
title: "[FEAT] Java Maven 주문/결제 API 구현"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "order", "payment"]
assignees: []
issue: "[FEAT] Java Maven 주문/결제 API 구현"
commit: "feat: (#397) Java Maven 주문 결제 API 구현"
branch: "feat/#397/java-maven-order-payment-api"
---

## ✨ 기능 요약

Java Maven 기준 구현에 주문과 결제 도메인을 추가한다. 직접 주문과 장바구니 기반 주문을 모두 지원하고, 주문 시점 배송지 스냅샷과 금액 계산, mock PG 결제/환불 흐름, 관리자 주문 상태 변경까지 구현해 구매 플로우 핵심 축을 완성한다.

## 📋 요구사항

- [x] 스키마 확장
  - [x] `V6__order_payment_support.sql`
  - [x] `orders`
  - [x] `order_items`
  - [x] `payments`
- [x] Order 도메인 추가
  - [x] `Order`
  - [x] `OrderItem`
  - [x] `OrderRepository`
  - [x] `OrderService`
  - [x] `OrderController`
  - [x] `OrderDtos`
- [x] Payment 도메인 추가
  - [x] `Payment`
  - [x] `PaymentRepository`
  - [x] `PaymentService`
  - [x] `PaymentController`
  - [x] `PaymentDtos`
- [x] Order API 구현
  - [x] `POST /api/v1/orders`
  - [x] `GET /api/v1/orders`
  - [x] `GET /api/v1/orders/{id}`
  - [x] `POST /api/v1/orders/{id}/cancel`
  - [x] `GET /api/v1/admin/orders`
  - [x] `PATCH /api/v1/admin/orders/{id}/status`
- [x] Payment API 구현
  - [x] `POST /api/v1/payments`
  - [x] `GET /api/v1/payments/{id}`
  - [x] `POST /api/v1/payments/{id}/refund`
- [x] 주문/결제 규칙 구현
  - [x] 직접 주문과 장바구니 주문 둘 다 지원
  - [x] 배송지 스냅샷 저장
  - [x] `totalAmount`, `pointUsed`, `finalAmount` 계산
  - [x] 장바구니 주문 완료 후 해당 사용자 장바구니 비우기
  - [x] mock PG 결제 성공 시 주문 상태 `PAID` 반영
  - [x] 환불 시 결제 상태 `REFUNDED`, 주문 상태 `REFUNDED` 동기화
  - [x] 관리자 주문 목록/상태 변경 지원
- [x] 기존 계층 보강
  - [x] `PriceEntryRepository#findByProductIdAndSellerId`
  - [x] `FlywayMigrationTest`를 `v6` 기준으로 갱신
  - [x] `ORDERS`, `ORDER_ITEMS`, `PAYMENTS` 테이블 검증 추가
- [x] API 테스트 추가
  - [x] `OrderPaymentApiTest`
  - [x] 직접 주문 생성/조회/취소 검증
  - [x] 관리자 주문 목록/상태 변경 검증
  - [x] 장바구니 기반 주문 + 결제/환불 검증
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd -Dtest=OrderPaymentApiTest test`
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `11. 주문/결제 API 구현`에 해당한다.
- 다음 단계는 `12. 리뷰/위시리스트/포인트 API 구현`이다.
