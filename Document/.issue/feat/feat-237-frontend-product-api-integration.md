---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 상품(Product) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 상품(Product) API 단계 연동"
commit: "feat: (#237) 상품 API CRUD/옵션/이미지 연동 및 Product API 테스트 페이지 추가"
branch: "feat/#237/frontend-product-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(4. 상품)에 맞춰 프론트 상품 API 연동 범위를 확장했습니다. 기존 상품 목록/상세 조회를 유지하면서 관리자 기준 상품 CRUD, 옵션 CRUD, 이미지 등록/삭제 API를 호출할 수 있는 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 상품 관리자 API 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `POST /products` (Admin)
  - [x] `PATCH /products/:id` (Admin)
  - [x] `DELETE /products/:id` (Admin)
  - [x] `POST /products/:id/options` (Admin)
  - [x] `PATCH /products/:id/options/:optionId` (Admin)
  - [x] `DELETE /products/:id/options/:optionId` (Admin)
  - [x] `POST /products/:id/images` (Admin)
  - [x] `DELETE /products/:id/images/:imageId` (Admin)
- [x] 상품 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/ProductApiPage.tsx`)
  - [x] 목록 조회/단건 조회
  - [x] 상품 생성/수정/삭제
  - [x] 옵션 추가/수정/삭제
  - [x] 이미지 추가/삭제
- [x] 헤더/라우트에 Product API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/product-api`에서 상품 API 전반 요청을 단계별로 실행 가능
- [x] 관리자 토큰으로 상품/옵션/이미지 변경 요청 검증 가능
- [x] 일반 사용자/미로그인 상태에서 권한 오류 응답 확인 가능
- [x] 타입체크 및 빌드 오류 없음

## ℹ️ 참고

> 명세 문서와 서버 구현 간 차이는 서버 구현 기준으로 우선 반영했습니다.

- 명세서 `POST /products/:id/images`는 `multipart`로 기술되어 있으나, 현재 서버 구현은 JSON Body(`{ url, isMain?, sortOrder? }`)를 사용
- 프론트 테스트 페이지는 위 서버 구현 포맷 기준으로 요청하도록 구성
