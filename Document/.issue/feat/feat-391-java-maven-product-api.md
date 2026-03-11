---
name: "Java Maven Product API"
about: "Java Spring Boot Maven 상품 API 구현"
title: "[FEAT] Java Maven 상품 API 구현"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "product"]
assignees: []
issue: "[FEAT] Java Maven 상품 API 구현"
commit: "feat: (#391) Java Maven 상품 API 구현"
branch: "feat/#391/java-maven-product-api"
---

## ✨ 기능 요약

Java Maven 기준 구현에 상품 도메인을 추가한다. 공개 상품 목록/상세 조회와 관리자 전용 상품 생성/수정/삭제를 구현하고, 카테고리/스펙/가격 비교 데이터까지 연결해 이후 가격 비교 흐름의 기반을 만든다.

## 📋 요구사항

- [x] Product 도메인 엔티티 및 저장소 추가
  - [x] `Product`
  - [x] `ProductSpec`
  - [x] `Seller`
  - [x] `PriceEntry`
  - [x] `ProductRepository`
  - [x] `ProductSpecRepository`
  - [x] `SellerRepository`
  - [x] `PriceEntryRepository`
- [x] Product 요청/응답 DTO 추가
  - [x] `SaveProductRequest`
- [x] Product 서비스 구현
  - [x] 상품 목록 조회
  - [x] 상품 상세 조회
  - [x] 관리자 상품 생성
  - [x] 관리자 상품 수정
  - [x] 관리자 상품 삭제
  - [x] `categoryId`, `search`, `minPrice`, `maxPrice`, `sort`, `page`, `limit` 처리
  - [x] `lowestPrice`, `averagePrice`, `sellerCount` 집계
  - [x] 상세 응답에 `category`, `specs`, `priceEntries` 포함
  - [x] slug 자동 생성/중복 검사
- [x] Product 컨트롤러 추가
  - [x] `GET /api/v1/products`
  - [x] `GET /api/v1/products/{id}`
  - [x] `POST /api/v1/products`
  - [x] `PATCH /api/v1/products/{id}`
  - [x] `DELETE /api/v1/products/{id}`
- [x] 보안 규칙 보강
  - [x] 공개 조회 엔드포인트 `permitAll`
  - [x] 관리자 전용 작업 `AUTH_403`
- [x] Product API 테스트 추가
  - [x] 공개 목록/상세 조회
  - [x] 관리자 생성/수정/삭제
  - [x] 비관리자 접근 차단
- [x] 구현 중 발견된 조회 이슈 수정
  - [x] nullable 검색 파라미터 처리 보강
  - [x] 목록 조회 가격 집계 테스트 데이터 연관관계 보강
  - [x] 상세 조회 다중 bag fetch 문제를 컬렉션 분리 조회로 해결
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd -Dtest=ProductApiTest test`
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `8. 상품(Product) API 구현`에 해당한다.
- 다음 단계는 `9. 스펙/판매처/가격 API 구현`이다.
