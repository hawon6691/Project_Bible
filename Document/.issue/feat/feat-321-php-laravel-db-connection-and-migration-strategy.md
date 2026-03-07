---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Laravel DB 연결 및 기본 마이그레이션 전략 확정"
labels: feature
assignees: ""
issue: "[FEAT] PHP Laravel DB 연결 및 기본 마이그레이션 전략 확정"
commit: "feat: (#321) PHP Laravel DB 연결 및 기본 마이그레이션 전략 확정"
branch: "feat/#321/php-laravel-db-connection-and-migration-strategy"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 MySQL 기준 DB 연결을 검증하고, 핵심 도메인 기준의 Laravel 기본 마이그레이션 전략을 확정한다.

## 📋 요구사항

- [x] 현재 Laravel migration 상태 확인
  - [x] 기본 Laravel migration(`users`, `cache`, `jobs`) 확인
  - [x] 기본 `User` 모델 상태 확인
- [x] 기존 PBShop MySQL 스키마 기준 확인
  - [x] Docker MySQL 컨테이너(`projectbible-mysql`) 접속 확인
  - [x] PBShop DB(`pbdb`) 기준 연결 가능성 확인
- [x] 핵심 도메인 migration 보강
  - [x] `0001_01_01_000000_create_users_table.php` 를 PBShop 기준으로 확장
  - [x] `users` 테이블을 PBShop 컬럼 기준으로 보강
    - [x] `nickname`
    - [x] `role`
    - [x] `status`
    - [x] `phone`
    - [x] `profile_image_url`
    - [x] `bio`
    - [x] `last_login_at`
  - [x] 핵심 카탈로그 테이블 추가
    - [x] `categories`
    - [x] `sellers`
    - [x] `products`
    - [x] `product_specs`
    - [x] `price_entries`
- [x] 기본 인프라 migration 추가
  - [x] `system_settings` 테이블 migration 추가
- [x] 마이그레이션 전략 문서화
  - [x] `database/migrations/README.md` 추가
  - [x] 기준 문서와 SQL 기준본(`03_erd.md`, `Database/mysql/mysql_table.sql`) 참조 원칙 명시
  - [x] 초기 핵심 도메인 테이블 우선 전략 명시
- [x] User 모델 보강
  - [x] mass assignable 필드 확장
  - [x] `last_login_at` cast 추가
- [x] DB 연결 및 migration 검증
  - [x] 임시 검증 DB `pbdb_php_validation` 생성
  - [x] 검증 DB 권한 부여
  - [x] `php artisan migrate:fresh --force` 통과
  - [x] 핵심 migration 적용 성공 확인
