---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Engagement Community Support Slice"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Engagement Community Support Slice 문서 작성"
commit: "feat: (#441) JavaScript Express Prisma 참여·커뮤니티·지원 API 구현"
branch: "feature/#441/javascript-express-prisma-engagement-community-support-slice"
---

## ✨ 기능 요약

> JavaScript Express Prisma 구현체에 리뷰, 위시리스트, 포인트, 커뮤니티, 상품 문의, 고객센터, FAQ, 공지 API를 추가한다.

## 📋 요구사항

- [x] Prisma schema에 `PointTransaction` 모델 추가
- [x] Prisma schema에 `Board`, `Post`, `Comment` 모델 추가
- [x] Prisma schema에 `Inquiry` 모델 추가
- [x] Prisma schema에 `SupportTicket`, `TicketReply` 모델 추가
- [x] Prisma schema에 `Faq`, `Notice` 모델 추가
- [x] Prisma schema에 `point_type`, `ticket_category`, `ticket_status`, `faq_category` enum 추가
- [x] 기존 `User`, `Product`, `Review` 관계 보정
- [x] `engagement-routes.js` 추가
- [x] `community-routes.js` 추가
- [x] `support-routes.js` 추가
- [x] `GET /api/v1/products/:productId/reviews` 구현
- [x] `POST /api/v1/products/:productId/reviews` 구현
- [x] `GET /api/v1/wishlist` 구현
- [x] `POST /api/v1/wishlist/:productId` 구현
- [x] `DELETE /api/v1/wishlist/:productId` 구현
- [x] `GET /api/v1/points/balance` 구현
- [x] `GET /api/v1/points/transactions` 구현
- [x] `POST /api/v1/admin/points/grant` 구현
- [x] `GET /api/v1/community/boards` 구현
- [x] `GET /api/v1/community/posts` 구현
- [x] `POST /api/v1/community/posts` 구현
- [x] `GET /api/v1/community/posts/:id` 구현
- [x] `POST /api/v1/community/posts/:id/comments` 구현
- [x] `GET /api/v1/products/:productId/inquiries` 구현
- [x] `POST /api/v1/products/:productId/inquiries` 구현
- [x] `GET /api/v1/support/tickets` 구현
- [x] `POST /api/v1/support/tickets` 구현
- [x] `GET /api/v1/support/tickets/:id` 구현
- [x] `POST /api/v1/support/tickets/:id/reply` 구현
- [x] `GET /api/v1/admin/support/tickets` 구현
- [x] `PATCH /api/v1/admin/support/tickets/:id/status` 구현
- [x] `GET /api/v1/faqs` 구현
- [x] `POST /api/v1/faqs` 구현
- [x] `GET /api/v1/notices` 구현
- [x] `GET /api/v1/notices/:id` 구현
- [x] 샘플 데이터 기준 주요 참여/커뮤니티/지원 API 응답 검증

## ✅ 산출물

- `BackEnd/JavaScript/expressshop_prismaorm/prisma/schema.prisma`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/engagement-routes.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/community-routes.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/support-routes.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/app.js`

## 검증 메모

- `GET /api/v1/products/1/reviews` 성공
- `GET /api/v1/wishlist` 성공
- `GET /api/v1/points/transactions` 성공
- `GET /api/v1/community/posts` 성공
- `GET /api/v1/products/1/inquiries` 성공
- `GET /api/v1/support/tickets` 성공
- `GET /api/v1/faqs` 성공
- `GET /api/v1/notices` 성공
- 공통 PostgreSQL 샘플 데이터 기준 응답 확인

## 메모

- 이번 단계는 JavaScript 구현체의 참여, 커뮤니티, 고객지원 축을 여는 작업이다.
- 전체 기능 구현이 끝난 것은 아니며 다음 단계는 `ranking`, `recommendation`, `deal`, `chat`, `activity`, `ops/admin` 축으로 이어진다.
