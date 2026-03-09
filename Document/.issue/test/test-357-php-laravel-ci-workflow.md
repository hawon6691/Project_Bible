---
name: "🧪 Test Request"
about: "테스트 및 검증 작업"
title: "[TEST] PHP Laravel GitHub Actions CI 추가"
labels: ["test"]
issue: "[TEST] PHP Laravel GitHub Actions CI 추가"
commit: "test: (#357) PHP Laravel GitHub Actions CI 추가"
branch: "test/#357/php-laravel-ci-workflow"
---

## 🧪 테스트 요약
PHP Laravel 백엔드에 전용 GitHub Actions CI 워크플로를 추가해 PR/Push 기준 자동 검증 경로를 만든다.

## 📋 검증 항목
- [x] PHP 전용 워크플로 추가 (`.github/workflows/php-laravel-ci.yml`)
- [x] `BackEnd/PHP/laravelshop/**` 경로 기준 트리거 설정
- [x] PHP 8.3 / Composer 환경 구성
- [x] 의존성 캐시 설정
- [x] Laravel 환경 준비 (`.env`, `APP_KEY`)
- [x] 라우트 스모크 체크 (`php artisan route:list`)
- [x] 코드 스타일 체크 (`vendor/bin/pint --test`)
- [x] 전체 테스트 실행 (`php artisan test`)

## 📌 결과
- TypeScript CI와 분리된 PHP 전용 CI 경로가 추가되었다.
- 이후 PR 단계에서 Laravel 품질 검증을 자동화할 수 있다.
