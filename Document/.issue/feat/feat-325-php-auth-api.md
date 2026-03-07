---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Auth API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Auth API 구현"
commit: "feat: (#325) PHP Auth API 구현"
branch: "feat/#325/php-auth-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 인증 API를 구현하고 JWT 기반 로그인/로그아웃/토큰 재발급 흐름을 구성한다.

## 📋 요구사항

- [x] 인증 설정 보강
  - [x] `firebase/php-jwt` 의존성 추가
  - [x] `.env`, `.env.example`, `config/pbshop.php`에 JWT/인증 TTL 설정 추가
  - [x] JWT 기본 시크릿 길이를 라이브러리 요구사항에 맞게 보정
- [x] 인증 지원 테이블 및 모델 추가
  - [x] `auth_codes` migration 및 `AuthCode` 모델 추가
  - [x] `refresh_tokens` migration 및 `RefreshToken` 모델 추가
  - [x] `social_accounts` migration 및 `SocialAccount` 모델 추가
  - [x] 기존 `password_reset_tokens` 테이블과 연동하는 `PasswordResetToken` 모델 추가
- [x] 인증 서비스/인프라 구성
  - [x] `JwtService` 추가
  - [x] `AuthenticateApiToken` 미들웨어 추가
  - [x] `bootstrap/app.php`에 `auth.api` alias 등록
- [x] Auth API 요청 검증 클래스 추가
  - [x] `SignupRequest`
  - [x] `VerifyEmailRequest`
  - [x] `ResendVerificationRequest`
  - [x] `LoginRequest`
  - [x] `RefreshTokenRequest`
  - [x] `PasswordResetRequestRequest`
  - [x] `PasswordResetVerifyRequest`
  - [x] `PasswordResetConfirmRequest`
  - [x] `SocialCompleteRequest`
  - [x] `SocialLinkRequest`
- [x] Auth API 서비스 구현
  - [x] 회원가입 시 사용자 생성 및 이메일 인증코드 발급
  - [x] 이메일 인증 및 인증 메일 재발송
  - [x] 로그인 및 JWT access token 발급
  - [x] refresh token 저장/재발급/폐기
  - [x] 로그아웃 시 활성 refresh token 폐기
  - [x] 비밀번호 재설정 요청/코드 검증/최종 변경
  - [x] 소셜 로그인 mock redirect/callback 흐름 추가
  - [x] 소셜 계정 연동/해제 및 추가 정보 입력 처리
- [x] Auth 컨트롤러 및 라우트 연결
  - [x] `POST /api/v1/auth/signup`
  - [x] `POST /api/v1/auth/verify-email`
  - [x] `POST /api/v1/auth/resend-verification`
  - [x] `POST /api/v1/auth/login`
  - [x] `POST /api/v1/auth/refresh`
  - [x] `POST /api/v1/auth/logout`
  - [x] `POST /api/v1/auth/password-reset/request`
  - [x] `POST /api/v1/auth/password-reset/verify`
  - [x] `POST /api/v1/auth/password-reset/confirm`
  - [x] `GET /api/v1/auth/login/{provider}`
  - [x] `GET /api/v1/auth/callback/{provider}`
  - [x] `POST /api/v1/auth/social/complete`
  - [x] `POST /api/v1/auth/social/link`
  - [x] `DELETE /api/v1/auth/social/unlink/{provider}`
- [x] 인증 플로우 테스트 추가
  - [x] `tests/Feature/Api/AuthApiTest.php` 추가
  - [x] 회원가입 시 사용자/인증코드 생성 검증
  - [x] 이메일 인증 후 로그인/토큰 재발급/로그아웃 검증
  - [x] `php artisan test tests/Feature/Api/AuthApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list`에서 Auth API 라우트 확인
