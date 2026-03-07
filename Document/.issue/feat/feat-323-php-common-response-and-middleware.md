---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP 공통 응답 포맷 및 미들웨어 구성"
labels: feature
assignees: ""
issue: "[FEAT] PHP 공통 응답 포맷 및 미들웨어 구성"
commit: "feat: (#323) PHP 공통 응답 포맷 및 미들웨어 구성"
branch: "feat/#323/php-common-response-and-middleware"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 공통 API 응답 계약, 예외 처리, 요청 컨텍스트 미들웨어를 구성한다.

## 📋 요구사항

- [x] 공통 응답 포맷 보강
  - [x] `ApiResponse`가 `success/data/meta/error` 구조를 유지하도록 정리
  - [x] `meta.requestId`, `meta.locale`, `meta.currency` 자동 포함
- [x] 요청 컨텍스트 미들웨어 추가
  - [x] `SetApiRequestContext` 추가
  - [x] `X-Request-Id` 처리
  - [x] `X-Currency` 처리
  - [x] 응답 헤더에 `X-Request-Id`, `X-Currency` 반영
- [x] 로케일 미들웨어 보강
  - [x] `SetApiLocale`가 `config('pbshop.i18n.supported_locales')` 기준 사용
  - [x] 응답 헤더에 `Content-Language` 반영
- [x] 전역 예외 처리 구성
  - [x] `BusinessException` 렌더링
  - [x] `ValidationException` 렌더링
  - [x] `AuthenticationException` 렌더링
  - [x] `NotFoundHttpException` 렌더링
  - [x] 기타 예외를 API 표준 에러 포맷으로 렌더링
- [x] Laravel 부트스트랩 반영
  - [x] `bootstrap/app.php`에 `api.context` 미들웨어 alias 추가
  - [x] `/api/v1` 라우트 그룹에 `api.context`, `api.locale` 적용
- [x] 공통 계약 테스트 추가
  - [x] `tests/Feature/Api/HealthRouteTest.php` 추가
  - [x] `/api/v1/health` 응답 본문/헤더 검증
  - [x] `php artisan test tests/Feature/Api/HealthRouteTest.php` 통과
