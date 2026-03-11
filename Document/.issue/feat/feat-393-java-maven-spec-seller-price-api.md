---
name: "Java Maven Spec Seller Price API"
about: "Java Spring Boot Maven 스펙/판매처/가격 API 구현"
title: "[FEAT] Java Maven 스펙/판매처/가격 API 구현"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "spec", "seller", "price"]
assignees: []
issue: "[FEAT] Java Maven 스펙/판매처/가격 API 구현"
commit: "feat: (#393) Java Maven 스펙 판매처 가격 API 구현"
branch: "feat/#393/java-maven-spec-seller-price-api"
---

## ✨ 기능 요약

Java Maven 기준 구현에 스펙 정의, 상품 스펙 값, 판매처, 가격 비교, 가격 이력, 가격 알림 도메인을 추가한다. 공개 가격 비교 흐름과 관리자/판매자 관리 기능을 함께 구현해 이후 장바구니와 주문 단계가 참조할 비교 데이터 계층을 완성한다.

## 📋 요구사항

- [x] 스키마 확장
  - [x] `V4__spec_seller_price_support.sql`
  - [x] `spec_definitions`
  - [x] `price_alerts`
- [x] Spec 도메인 추가
  - [x] `SpecDefinition`
  - [x] `SpecDefinitionRepository`
  - [x] `SpecService`
  - [x] `SpecController`
  - [x] `SaveSpecDefinitionRequest`
  - [x] `SetProductSpecsRequest`
- [x] Seller 도메인 보강
  - [x] `SellerRepository` 검색/중복 검사 메서드 확장
  - [x] `SellerService`
  - [x] `SellerController`
  - [x] `SaveSellerRequest`
- [x] Price 도메인 추가/보강
  - [x] `PriceAlert`
  - [x] `PriceAlertRepository`
  - [x] `PriceEntryRepository` 상세/이력 조회 보강
  - [x] `PriceService`
  - [x] `PriceController`
  - [x] `SavePriceEntryRequest`
  - [x] `UpdatePriceEntryRequest`
  - [x] `CreatePriceAlertRequest`
- [x] Spec API 구현
  - [x] `GET /api/v1/specs/definitions`
  - [x] `POST /api/v1/specs/definitions`
  - [x] `PATCH /api/v1/specs/definitions/{id}`
  - [x] `DELETE /api/v1/specs/definitions/{id}`
  - [x] `GET /api/v1/products/{productId}/specs`
  - [x] `PUT /api/v1/products/{productId}/specs`
- [x] Seller API 구현
  - [x] `GET /api/v1/sellers`
  - [x] `GET /api/v1/sellers/{id}`
  - [x] `POST /api/v1/sellers`
  - [x] `PATCH /api/v1/sellers/{id}`
  - [x] `DELETE /api/v1/sellers/{id}`
- [x] Price API 구현
  - [x] `GET /api/v1/products/{productId}/prices`
  - [x] `POST /api/v1/products/{productId}/prices`
  - [x] `PATCH /api/v1/prices/{id}`
  - [x] `DELETE /api/v1/prices/{id}`
  - [x] `GET /api/v1/products/{productId}/price-history`
  - [x] `GET /api/v1/price-alerts`
  - [x] `POST /api/v1/price-alerts`
  - [x] `DELETE /api/v1/price-alerts/{id}`
- [x] 보안 규칙 보강
  - [x] 공개 조회 엔드포인트 `permitAll`
  - [x] 스펙 정의/판매처 관리 `ADMIN`
  - [x] 가격 등록/수정 `SELLER | ADMIN`
  - [x] 가격 삭제 `ADMIN`
  - [x] 가격 알림은 본인 기준으로만 관리
- [x] 구현 중 보강 사항
  - [x] `PriceEntry#getCheckedAt` getter 추가
  - [x] 가격 이력 응답에서 null-safe map 구성
  - [x] `FlywayMigrationTest`를 `v4` 기준으로 갱신
  - [x] `SPEC_DEFINITIONS`, `PRICE_ALERTS` 테이블 검증 추가
- [x] API 테스트 추가
  - [x] `SpecSellerPriceApiTest`
  - [x] 공개 조회 검증
  - [x] 관리자 스펙 정의/판매처 관리 검증
  - [x] 판매자/관리자 가격 관리 검증
  - [x] 사용자 가격 알림 검증
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd -Dtest=SpecSellerPriceApiTest test`
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `9. 스펙/판매처/가격 API 구현`에 해당한다.
- 다음 단계는 `10. 장바구니/배송지 API 구현`이다.
