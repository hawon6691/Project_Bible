---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 배송지(Address) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 배송지(Address) API 단계 연동"
commit: "feat: (#247) 배송지 API 연동 및 Address API 테스트 페이지 추가"
branch: "feat/#247/frontend-address-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(9. 배송지)에 맞춰 프론트 Address API 연동을 완료했습니다. 주소 목록/추가/수정/삭제를 전용 테스트 페이지에서 요청 단위로 검증할 수 있도록 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 배송지 API 엔드포인트 함수 추가/보강 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `GET /addresses`
  - [x] `POST /addresses`
  - [x] `PATCH /addresses/:id`
  - [x] `DELETE /addresses/:id`
- [x] 배송지 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/AddressApiPage.tsx`)
  - [x] 목록 조회
  - [x] 주소 추가
  - [x] 주소 수정
  - [x] 주소 삭제
- [x] 헤더/라우트에 Address API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/address-api`에서 배송지 API 단계별 요청 검증 가능
- [x] 로그인 사용자 기준 주소 CRUD 흐름 정상 동작
- [x] 타입체크 및 빌드 오류 없음
