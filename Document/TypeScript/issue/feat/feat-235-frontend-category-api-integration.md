---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 카테고리(Category) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 카테고리(Category) API 단계 연동"
commit: "feat: (#235) 카테고리 API CRUD 연동 및 Category API 테스트 페이지 추가"
branch: "feat/#235/frontend-category-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(3. 카테고리)에 맞춰 프론트에 카테고리 API를 단계적으로 적용했습니다. 기존 트리 조회뿐 아니라 단건 조회, 생성/수정/삭제(Admin)까지 호출 가능한 엔드포인트를 추가하고, 전용 테스트 페이지를 통해 요청/응답을 검증할 수 있게 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 카테고리 API 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `GET /categories`
  - [x] `GET /categories/:id`
  - [x] `POST /categories` (Admin)
  - [x] `PATCH /categories/:id` (Admin)
  - [x] `DELETE /categories/:id` (Admin)
- [x] 카테고리 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/CategoryApiPage.tsx`)
  - [x] 트리 목록 조회 렌더링
  - [x] 단건 조회 폼/결과 렌더링
  - [x] 생성/수정/삭제 요청 폼 구성
- [x] 헤더/라우트에 Category API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/category-api`에서 카테고리 조회/단건/API 요청 흐름 동작
- [x] 관리자 토큰으로 생성/수정/삭제 요청 가능
- [x] 일반 사용자 토큰/미로그인 상태에서 권한 오류 응답 확인 가능
- [x] 타입체크 및 빌드 오류 없음
