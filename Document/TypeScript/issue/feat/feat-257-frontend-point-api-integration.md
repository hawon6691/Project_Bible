---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 포인트(Point) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 포인트(Point) API 단계 연동"
commit: "feat: (#257) 포인트 API 연동 및 Point API 테스트 페이지 추가"
branch: "feat/#257/frontend-point-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(14. 포인트)에 맞춰 프론트 Point API 연동을 완료했습니다. 잔액 조회/내역 조회/관리자 지급 요청을 전용 테스트 페이지에서 요청 단위로 검증할 수 있게 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 포인트 API 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `GET /points/balance`
  - [x] `GET /points/transactions`
  - [x] `POST /admin/points/grant` (Admin)
- [x] 포인트 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/PointApiPage.tsx`)
  - [x] 잔액 조회
  - [x] 내역 조회(페이지/필터)
  - [x] 관리자 수동 지급
- [x] 헤더/라우트에 Point API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/point-api`에서 포인트 API 단계별 요청 검증 가능
- [x] 사용자 토큰 기준 잔액/내역 조회 요청 검증 가능
- [x] 관리자 토큰 기준 포인트 지급 요청 검증 가능
- [x] 타입체크 및 빌드 오류 없음
