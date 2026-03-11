---
name: "Java Maven Category API"
about: "Java Spring Boot Maven 카테고리 API 구현"
title: "[FEAT] Java Maven 카테고리 API 구현"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "category"]
assignees: []
issue: "[FEAT] Java Maven 카테고리 API 구현"
commit: "feat: (#389) Java Maven 카테고리 API 구현"
branch: "feat/#389/java-maven-category-api"
---

## ✨ 기능 요약

Java Maven 기준 구현에 계층형 카테고리 도메인을 추가한다. 공개 카테고리 트리 조회와 단건 조회를 제공하고, 관리자 전용 카테고리 생성/수정/삭제를 구현해 이후 상품 도메인이 참조할 기준 카테고리 구조를 고정한다.

## 📋 요구사항

- [x] Category 엔티티 및 저장소 추가
  - [x] `Category`
  - [x] self reference `parent`, `children`
  - [x] `slug`, `depth`, `sortOrder`, `isVisible`
  - [x] `CategoryRepository`
  - [x] 트리 조회/slug 중복/하위 카테고리 존재 확인 메서드 추가
- [x] Category 요청 DTO 추가
  - [x] `SaveCategoryRequest`
- [x] Category 서비스 구현
  - [x] 전체 카테고리 트리 조회
  - [x] 카테고리 단건 조회
  - [x] 관리자 카테고리 생성
  - [x] 관리자 카테고리 수정
  - [x] 관리자 카테고리 삭제
  - [x] `slug`, `depth`, `sortOrder`, `isVisible` 처리
  - [x] slug 자동 생성
  - [x] 하위 카테고리 존재 시 삭제 차단
- [x] Category 컨트롤러 추가
  - [x] `GET /api/v1/categories`
  - [x] `GET /api/v1/categories/{id}`
  - [x] `POST /api/v1/categories`
  - [x] `PATCH /api/v1/categories/{id}`
  - [x] `DELETE /api/v1/categories/{id}`
- [x] 보안 규칙 보강
  - [x] 공개 조회 엔드포인트 `permitAll`
  - [x] 관리자 전용 작업 `AUTH_403`
- [x] Category API 테스트 추가
  - [x] 공개 트리 조회/단건 조회
  - [x] 관리자 생성/수정/삭제
  - [x] 비관리자 접근 차단
  - [x] 하위 카테고리 존재 시 삭제 차단
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `7. 카테고리(Category) API 구현`에 해당한다.
- 다음 단계는 `8. 상품(Product) API 구현`이다.
