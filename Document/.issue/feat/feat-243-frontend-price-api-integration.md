---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 가격비교/가격추이(Price) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 가격비교/가격추이(Price) API 단계 연동"
commit: "feat: (#243) 가격 API 연동 및 Price API 테스트 페이지 추가"
branch: "feat/#243/frontend-price-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(7. 가격비교/가격추이)에 맞춰 프론트에 Price API를 연동했습니다. 상품 가격비교/가격추이 조회와 가격 등록/수정/삭제, 사용자 최저가 알림 등록/조회/삭제를 전용 테스트 페이지에서 검증할 수 있게 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 가격 API 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `GET /products/:id/prices`
  - [x] `POST /products/:id/prices` (Seller/Admin)
  - [x] `PATCH /prices/:id` (Seller/Admin)
  - [x] `DELETE /prices/:id` (Admin)
  - [x] `GET /products/:id/price-history`
  - [x] `GET /price-alerts` (User)
  - [x] `POST /price-alerts` (User)
  - [x] `DELETE /price-alerts/:id` (User)
- [x] 가격 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/PriceApiPage.tsx`)
  - [x] 가격비교/가격추이 조회
  - [x] 가격 등록/수정/삭제 요청 폼 구성
  - [x] 최저가 알림 조회/등록/삭제 요청 폼 구성
- [x] 헤더/라우트에 Price API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/price-api`에서 가격 API 흐름을 단계별로 실행 가능
- [x] Seller/Admin 토큰으로 가격 등록/수정/삭제 요청 검증 가능
- [x] User 토큰으로 알림 등록/조회/삭제 요청 검증 가능
- [x] 타입체크 및 빌드 오류 없음
