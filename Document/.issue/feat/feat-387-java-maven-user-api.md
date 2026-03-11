---
name: "Java Maven User API"
about: "Java Spring Boot Maven 사용자 API 구현"
title: "[FEAT] Java Maven 사용자 API 구현"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "user"]
assignees: []
issue: "[FEAT] Java Maven 사용자 API 구현"
commit: "feat: (#387) Java Maven 사용자 API 구현"
branch: "feat/#387/java-maven-user-api"
---

## ✨ 기능 요약

Java Maven 기준 구현에 사용자 도메인을 추가한다. 내 정보 조회/수정/탈퇴, 공개 프로필, 프로필 이미지, 관리자 회원 목록/상태 변경을 구현하고 기존 Auth bearer 인증과 연동해 사용자 상태와 관리자 권한 경계를 고정한다.

## 📋 요구사항

- [x] 사용자 프로필 스키마 보강
  - [x] `V3__user_profile_support.sql` 추가
  - [x] `users.bio` 컬럼 추가
- [x] 사용자 엔티티/저장소 보강
  - [x] `User.bio` 필드 추가
  - [x] 검색용 `UserRepository` 페이지 조회 메서드 추가
- [x] User 요청 DTO 추가
  - [x] `UpdateMeRequest`
  - [x] `UpdateProfileRequest`
  - [x] `UpdateUserStatusRequest`
- [x] User 서비스 계층 구현
  - [x] 내 정보 조회
  - [x] 내 정보 수정
  - [x] 회원 탈퇴 처리
  - [x] 공개 프로필 조회
  - [x] 닉네임/소개글 수정
  - [x] 프로필 이미지 업로드/삭제
  - [x] 관리자 회원 목록 조회
  - [x] 관리자 회원 상태 변경
- [x] User 컨트롤러 추가
  - [x] `GET /api/v1/users/me`
  - [x] `PATCH /api/v1/users/me`
  - [x] `DELETE /api/v1/users/me`
  - [x] `GET /api/v1/users/{id}/profile`
  - [x] `PATCH /api/v1/users/me/profile`
  - [x] `POST /api/v1/users/me/profile-image`
  - [x] `DELETE /api/v1/users/me/profile-image`
  - [x] `GET /api/v1/users`
  - [x] `PATCH /api/v1/users/{id}/status`
- [x] 보안 규칙 보강
  - [x] 공개 프로필 `permitAll`
  - [x] 관리자 전용 엔드포인트 `AUTH_403` 처리
  - [x] 탈퇴/비활성 상태 사용자의 재로그인 차단
- [x] User API 테스트 추가
  - [x] 내 정보/프로필/프로필 이미지/탈퇴 흐름
  - [x] 관리자 회원 목록/상태 변경
  - [x] 비관리자 접근 차단
- [x] Flyway 테스트 버전 보정
  - [x] 현재 버전 `v3` 반영
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `6. 사용자(User) API 구현`에 해당한다.
- 다음 단계는 `7. 카테고리(Category) API 구현`이다.
