---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Laravel 공통 환경 설정 정리"
labels: feature
assignees: ""
issue: "[FEAT] PHP Laravel 공통 환경 설정 정리"
commit: "feat: (#319) PHP Laravel 공통 환경 설정 정리"
branch: "feat/#319/php-laravel-common-config"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 공통 환경 설정을 정리하고, Laravel 기본값을 프로젝트 기준에 맞게 고정한다.

## 📋 요구사항

- [x] 환경 변수 정리
  - [x] `.env` 보강
  - [x] `.env.example` 보강
  - [x] `APP_TIMEZONE=Asia/Seoul` 추가
  - [x] `FRONTEND_URL=http://localhost:3001` 추가
  - [x] `APP_SUPPORTED_LOCALES=ko,en,ja` 추가
  - [x] `APP_SUPPORTED_CURRENCIES=KRW,USD,JPY` 추가
  - [x] `API_PREFIX=v1` 추가
- [x] DB 환경 변수 정리
  - [x] MySQL 기본값 유지
  - [x] MariaDB 전용 env 추가
    - [x] `DB_MARIA_HOST`
    - [x] `DB_MARIA_PORT`
    - [x] `DB_MARIA_DATABASE`
    - [x] `DB_MARIA_USERNAME`
    - [x] `DB_MARIA_PASSWORD`
  - [x] PostgreSQL 전용 env 추가
    - [x] `DB_PGSQL_HOST`
    - [x] `DB_PGSQL_PORT`
    - [x] `DB_PGSQL_DATABASE`
    - [x] `DB_PGSQL_USERNAME`
    - [x] `DB_PGSQL_PASSWORD`
- [x] Laravel 기본 설정 보정
  - [x] `config/app.php` timezone을 env 기반으로 변경
  - [x] `config/database.php` default connection을 `mysql`로 변경
  - [x] `config/database.php`의 MySQL/MariaDB/PostgreSQL 기본값을 PBShop 기준으로 보정
  - [x] `config/queue.php`의 batching/failed DB 기본값을 `mysql` 기준으로 변경
- [x] PBShop 전용 설정 파일 추가
  - [x] `config/pbshop.php` 추가
  - [x] 프로젝트 메타 정보 정의
  - [x] API prefix/base path 정의
  - [x] 프론트엔드 URL 정의
  - [x] 공통 응답 계약 메타 정의
  - [x] 지원 언어/화폐 목록 정의
  - [x] 인프라 기본값(`database`, `cache_store`, `queue_connection`, `session_driver`) 정의
- [x] 설정 검증
  - [x] `php artisan config:show pbshop` 통과
  - [x] `php artisan config:show app` 통과
  - [x] `php artisan config:show database` 통과
  - [x] `pgsql` 설정이 `5432`로 분리되는 것 확인
