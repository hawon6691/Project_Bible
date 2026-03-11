---
name: "Java Maven Community Inquiry Support API"
about: "Java Spring Boot Maven 커뮤니티/문의/고객센터 API 구현"
title: "[FEAT] Java Maven 커뮤니티/문의/고객센터 API 구현"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "community", "inquiry", "support"]
assignees: []
issue: "[FEAT] Java Maven 커뮤니티/문의/고객센터 API 구현"
commit: "feat: (#401) Java Maven 커뮤니티 문의 고객센터 API 구현"
branch: "feat/#401/java-maven-community-inquiry-support-api"
---

## ✨ 기능 요약
Java Maven 트랙에 커뮤니티, 상품 문의, 고객센터 API를 추가해 TypeScript/PHP 기준 구현과 동일한 기능 범위를 맞춘다.

## 📋 요구사항
- [x] Flyway 마이그레이션 추가
  - [x] `boards`
  - [x] `posts`
  - [x] `post_likes`
  - [x] `post_comments`
  - [x] `product_inquiries`
  - [x] `support_tickets`
  - [x] `support_ticket_replies`
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
- [x] Inquiry API 구현
  - [x] `GET /api/v1/products/{productId}/inquiries`
  - [x] `POST /api/v1/products/{productId}/inquiries`
  - [x] `POST /api/v1/inquiries/{id}/answer`
  - [x] `GET /api/v1/inquiries/me`
  - [x] `DELETE /api/v1/inquiries/{id}`
- [x] Support API 구현
  - [x] `GET /api/v1/support/tickets`
  - [x] `POST /api/v1/support/tickets`
  - [x] `GET /api/v1/support/tickets/{id}`
  - [x] `POST /api/v1/support/tickets/{id}/reply`
  - [x] `GET /api/v1/admin/support/tickets`
  - [x] `PATCH /api/v1/admin/support/tickets/{id}/status`
- [x] SecurityConfig 공개/보호 경로 정리
- [x] Flyway migration 검증 갱신
- [x] 통합 테스트 추가
  - [x] 커뮤니티 게시글/좋아요/댓글 흐름
  - [x] 비밀 문의 마스킹/답변 흐름
  - [x] 고객센터 티켓/관리자 상태 변경 흐름
- [x] `mvnw.cmd test` 전체 통과

## 📌 참고
- TypeScript/PHP 기준 구현의 커뮤니티/문의/고객센터 범위에 맞춰 Java Maven 트랙도 동일 수준으로 정렬
