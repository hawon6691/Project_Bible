---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Review Wishlist Point API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Review Wishlist Point API 구현"
commit: "feat: (#339) PHP Review Wishlist Point API 구현"
branch: "feat/#339/php-review-wishlist-point-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 리뷰, 위시리스트, 포인트 API를 구현한다.

## 📋 요구사항

- [x] 리뷰/위시리스트/포인트 테이블 추가
  - [x] `reviews` migration 추가
  - [x] `wishlist_items` migration 추가
  - [x] `point_transactions` migration 추가
- [x] 도메인 모델 추가
  - [x] `Review` 모델 추가
  - [x] `WishlistItem` 모델 추가
  - [x] `PointTransaction` 모델 추가
- [x] Review 요청 검증/서비스/컨트롤러 추가
  - [x] `StoreReviewRequest`
  - [x] `UpdateReviewRequest`
  - [x] `ReviewService`
  - [x] `ReviewController`
- [x] Review API 구현
  - [x] `GET /api/v1/products/{productId}/reviews`
  - [x] `POST /api/v1/products/{productId}/reviews`
  - [x] `PATCH /api/v1/reviews/{id}`
  - [x] `DELETE /api/v1/reviews/{id}`
  - [x] 주문-상품 일치 검증
  - [x] 상품 `review_count`, `rating_avg` 재계산
  - [x] 리뷰 작성 시 500P 적립
- [x] Wishlist 요청 검증/서비스/컨트롤러 추가
  - [x] `ListWishlistRequest`
  - [x] `WishlistService`
  - [x] `WishlistController`
- [x] Wishlist API 구현
  - [x] `GET /api/v1/wishlist`
  - [x] `POST /api/v1/wishlist/{productId}` 토글
  - [x] `DELETE /api/v1/wishlist/{productId}`
- [x] Point 요청 검증/서비스/컨트롤러 추가
  - [x] `ListPointTransactionsRequest`
  - [x] `AdminGrantPointRequest`
  - [x] `PointService`
  - [x] `PointController`
- [x] Point API 구현
  - [x] `GET /api/v1/points/balance`
  - [x] `GET /api/v1/points/transactions`
  - [x] `POST /api/v1/admin/points/grant`
  - [x] 거래 추가 시 잔액 스냅샷 저장
- [x] 라우트 파일 분리 및 등록
  - [x] `routes/api_v1/reviews.php`
  - [x] `routes/api_v1/wishlist.php`
  - [x] `routes/api_v1/points.php`
  - [x] `routes/api_v1.php`에 loader 연결
- [x] 통합 테스트 추가
  - [x] `tests/Feature/Api/ReviewWishlistPointApiTest.php` 추가
  - [x] 리뷰 생성/수정/삭제 검증
  - [x] 리뷰 작성 시 포인트 적립 및 포인트 내역 검증
  - [x] 위시리스트 토글/조회/삭제 검증
  - [x] 관리자 포인트 지급 검증
  - [x] `php artisan test tests/Feature/Api/ReviewWishlistPointApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list --path=api/v1/reviews` 통과
  - [x] `php artisan route:list --path=api/v1/wishlist` 통과
  - [x] `php artisan route:list --path=api/v1/points` 통과
  - [x] `php artisan route:list --path=api/v1/admin/points` 통과
