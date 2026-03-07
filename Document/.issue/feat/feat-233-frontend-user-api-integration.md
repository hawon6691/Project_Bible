---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 회원(User) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 회원(User) API 단계 연동"
commit: "feat: (#233) 사용자 모듈 API 확장 및 User API 테스트 페이지 추가"
branch: "feat/#233/frontend-user-api-integration"
assignees: ""
---

## ✨ 기능 요약

> 인증 단계 다음 순서인 회원(User) 모듈을 프론트에 적용했습니다. 내 정보 조회/수정/탈퇴, 공개 프로필 조회, 프로필 수정, 프로필 이미지 초기화, 관리자 회원 목록/상태 변경 API를 클라이언트와 화면에서 호출 가능하도록 확장했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] User API 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `GET /users/me`
  - [x] `PUT /users/me` (서버 구현 기준)
  - [x] `DELETE /users/me`
  - [x] `GET /users/profile/:id` (서버 구현 기준)
  - [x] `PATCH /users/me/profile`
  - [x] `DELETE /users/me/profile-image`
  - [x] `GET /users` (Admin)
  - [x] `PATCH /users/:id/status` (Admin)
- [x] 사용자 API 전용 테스트 화면 추가 (`FrontEnd/src/pages/UserApiPage.tsx`)
  - [x] 내 정보 조회/수정/탈퇴 동작
  - [x] 공개 프로필 조회 동작
  - [x] 닉네임/소개글 수정 및 프로필 이미지 초기화 동작
  - [x] 관리자 권한일 때 회원 목록 조회/상태 변경 동작
- [x] 헤더/라우트에 User API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] 인증 유틸 보강(`setAccessToken`) (`FrontEnd/src/lib/auth.ts`)
- [x] 스타일 보강(메뉴 링크 스타일) (`FrontEnd/src/styles.css`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] 로그인 사용자 기준 User API 주요 흐름(조회/수정/탈퇴/프로필) 호출 가능
- [x] 관리자 계정 기준 회원 목록/상태 변경 API 호출 가능
- [x] 라우트 진입(` /user-api `) 및 API 호출 UI 동작 확인
- [x] 타입체크/빌드 오류 없음

## ℹ️ 참고

> API 명세서와 서버 실제 구현 간 경로/메서드 차이는 서버 구현을 기준으로 우선 반영했습니다.

- 명세: `GET /users/:id/profile`, 실제 구현: `GET /users/profile/:id`
- 명세: `PATCH /users/me`, 실제 구현: `PUT /users/me`
- 명세: `POST /users/me/profile-image` 항목은 현재 서버 컨트롤러에 미구현(삭제 API만 존재)
