---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 위시리스트(Wishlist) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 위시리스트(Wishlist) API 단계 연동"
commit: "feat: (#255) 위시리스트 API 연동 및 Wishlist API 테스트 페이지 추가"
branch: "feat/#255/frontend-wishlist-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(13. 위시리스트)에 맞춰 프론트 Wishlist API 연동을 완료했습니다. 목록 조회/토글/해제 요청을 전용 테스트 페이지에서 검증할 수 있게 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 위시리스트 API 엔드포인트 함수 추가/보강 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `GET /wishlist`
  - [x] `POST /wishlist/:productId`
  - [x] `DELETE /wishlist/:productId`
- [x] 위시리스트 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/WishlistApiPage.tsx`)
  - [x] 목록 조회
  - [x] 토글 요청
  - [x] 해제 요청
- [x] 헤더/라우트에 Wishlist API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/wishlist-api`에서 위시리스트 API 단계별 요청 검증 가능
- [x] 사용자 토큰 기준 위시리스트 등록/해제/조회 요청 검증 가능
- [x] 타입체크 및 빌드 오류 없음
