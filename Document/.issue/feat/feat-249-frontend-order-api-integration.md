---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 주문(Order) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 주문(Order) API 단계 연동"
commit: "feat: (#249) 주문 API 연동 및 Order API 테스트 페이지 추가"
branch: "feat/#249/frontend-order-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(10. 주문)에 맞춰 프론트 Order API 연동 범위를 확장했습니다. 사용자 주문 생성/조회/상세/취소와 관리자 주문 목록/상태 변경까지 전용 테스트 페이지에서 요청 단위로 검증할 수 있게 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 주문 API 엔드포인트 함수 추가/보강 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `POST /orders`
  - [x] `GET /orders`
  - [x] `GET /orders/:id`
  - [x] `POST /orders/:id/cancel`
  - [x] `GET /admin/orders` (Admin)
  - [x] `PATCH /admin/orders/:id/status` (Admin)
- [x] 주문 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/OrderApiPage.tsx`)
  - [x] 사용자 주문 목록/상세 조회
  - [x] 주문 생성(주소/아이템/옵션/포인트/메모)
  - [x] 주문 취소
  - [x] 관리자 주문 목록 조회/상태 변경
- [x] 헤더/라우트에 Order API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/order-api`에서 주문 API 흐름을 단계별로 수동 검증 가능
- [x] 사용자 토큰 기준 주문 생성/조회/취소 요청 검증 가능
- [x] 관리자 토큰 기준 주문 관리 API 요청 검증 가능
- [x] 타입체크 및 빌드 오류 없음
