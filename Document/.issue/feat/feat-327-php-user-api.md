---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP User API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP User API 구현"
commit: "feat: (#327) PHP User API 구현"
branch: "feat/#327/php-user-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 사용자 정보, 공개 프로필, 프로필 이미지, 관리자 회원 관리 API를 구현한다.

## 📋 요구사항

- [x] User 요청 검증 클래스 추가
  - [x] `UpdateMeRequest`
  - [x] `UpdateProfileRequest`
  - [x] `UpdateUserStatusRequest`
  - [x] `UploadProfileImageRequest`
- [x] User 서비스 구현
  - [x] 내 정보 조회
  - [x] 내 정보 수정
  - [x] 회원 탈퇴
  - [x] 공개 프로필 조회
  - [x] 닉네임/소개글 수정
  - [x] 프로필 이미지 업로드
  - [x] 프로필 이미지 삭제
  - [x] 관리자 회원 목록 조회
  - [x] 관리자 회원 상태 변경
- [x] 관리자 권한 검증 추가
  - [x] `UserService`에서 `ADMIN` 역할 확인
  - [x] 비관리자 접근 시 `FORBIDDEN` 에러 반환
- [x] User 컨트롤러/라우트 연결
  - [x] `GET /api/v1/users/me`
  - [x] `PATCH /api/v1/users/me`
  - [x] `DELETE /api/v1/users/me`
  - [x] `GET /api/v1/users/{id}/profile`
  - [x] `PATCH /api/v1/users/me/profile`
  - [x] `POST /api/v1/users/me/profile-image`
  - [x] `DELETE /api/v1/users/me/profile-image`
  - [x] `GET /api/v1/users`
  - [x] `PATCH /api/v1/users/{id}/status`
- [x] 사용자 응답 직렬화 정리
  - [x] `UserResponse` 형태의 주요 필드 응답 구성
  - [x] 공개 프로필 응답에서 민감 정보 제외
  - [x] 관리자 목록 조회 시 pagination 메타 포함
- [x] User API 테스트 추가
  - [x] `tests/Feature/Api/UserApiTest.php` 추가
  - [x] 내 정보 조회/수정 검증
  - [x] 프로필 수정/이미지 업로드/이미지 삭제 검증
  - [x] 회원 탈퇴 검증
  - [x] 공개 프로필 조회 검증
  - [x] 관리자 회원 목록/상태 변경 검증
  - [x] 비관리자 접근 차단 검증
  - [x] `php artisan test tests/Feature/Api/UserApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list --path=api/v1/users` 통과
