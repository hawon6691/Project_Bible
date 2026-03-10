---
title: "[FEAT] PHP Swagger 생성 복구"
labels: ["documentation", "php", "swagger", "openapi"]
issue: "[FEAT] PHP Swagger 생성 복구"
commit: "feat: (#371) PHP swagger 생성 복구"
branch: "feat/#371/php-swagger-generation-recovery"
---

## ✨ 기능 요약
- PHP Laravel 백엔드에서 실패하던 Swagger 생성 흐름을 복구하고 실제 산출물 생성까지 확인한다.

## 📋 요구사항
- [x] `php artisan l5-swagger:generate` 실패 원인 추적
- [x] `config/l5-swagger.php`의 잘못된 상수 참조 제거
- [x] `config/scribe.php`의 패키지 미설치 상태에서도 부팅을 막지 않도록 방어 로직 추가
- [x] 누락된 문서 패키지 복구
  - [x] `darkaonline/l5-swagger`
  - [x] `knuckleswtf/scribe`
- [x] Swagger 생성 명령 재실행
- [x] 생성 산출물 확인
  - [x] `storage/api-docs/api-docs.json`
  - [x] `storage/api-docs/api-docs.yaml`

## 📌 원인 분석
- `config/l5-swagger.php`에서 존재하지 않는 `L5Swagger\Generator` 상수를 기본값으로 사용하고 있었다.
- `config/scribe.php`에서 `Knuckles\Scribe\Config\AuthIn`, `Defaults`, `Strategies`를 직접 참조해 패키지 미설치 상태에서 부팅을 막고 있었다.
- `composer.lock`에는 문서 패키지가 기록되어 있었지만 실제 `vendor`에는 설치되어 있지 않았다.

## ✅ 처리 결과
- `config/l5-swagger.php` 기본 OpenAPI 버전을 문자열 기본값으로 수정했다.
- `config/scribe.php`를 `enum_exists()`, `class_exists()`, `function_exists()` 기반 방어 로직으로 보강했다.
- `php C:\ProgramData\ComposerSetup\bin\composer install --no-interaction --prefer-dist -vvv`로 누락 패키지를 복구했다.
- `php artisan l5-swagger:generate` 실행 성공을 확인했다.
- `storage/api-docs/api-docs.json`, `storage/api-docs/api-docs.yaml` 생성까지 확인했다.

## 📂 영향 범위
- `BackEnd/PHP/laravelshop/config/l5-swagger.php`
- `BackEnd/PHP/laravelshop/config/scribe.php`
- `BackEnd/PHP/laravelshop/storage/api-docs/api-docs.json`
- `BackEnd/PHP/laravelshop/storage/api-docs/api-docs.yaml`
