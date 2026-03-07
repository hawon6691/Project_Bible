---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 결제(Payment) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 결제(Payment) API 단계 연동"
commit: "feat: (#251) 결제 API 연동 및 Payment API 테스트 페이지 추가"
branch: "feat/#251/frontend-payment-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(11. 결제)에 맞춰 프론트 Payment API 연동을 완료했습니다. 결제 요청/상세 조회/환불 요청을 전용 테스트 페이지에서 검증할 수 있게 구성했으며, 서버 구현 기준 관리자 환불 API도 함께 반영했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 결제 API 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `POST /payments`
  - [x] `GET /payments/:id`
  - [x] `POST /payments/:id/refund`
  - [x] `POST /admin/payments/:id/refund` (서버 구현 기준)
- [x] 결제 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/PaymentApiPage.tsx`)
  - [x] 결제 대상 주문 목록 조회 (`GET /orders`)
  - [x] 결제 요청/상세 조회
  - [x] 사용자 환불 요청
  - [x] 관리자 환불 요청
- [x] 헤더/라우트에 Payment API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/payment-api`에서 결제 API 흐름을 단계별로 수동 검증 가능
- [x] 사용자 토큰 기준 결제 요청/조회/환불 요청 검증 가능
- [x] 관리자 토큰 기준 관리자 환불 API 요청 검증 가능
- [x] 타입체크 및 빌드 오류 없음
