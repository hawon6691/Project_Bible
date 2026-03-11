---
name: "Java Maven Review Wishlist Point API"
about: "Java Spring Boot Maven 리뷰/위시리스트/포인트 API 구현"
title: "[FEAT] Java Maven 리뷰/위시리스트/포인트 API 구현"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "review", "wishlist", "point"]
assignees: []
issue: "[FEAT] Java Maven 리뷰/위시리스트/포인트 API 구현"
commit: "feat: (#399) Java Maven 리뷰 위시리스트 포인트 API 구현"
branch: "feat/#399/java-maven-review-wishlist-point-api"
---

## ✨ 기능 요약

Java Maven 기준 구현에 리뷰, 위시리스트, 포인트 도메인을 추가한다. 구매 주문 기반 리뷰 작성 검증, 상품 평점 집계, 위시리스트 토글, 리뷰 적립 포인트와 관리자 수동 지급까지 연결해 구매 이후 사용자 활동 흐름을 완성한다.

## 📋 요구사항

- [x] 스키마 확장
  - [x] `V7__review_wishlist_point_support.sql`
  - [x] `users.point_balance`
  - [x] `reviews`
  - [x] `wishlist_items`
  - [x] `point_transactions`
- [x] Review 도메인 추가
  - [x] `Review`
  - [x] `ReviewRepository`
  - [x] `ReviewService`
  - [x] `ReviewController`
  - [x] `ReviewDtos`
- [x] Wishlist 도메인 추가
  - [x] `WishlistItem`
  - [x] `WishlistRepository`
  - [x] `WishlistService`
  - [x] `WishlistController`
- [x] Point 도메인 추가
  - [x] `PointTransaction`
  - [x] `PointTransactionRepository`
  - [x] `PointService`
  - [x] `PointController`
  - [x] `PointDtos`
- [x] Review API 구현
  - [x] `GET /api/v1/products/{productId}/reviews`
  - [x] `POST /api/v1/products/{productId}/reviews`
  - [x] `PATCH /api/v1/reviews/{id}`
  - [x] `DELETE /api/v1/reviews/{id}`
- [x] Wishlist API 구현
  - [x] `GET /api/v1/wishlist`
  - [x] `POST /api/v1/wishlist/{productId}`
  - [x] `DELETE /api/v1/wishlist/{productId}`
- [x] Point API 구현
  - [x] `GET /api/v1/points/balance`
  - [x] `GET /api/v1/points/transactions`
  - [x] `POST /api/v1/admin/points/grant`
- [x] 리뷰/포인트 규칙 구현
  - [x] 주문 소유자 + 구매 상품 일치 검증
  - [x] 동일 주문/상품 리뷰 중복 작성 차단
  - [x] 리뷰 생성/수정/삭제 후 `products.review_count`, `products.rating_avg` 재계산
  - [x] 리뷰 작성 시 `500` 포인트 적립
  - [x] 포인트 거래에 잔액 스냅샷 저장
  - [x] 관리자 수동 포인트 지급 지원
- [x] 기존 계층 보강
  - [x] `SecurityConfig` 공개 리뷰 조회 경로 허용
  - [x] `ErrorCode.CONFLICT` 추가
  - [x] `FlywayMigrationTest`를 `v7` 기준으로 갱신
  - [x] `REVIEWS`, `WISHLIST_ITEMS`, `POINT_TRANSACTIONS` 테이블 검증 추가
- [x] API 테스트 추가
  - [x] `ReviewWishlistPointApiTest`
  - [x] 리뷰 생성/조회/수정/삭제 검증
  - [x] 리뷰 작성 포인트 적립 검증
  - [x] 위시리스트 토글/조회/삭제 검증
  - [x] 관리자 포인트 지급 및 내역/잔액 검증
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd -Dtest=ReviewWishlistPointApiTest test`
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `12. 리뷰/위시리스트/포인트 API 구현`에 해당한다.
- 다음 단계는 `13. 커뮤니티/문의/고객센터 API 구현`이다.
