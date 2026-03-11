---
name: "Java Maven Cart Address API"
about: "Java Spring Boot Maven 장바구니/배송지 API 구현"
title: "[FEAT] Java Maven 장바구니/배송지 API 구현"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "cart", "address"]
assignees: []
issue: "[FEAT] Java Maven 장바구니/배송지 API 구현"
commit: "feat: (#395) Java Maven 장바구니 배송지 API 구현"
branch: "feat/#395/java-maven-cart-address-api"
---

## ✨ 기능 요약

Java Maven 기준 구현에 배송지와 장바구니 도메인을 추가한다. 사용자별 기본 배송지 관리, 장바구니 항목 병합, 가격 비교 기반 합계 계산을 구현해 주문/결제 단계에서 사용할 구매 준비 계층을 완성한다.

## 📋 요구사항

- [x] 스키마 확장
  - [x] `V5__cart_address_support.sql`
  - [x] `addresses`
  - [x] `cart_items`
- [x] Address 도메인 추가
  - [x] `Address`
  - [x] `AddressRepository`
  - [x] `AddressService`
  - [x] `AddressController`
  - [x] `AddressDtos`
- [x] Cart 도메인 추가
  - [x] `CartItem`
  - [x] `CartItemRepository`
  - [x] `CartService`
  - [x] `CartController`
  - [x] `CartDtos`
- [x] Address API 구현
  - [x] `GET /api/v1/addresses`
  - [x] `POST /api/v1/addresses`
  - [x] `PATCH /api/v1/addresses/{id}`
  - [x] `DELETE /api/v1/addresses/{id}`
- [x] Cart API 구현
  - [x] `GET /api/v1/cart`
  - [x] `POST /api/v1/cart`
  - [x] `PATCH /api/v1/cart/{itemId}`
  - [x] `DELETE /api/v1/cart/{itemId}`
  - [x] `DELETE /api/v1/cart`
- [x] 장바구니/배송지 규칙 구현
  - [x] 첫 배송지 생성 시 기본 배송지 자동 지정
  - [x] 기본 배송지 변경 시 기존 기본값 해제
  - [x] 기본 배송지 삭제 시 다음 주소 승격
  - [x] 동일 `product + seller + selectedOptions` 조합 장바구니 항목 병합
  - [x] 장바구니 요약 `itemCount`, `totalQuantity`, `totalAmount` 계산
- [x] 조회 최적화 및 직렬화 보강
  - [x] `CartItemRepository` 엔티티 그래프 보강
  - [x] `selectedOptions` JSON 직렬화/역직렬화 처리
  - [x] 판매처별 가격 비교 엔트리 기반 금액 계산
- [x] 구현 중 보강 사항
  - [x] `FlywayMigrationTest`를 `v5` 기준으로 갱신
  - [x] `ADDRESSES`, `CART_ITEMS` 테이블 검증 추가
  - [x] Surefire 테스트 탐지 충돌 해결
  - [x] `SystemControllerTestSupport`를 `ApiIntegrationTestSupport`로 리네임
- [x] API 테스트 추가
  - [x] `CartAddressApiTest`
  - [x] 배송지 생성/조회/수정/삭제 검증
  - [x] 장바구니 추가/병합/수정/삭제/전체 비우기 검증
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd -Dtest=CartAddressApiTest test`
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `10. 장바구니/배송지 API 구현`에 해당한다.
- 다음 단계는 `11. 주문/결제 API 구현`이다.
