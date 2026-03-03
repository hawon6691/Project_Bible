---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 인증 API 전체 연동 및 토큰 갱신 흐름 구축"
labels: feature
issue: "[FEAT] FrontEnd 인증 API 전체 연동 및 토큰 갱신 흐름 구축"
commit: "feat: (#231) 프론트 인증 플로우 확장(회원가입/이메일인증/비밀번호재설정/리프레시)"
branch: "feat/#231/frontend-auth-api-integration"
assignees: ""
---

## ✨ 기능 요약

> 비어 있던 FrontEnd를 API 계약 기반(Vite + React + TypeScript)으로 재구성하고, 인증 모듈을 로그인/회원가입 수준에서 확장해 이메일 인증, 비밀번호 재설정, 토큰 자동 갱신까지 실제 백엔드 엔드포인트와 연동했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] FrontEnd 기본 스캐폴딩 생성 및 개발 서버 포트 `3001` 고정 (`FrontEnd/package.json`, `FrontEnd/vite.config.ts`)
- [x] 공통 API 클라이언트에 백엔드 표준 응답(`success/data/meta`) 처리 반영 (`FrontEnd/src/lib/apiClient.ts`)
- [x] 인증 토큰 저장소(access/refresh) 유틸 구성 (`FrontEnd/src/lib/auth.ts`)
- [x] `401` 응답 시 `auth/refresh` 자동 재발급 후 원요청 재시도 로직 적용 (`FrontEnd/src/lib/apiClient.ts`)
- [x] Auth 타입 확장(verify-email, reset-password 등) (`FrontEnd/src/lib/types.ts`)
- [x] 인증 API 엔드포인트 함수 추가
  - [x] `POST /auth/signup`
  - [x] `POST /auth/verify-email`
  - [x] `POST /auth/resend-verification`
  - [x] `POST /auth/login`
  - [x] `POST /auth/logout`
  - [x] `POST /auth/refresh`
  - [x] `POST /auth/password-reset/request`
  - [x] `POST /auth/password-reset/verify`
  - [x] `POST /auth/password-reset/confirm`
  (`FrontEnd/src/lib/endpoints.ts`)
- [x] 로그인 화면 연동 및 인증/재설정 링크 추가 (`FrontEnd/src/pages/LoginPage.tsx`)
- [x] 회원가입 화면 연동 및 이메일 인증 라우트 이동 처리 (`FrontEnd/src/pages/SignupPage.tsx`)
- [x] 이메일 인증 화면(코드 검증/재발송) 신규 추가 (`FrontEnd/src/pages/VerifyEmailPage.tsx`)
- [x] 비밀번호 재설정 3단계 화면(요청→코드확인→변경) 신규 추가 (`FrontEnd/src/pages/PasswordResetPage.tsx`)
- [x] 인증 관련 라우트 추가 (`FrontEnd/src/App.tsx`)
- [x] 인증 링크 스타일 보강 (`FrontEnd/src/styles.css`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] 비로그인 사용자 기준 회원가입 → 이메일 인증 → 로그인 흐름 실행 가능
- [x] 비밀번호 재설정 전체 흐름(요청/코드확인/신규 비밀번호 설정) 실행 가능
- [x] 액세스 토큰 만료 시 리프레시 토큰으로 자동 갱신 후 요청 재시도 동작 확인
- [x] 인증 모듈 변경 이후 FrontEnd 타입 체크 및 빌드 오류 없음
