---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Laravel 백엔드 부트스트랩"
labels: feature
assignees: ""
issue: "[FEAT] PHP Laravel 백엔드 부트스트랩"
commit: "feat: (#315) PHP Laravel 백엔드 부트스트랩"
branch: "feat/#315/php-laravel-bootstrap"
---

## ✨ 기능 요약

> PBShop의 PHP 트랙을 시작하기 위해 Laravel 프로젝트를 부트스트랩하고 실행 가능한 기본 환경을 구성한다.

## 📋 요구사항

- [x] PHP 실행 환경 점검
  - [x] PHP 8.3 설치 여부 확인
  - [x] `BackEnd/PHP/laravelshop` 경로 상태 확인
  - [x] Composer 미설치 상태 확인
- [x] Composer 설치 기반 준비
  - [x] PHP 설치 경로 확인
  - [x] `php.ini` 생성
  - [x] Laravel/Composer 필수 확장 활성화
    - [x] `openssl`
    - [x] `curl`
    - [x] `mbstring`
    - [x] `fileinfo`
    - [x] `intl`
    - [x] `mysqli`
    - [x] `pdo_mysql`
    - [x] `pdo_pgsql`
    - [x] `pgsql`
    - [x] `sockets`
    - [x] `zip`
  - [x] Composer 설치 성공
- [x] Laravel 프로젝트 생성
  - [x] Laravel 신규 프로젝트 생성
  - [x] 생성 결과를 `BackEnd/PHP/laravelshop` 실제 경로에 반영
  - [x] 임시 생성 폴더 정리
- [x] Laravel 기본 환경 설정
  - [x] `.env` 수정
  - [x] `.env.example` 수정
  - [x] `APP_NAME=PBShop` 반영
  - [x] `APP_URL=http://localhost:8000` 반영
  - [x] `APP_LOCALE=ko` 반영
  - [x] `DB_CONNECTION=mysql` 반영
  - [x] Docker MySQL 기준 DB 설정 반영
    - [x] `DB_HOST=127.0.0.1`
    - [x] `DB_PORT=3306`
    - [x] `DB_DATABASE=pbdb`
    - [x] `DB_USERNAME=project_bible`
    - [x] `DB_PASSWORD=project_bible`
- [x] 기본 실행 검증
  - [x] `php artisan --version` 통과
  - [x] Laravel Framework 12.x 확인
