---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma First API Slice"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma First API Slice 문서 작성"
commit: "feat: (#437) JavaScript Express Prisma 첫 API 슬라이스 구현"
branch: "feat/#437/javascript-express-prisma-first-api-slice"
---

## ✨ 기능 요약

> JavaScript Express Prisma 구현체에 인증, 사용자, 상품, 스펙, 가격 비교의 첫 API 슬라이스를 추가한다.

## 📋 요구사항

- [x] 공통 응답 형식 유틸 추가
- [x] 공통 HTTP 에러 유틸 추가
- [x] async handler 유틸 추가
- [x] JWT 기반 access/refresh token 서비스 추가
- [x] 인증 미들웨어와 역할 체크 미들웨어 추가
- [x] `POST /api/v1/auth/signup` 구현
- [x] `POST /api/v1/auth/login` 구현
- [x] `POST /api/v1/auth/logout` 구현
- [x] `POST /api/v1/auth/refresh` 구현
- [x] `GET /api/v1/auth/me` 구현
- [x] `GET /api/v1/users/me` 구현
- [x] `PATCH /api/v1/users/me` 구현
- [x] `GET /api/v1/users` 구현
- [x] `GET /api/v1/users/:id/profile` 구현
- [x] `GET /api/v1/categories` 구현
- [x] `GET /api/v1/products` 구현
- [x] `GET /api/v1/products/:id` 구현
- [x] `GET /api/v1/specs/definitions` 구현
- [x] `GET /api/v1/products/:id/specs` 구현
- [x] `GET /api/v1/products/:id/prices` 구현
- [x] `GET /api/v1/products/:id/price-history` 구현
- [x] Prisma schema enum/관계 보정
- [x] 실제 샘플 데이터 기준 로그인 및 주요 API 응답 검증

## ✅ 산출물

- `BackEnd/JavaScript/expressshop_prismaorm/src/app.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/middleware/auth.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/auth-routes.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/user-routes.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/catalog-routes.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/health-routes.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/services/token-service.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/utils/response.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/utils/http-error.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/utils/async-handler.js`
- `BackEnd/JavaScript/expressshop_prismaorm/prisma/schema.prisma`
- `BackEnd/JavaScript/expressshop_prismaorm/package.json`

## 검증 메모

- 관리자 계정 `admin@nestshop.com / Password1!` 로그인 성공
- `GET /api/v1/users/me` 성공
- `GET /api/v1/products` 성공
- `GET /api/v1/products/1` 성공
- `GET /api/v1/specs/definitions?categoryId=2` 성공
- `GET /api/v1/products/1/prices` 성공
- `GET /api/v1/products/1/price-history` 성공

## 메모

- 이번 단계는 공통 명세 전체 구현이 아니라 JavaScript 구현체의 첫 도메인 API 슬라이스를 여는 작업이다.
- 다음 단계는 `cart`, `address`, `order`, `payment`로 이어지는 구매 흐름 API 구현이다.
