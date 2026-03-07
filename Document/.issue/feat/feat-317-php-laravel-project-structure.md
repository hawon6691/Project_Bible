---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Laravel 프로젝트 구조 정리"
labels: feature
assignees: ""
issue: "[FEAT] PHP Laravel 프로젝트 구조 정리"
commit: "feat: (#317) PHP Laravel 프로젝트 구조 정리"
branch: "feat/#317/php-laravel-project-structure"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 TypeScript 기준 구조를 참고한 Laravel API 프로젝트 기본 구조를 정리하고, `/api/v1` 라우트 진입점을 연결한다.

## 📋 요구사항

- [x] TypeScript 기준 구조 확인
  - [x] `Document/TypeScript/01_folder-structure.md` 참고
  - [x] `BackEnd/TypeScript/nestshop/src` 모듈 구조 참고
- [x] Laravel 기본 구조 점검
  - [x] `app`, `routes`, `bootstrap/app.php` 현재 상태 확인
  - [x] Laravel 12 기준 `routes/api.php` 부재 확인
- [x] 공통 구조 추가
  - [x] `app/Common/Constants/ApiRoutes.php`
  - [x] `app/Common/Exceptions/BusinessException.php`
  - [x] `app/Common/Http/ApiResponse.php`
  - [x] `app/Http/Requests/ApiRequest.php`
  - [x] `app/Http/Middleware/SetApiLocale.php`
- [x] API 컨트롤러 구조 추가
  - [x] `app/Http/Controllers/Api/V1/ApiController.php`
  - [x] `app/Http/Controllers/Api/V1/System/SystemController.php`
- [x] 향후 구현용 디렉터리 구조 추가
  - [x] `app/Services`
  - [x] `app/Repositories`
  - [x] `app/DTOs`
  - [x] `app/Modules/Auth`
  - [x] `app/Modules/User`
  - [x] `app/Modules/Category`
  - [x] `app/Modules/Product`
- [x] API 라우트 구조 추가
  - [x] `routes/api_v1.php`
  - [x] `routes/api_v1/system.php`
  - [x] `routes/api_v1/auth.php`
  - [x] `routes/api_v1/users.php`
  - [x] `routes/api_v1/categories.php`
  - [x] `routes/api_v1/products.php`
- [x] Laravel 부트스트랩 연결
  - [x] `bootstrap/app.php` 에 API 라우트 등록
  - [x] `api.locale` 미들웨어 alias 등록
  - [x] `/api/v1` 기준 prefix 정리
- [x] 기본 실행 검증
  - [x] `php artisan route:list` 통과
  - [x] `GET /api/v1/health` 라우트 등록 확인
  - [x] `php artisan about --only=environment` 통과
