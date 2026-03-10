---
name: "Java Maven Common Contract And Security"
about: "Java Spring Boot Maven 공통 응답/예외 처리/보안 기본 구성"
title: "[FEAT] Java Maven 공통 응답, 예외 처리, 보안 기본 구성"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "security", "exception"]
assignees: []
issue: "[FEAT] Java Maven 공통 응답, 예외 처리, 보안 기본 구성"
commit: "feat: (#383) Java Maven 공통 응답 예외 처리 보안 기본 구성"
branch: "feat/#383/java-maven-common-contract-and-security"
---

## ✨ 기능 요약

Java Maven 기준 구현의 공통 API 계약을 고정한다. 성공/실패 응답 envelope, request context, 전역 예외 처리, 인증 실패 응답, 기본 보안 설정을 공통 계층으로 정리해 이후 모듈 구현 시 일관된 규격을 강제한다.

## 📋 요구사항

- [x] 공통 API 응답 포맷 보강
  - [x] `ApiResponse` 재구성
  - [x] `meta.requestId`
  - [x] `meta.locale`
  - [x] `meta.currency`
  - [x] 에러 상세 `details` 지원
- [x] 공통 요청 컨텍스트 추가
  - [x] `ApiRequestContext`
  - [x] `ApiRequestContextFilter`
  - [x] `X-Request-Id` 응답 헤더 반영
  - [x] `Accept-Language` 기반 locale 반영
- [x] 전역 예외 처리 계층 추가
  - [x] `ErrorCode`
  - [x] `BusinessException`
  - [x] `ApiExceptionHandler`
  - [x] validation 오류 공통 응답 처리
  - [x] unhandled exception 공통 응답 처리
- [x] 보안 기본 구성 보강
  - [x] `SecurityProblemSupport`
  - [x] 인증 실패 `AUTH_401` JSON 응답
  - [x] 권한 실패 `AUTH_403` JSON 응답
  - [x] `PasswordEncoder` 기본 bean 추가
  - [x] request context filter를 security chain 앞단에 연결
- [x] 테스트 공통 베이스 추가
  - [x] `SystemControllerTestSupport`
- [x] 공통 계약 테스트 추가
  - [x] 비인증 접근 `AUTH_401`
  - [x] 비즈니스 예외 `COMMON_400`
  - [x] validation 예외 `COMMON_400_VALIDATION`
  - [x] request id/meta 반영 확인
- [x] 기존 시스템 테스트 구조 정리
  - [x] `SystemControllerTest`를 공통 베이스 사용 구조로 변경
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `4. 공통 응답 포맷/예외 처리/보안 기본 구성`에 해당한다.
- 다음 단계는 `5. 인증(Auth) API 구현`이다.
