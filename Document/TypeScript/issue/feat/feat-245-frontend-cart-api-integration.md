---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 장바구니(Cart) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 장바구니(Cart) API 단계 연동"
commit: "feat: (#245) 장바구니 API 연동 및 Cart API 테스트 페이지 추가"
branch: "feat/#245/frontend-cart-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(8. 장바구니)에 맞춰 프론트 Cart API 연동 단계를 정리했습니다. 기존 사용자용 CartPage 흐름을 유지하면서, 엔드포인트 단위 검증이 가능한 Cart API 전용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 장바구니 API 엔드포인트 연동 확인 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `GET /cart`
  - [x] `POST /cart`
  - [x] `PATCH /cart/:itemId`
  - [x] `DELETE /cart/:itemId`
  - [x] `DELETE /cart`
- [x] 장바구니 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/CartApiPage.tsx`)
  - [x] 목록 조회
  - [x] 항목 추가
  - [x] 수량 변경
  - [x] 항목 삭제/전체 비우기
- [x] 헤더/라우트에 Cart API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/cart-api`에서 장바구니 API 전 단계 요청을 수동 검증 가능
- [x] 로그인/비로그인(게스트 키) 상태 모두에서 Cart 동작 확인 가능
- [x] 타입체크 및 빌드 오류 없음

## ℹ️ 참고

> 서버 구현 기준 확장 사항

- 엔드포인트 유틸은 사용자 장바구니 외에 게스트 장바구니(`/cart/guest`)도 자동 처리하도록 이미 구성되어 있음
