---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 리뷰(Review) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 리뷰(Review) API 단계 연동"
commit: "feat: (#253) 리뷰 API 연동 및 Review API 테스트 페이지 추가"
branch: "feat/#253/frontend-review-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(12. 리뷰)에 맞춰 프론트 Review API 연동을 완료했습니다. 리뷰 목록 조회/작성/수정/삭제를 전용 테스트 페이지에서 요청 단위로 검증할 수 있게 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 리뷰 API 엔드포인트 함수 추가/보강 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `GET /products/:productId/reviews`
  - [x] `POST /products/:productId/reviews`
  - [x] `PATCH /reviews/:id`
  - [x] `DELETE /reviews/:id`
- [x] 리뷰 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/ReviewApiPage.tsx`)
  - [x] 상품 리뷰 목록 조회
  - [x] 리뷰 작성
  - [x] 리뷰 수정
  - [x] 리뷰 삭제
- [x] 헤더/라우트에 Review API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/review-api`에서 리뷰 API 단계별 요청 검증 가능
- [x] 사용자 토큰 기준 리뷰 작성/수정/삭제 요청 검증 가능
- [x] 타입체크 및 빌드 오류 없음
