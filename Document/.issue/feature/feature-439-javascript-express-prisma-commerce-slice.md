---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Commerce Slice"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Commerce Slice 문서 작성"
commit: "feat: (#439) JavaScript Express Prisma 구매 흐름 API 구현"
branch: "feature/#439/javascript-express-prisma-commerce-slice"
---

## ✨ 기능 요약

> JavaScript Express Prisma 구현체에 장바구니, 배송지, 주문, 결제의 구매 흐름 API를 추가한다.

## 📋 요구사항

- [x] `commerce-routes.js` 추가
- [x] `GET /api/v1/cart` 구현
- [x] `POST /api/v1/cart` 구현
- [x] `PATCH /api/v1/cart/:itemId` 구현
- [x] `DELETE /api/v1/cart/:itemId` 구현
- [x] `DELETE /api/v1/cart` 구현
- [x] `GET /api/v1/addresses` 구현
- [x] `POST /api/v1/addresses` 구현
- [x] `PATCH /api/v1/addresses/:id` 구현
- [x] `DELETE /api/v1/addresses/:id` 구현
- [x] `GET /api/v1/orders` 구현
- [x] `GET /api/v1/orders/:id` 구현
- [x] `POST /api/v1/orders` 구현
- [x] `POST /api/v1/orders/:id/cancel` 구현
- [x] `GET /api/v1/admin/orders` 구현
- [x] `PATCH /api/v1/admin/orders/:id/status` 구현
- [x] `POST /api/v1/payments` 구현
- [x] `GET /api/v1/payments/:id` 구현
- [x] `POST /api/v1/payments/:id/refund` 구현
- [x] Prisma schema에 cart seller relation 추가
- [x] order status enum 보정
- [x] payment method/status enum 보정
- [x] 샘플 사용자 기준 장바구니, 주소, 주문, 결제 API 응답 검증

## ✅ 산출물

- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/commerce-routes.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/app.js`
- `BackEnd/JavaScript/expressshop_prismaorm/prisma/schema.prisma`

## 검증 메모

- `user1@nestshop.com / Password1!` 로그인 성공
- `GET /api/v1/cart` 성공
- `GET /api/v1/addresses` 성공
- `GET /api/v1/orders` 성공
- `GET /api/v1/orders/1` 성공
- `GET /api/v1/payments/1` 성공
- 공통 PostgreSQL 샘플 데이터 기준으로 구매 흐름 조회 응답 확인

## 메모

- 이번 단계는 JavaScript 구현체의 구매 흐름 API를 여는 작업이다.
- 다음 단계는 `review`, `wishlist`, `point`, `community`, `inquiry`, `support` 쪽으로 이어간다.
