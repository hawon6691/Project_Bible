---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] PHP Eloquent ORM Project Rename"
labels: chore
assignees: ""
issue: "[CHORE] PHP Eloquent ORM Project Rename 문서 작성"
commit: "chore: (#429) PHP Eloquent ORM 프로젝트 이름 정리"
branch: "chore/#429/php-eloquentorm-project-rename"
---

## 🛠️ 작업 요약

> PHP 구현체와 CI 이름을 `php-laravel-composer-eloquent-postgresql`, `php-eloquentorm-ci` 기준으로 정리한다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

- PHP 백엔드 구현은 Laravel 기반이며 데이터 접근 방식은 Eloquent ORM 기준이다.
- 구현체 식별자와 CI 이름도 실제 구현 성격에 맞게 `eloquentorm` 기준으로 맞출 필요가 있었다.
- 기존 `laravelshop`, `php-laravel-ci` 표기는 프레임워크 중심 이름이어서 ORM 기준 비교에 불리했다.

## 📋 요구사항

- [x] PHP 프로젝트 폴더명을 `php-laravel-composer-eloquent-postgresql`으로 변경
- [x] CI 파일명을 `php-laravel-composer-eloquent-postgresql-ci.yml`로 변경
- [x] workflow 표시명을 `PBShop PHP Eloquent ORM CI`로 수정
- [x] concurrency group을 `php-eloquentorm-ci` 기준으로 수정
- [x] workflow 내부 경로 및 artifact 경로 수정
- [x] `BackEnd/PHP/README.md` 경로 및 이름 수정
- [x] 루트 README의 PHP 백엔드 경로 수정
- [x] PHP 문서 세트의 구현체 경로 수정
- [x] 공통 CI/테스트 문서의 PHP 구현체 경로 수정
- [x] 이전 `BackEnd/PHP/laravelshop`, `php-laravel-ci` 참조 제거 확인

## ✅ 산출물

- `.github/workflows/php-laravel-composer-eloquent-postgresql-ci.yml`
- `BackEnd/PHP/php-laravel-composer-eloquent-postgresql`
- `BackEnd/PHP/README.md`
- `README.md`
- `Document/06_ci-specification.md`
- `Document/05_test-specification.md`
- `Document/PHP/01_folder-structure.md`
- `Document/PHP/02_operations-runbook.md`
- `Document/PHP/03_implementation-status.md`
- `Document/PHP/04_completion-report.md`

## 검증 메모

- 프로젝트 폴더명이 `php-laravel-composer-eloquent-postgresql`으로 변경됨
- CI 파일명이 `php-laravel-composer-eloquent-postgresql-ci.yml`로 정리됨
- workflow 내부 경로가 새 폴더 기준으로 수정됨
- README와 문서의 구현체 경로가 새 이름 기준으로 수정됨
- 기존 `BackEnd/PHP/laravelshop`, `php-laravel-ci`, `PBShop PHP Laravel CI` 참조가 남지 않도록 검색 확인함

## 메모

- 이번 변경은 서비스 기능을 바꾸는 작업이 아니라 구현체 식별자와 CI 이름을 Eloquent ORM 기준으로 정리하는 작업이다.
- Laravel 프레임워크 자체와 서비스 도메인 문구는 rename 범위에 포함하지 않는다.
