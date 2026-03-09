---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Community Inquiry Support API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Community Inquiry Support API 구현"
commit: "feat: (#341) PHP Community Inquiry Support API 구현"
branch: "feat/#341/php-community-inquiry-support-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 커뮤니티, 상품 문의, 고객센터 API를 구현한다.

## 📋 요구사항

- [x] 커뮤니티/문의/고객센터 테이블 추가
  - [x] `boards` migration 추가
  - [x] `posts` migration 추가
  - [x] `post_likes` migration 추가
  - [x] `post_comments` migration 추가
  - [x] `product_inquiries` migration 추가
  - [x] `support_tickets` migration 추가
  - [x] `support_ticket_replies` migration 추가
- [x] 도메인 모델 추가
  - [x] `Board`
  - [x] `Post`
  - [x] `PostLike`
  - [x] `PostComment`
  - [x] `ProductInquiry`
  - [x] `SupportTicket`
  - [x] `SupportTicketReply`
- [x] Community 요청 검증/서비스/컨트롤러 추가
  - [x] `StorePostRequest`
  - [x] `UpdatePostRequest`
  - [x] `StoreCommentRequest`
  - [x] `CommunityService`
  - [x] `CommunityController`
- [x] Community API 구현
  - [x] `GET /api/v1/boards`
  - [x] `GET /api/v1/boards/{boardId}/posts`
  - [x] `GET /api/v1/posts/{id}`
  - [x] `POST /api/v1/boards/{boardId}/posts`
  - [x] `PATCH /api/v1/posts/{id}`
  - [x] `DELETE /api/v1/posts/{id}`
  - [x] `POST /api/v1/posts/{id}/like`
  - [x] `GET /api/v1/posts/{id}/comments`
  - [x] `POST /api/v1/posts/{id}/comments`
  - [x] `DELETE /api/v1/comments/{id}`
  - [x] 게시글 조회 시 `view_count` 증가
  - [x] 좋아요/댓글 수 동기화
- [x] Inquiry 요청 검증/서비스/컨트롤러 추가
  - [x] `StoreInquiryRequest`
  - [x] `AnswerInquiryRequest`
  - [x] `InquiryService`
  - [x] `InquiryController`
- [x] Inquiry API 구현
  - [x] `GET /api/v1/products/{productId}/inquiries`
  - [x] `POST /api/v1/products/{productId}/inquiries`
  - [x] `POST /api/v1/inquiries/{id}/answer`
  - [x] `GET /api/v1/inquiries/me`
  - [x] `DELETE /api/v1/inquiries/{id}`
  - [x] 공개 목록에서 비밀 문의 마스킹 처리
  - [x] 관리자 답변 시 `answered_by`, `answered_at` 저장
- [x] Support 요청 검증/서비스/컨트롤러 추가
  - [x] `StoreSupportTicketRequest`
  - [x] `ReplySupportTicketRequest`
  - [x] `UpdateSupportTicketStatusRequest`
  - [x] `SupportService`
  - [x] `SupportController`
- [x] Support API 구현
  - [x] `GET /api/v1/support/tickets`
  - [x] `POST /api/v1/support/tickets`
  - [x] `GET /api/v1/support/tickets/{id}`
  - [x] `POST /api/v1/support/tickets/{id}/reply`
  - [x] `GET /api/v1/admin/support/tickets`
  - [x] `PATCH /api/v1/admin/support/tickets/{id}/status`
  - [x] 티켓 번호 자동 생성
  - [x] 관리자 답변/상태 변경 흐름 반영
- [x] 라우트 파일 분리 및 등록
  - [x] `routes/api_v1/community.php`
  - [x] `routes/api_v1/inquiries.php`
  - [x] `routes/api_v1/support.php`
  - [x] `routes/api_v1.php`에 loader 연결
- [x] 통합 테스트 추가
  - [x] `tests/Feature/Api/CommunityInquirySupportApiTest.php` 추가
  - [x] 커뮤니티 게시글/좋아요/댓글 흐름 검증
  - [x] 상품 문의 작성/비밀 마스킹/관리자 답변 검증
  - [x] 고객센터 티켓 생성/답변/상태 변경 검증
  - [x] `php artisan test tests/Feature/Api/CommunityInquirySupportApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list --path=api/v1/boards` 통과
  - [x] `php artisan route:list --path=api/v1/support` 통과
  - [x] `php artisan route:list --path=api/v1/admin/support` 통과
  - [x] `php artisan route:list | Select-String 'inquiries|support|boards|posts|comments'` 검증
