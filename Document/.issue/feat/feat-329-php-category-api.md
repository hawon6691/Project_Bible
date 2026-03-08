---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Category API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Category API 구현"
commit: "feat: (#329) PHP Category API 구현"
branch: "feat/#329/php-category-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 계층형 카테고리 조회와 관리자 카테고리 관리 API를 구현한다.

## 📋 요구사항

- [x] Category 모델 추가
  - [x] `Category` 모델 생성
  - [x] `parent`, `children` 관계 정의
  - [x] `is_visible` 캐스팅 설정
- [x] Category 요청 검증 클래스 추가
  - [x] `CreateCategoryRequest`
  - [x] `UpdateCategoryRequest`
- [x] Category 서비스 구현
  - [x] 전체 카테고리 트리 조회
  - [x] 카테고리 단건 조회
  - [x] 관리자 카테고리 생성
  - [x] 관리자 카테고리 수정
  - [x] 관리자 카테고리 삭제
  - [x] 카테고리 `slug`, `depth`, `sortOrder`, `isVisible` 처리
  - [x] 하위 카테고리 존재 시 삭제 차단
- [x] 관리자 권한 검증 추가
  - [x] 비관리자 접근 시 `FORBIDDEN` 에러 반환
- [x] Category 컨트롤러/라우트 연결
  - [x] `GET /api/v1/categories`
  - [x] `GET /api/v1/categories/{id}`
  - [x] `POST /api/v1/categories`
  - [x] `PATCH /api/v1/categories/{id}`
  - [x] `DELETE /api/v1/categories/{id}`
- [x] 응답 구조 정리
  - [x] 트리 응답에 `children` 포함
  - [x] 단건 응답에 부모 카테고리 정보 포함
  - [x] 카멜 케이스 응답 필드(`parentId`, `sortOrder`, `isVisible`) 사용
- [x] Category API 테스트 추가
  - [x] `tests/Feature/Api/CategoryApiTest.php` 추가
  - [x] 공개 트리 조회/단건 조회 검증
  - [x] 관리자 생성/수정/삭제 검증
  - [x] 비관리자 접근 차단 검증
  - [x] 하위 카테고리 존재 시 삭제 차단 검증
  - [x] `php artisan test tests/Feature/Api/CategoryApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list --path=api/v1/categories` 통과
