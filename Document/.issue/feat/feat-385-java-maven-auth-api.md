---
name: "Java Maven Auth API"
about: "Java Spring Boot Maven 인증 API 구현"
title: "[FEAT] Java Maven 인증 API 구현"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "auth"]
assignees: []
issue: "[FEAT] Java Maven 인증 API 구현"
commit: "feat: (#385) Java Maven 인증 API 구현"
branch: "feat/#385/java-maven-auth-api"
---

## ✨ 기능 요약

Java Maven 기준 구현에 인증 도메인을 추가한다. 회원가입, 이메일 인증, 로그인, 토큰 재발급, 로그아웃, 비밀번호 재설정, 소셜 로그인/연동/해제를 공통 API 계약에 맞게 구현하고, bearer 인증 필터까지 연결해 이후 사용자 도메인부터 동일한 인증 체계를 재사용할 수 있게 만든다.

## 📋 요구사항

- [x] 인증 지원 DB 스키마 추가
  - [x] `users.phone` 컬럼 반영
  - [x] `V2__auth_support.sql` 추가
  - [x] `auth_verification_codes`
  - [x] `auth_access_tokens`
  - [x] `auth_refresh_tokens`
  - [x] `auth_password_reset_requests`
  - [x] `social_accounts`
- [x] 사용자/인증 엔티티 및 저장소 추가
  - [x] `User`
  - [x] `UserRepository`
  - [x] `AuthVerificationCode`
  - [x] `AuthAccessToken`
  - [x] `AuthRefreshToken`
  - [x] `AuthPasswordResetRequest`
  - [x] `SocialAccount`
  - [x] 각 JPA Repository 추가
- [x] Auth 요청 DTO 추가
  - [x] `SignupRequest`
  - [x] `VerifyEmailRequest`
  - [x] `ResendVerificationRequest`
  - [x] `LoginRequest`
  - [x] `RefreshRequest`
  - [x] `PasswordResetRequest`
  - [x] `PasswordResetVerifyRequest`
  - [x] `PasswordResetConfirmRequest`
  - [x] `SocialCompleteRequest`
  - [x] `SocialLinkRequest`
- [x] 인증 서비스 계층 구현
  - [x] 회원가입
  - [x] 이메일 인증/재발송
  - [x] 로그인
  - [x] 로그아웃
  - [x] refresh token 재발급
  - [x] 비밀번호 재설정 요청/검증/변경
  - [x] 소셜 로그인 redirect/callback
  - [x] 소셜 프로필 완료
  - [x] 소셜 계정 연동/해제
- [x] bearer 인증 계층 추가
  - [x] `AuthenticatedUserPrincipal`
  - [x] `BearerTokenAuthenticationFilter`
  - [x] access token 기반 SecurityContext 구성
- [x] Auth 컨트롤러 추가
  - [x] `POST /api/v1/auth/signup`
  - [x] `POST /api/v1/auth/verify-email`
  - [x] `POST /api/v1/auth/resend-verification`
  - [x] `POST /api/v1/auth/login`
  - [x] `POST /api/v1/auth/logout`
  - [x] `POST /api/v1/auth/refresh`
  - [x] `POST /api/v1/auth/password-reset/request`
  - [x] `POST /api/v1/auth/password-reset/verify`
  - [x] `POST /api/v1/auth/password-reset/confirm`
  - [x] `GET /api/v1/auth/login/{provider}`
  - [x] `GET /api/v1/auth/callback/{provider}`
  - [x] `POST /api/v1/auth/social/complete`
  - [x] `POST /api/v1/auth/social/link`
  - [x] `DELETE /api/v1/auth/social/unlink/{provider}`
- [x] 보안 접근 제어 보정
  - [x] 공개 인증 엔드포인트만 `permitAll`
  - [x] `logout`, `social/complete`, `social/link`, `social/unlink`는 bearer 인증 요구
- [x] Auth API 테스트 추가
  - [x] 회원가입 -> 이메일 인증 -> 로그인 -> refresh -> 로그아웃 -> 비밀번호 재설정 흐름
  - [x] 소셜 redirect/callback/complete/link/unlink 흐름
  - [x] 보호된 인증 엔드포인트 `AUTH_401` 확인
- [x] 기존 Flyway 테스트 보정
  - [x] 현재 버전 `v2` 반영
  - [x] auth 지원 테이블 존재 검증 추가
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `5. 인증(Auth) API 구현`에 해당한다.
- 다음 단계는 `6. 사용자(User) API 구현`이다.
