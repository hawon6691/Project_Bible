---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 데이터베이스 스키마 정합화 및 멀티 엔진 시드 정리"
labels: feature
issue: "[FEAT] 데이터베이스 스키마 정합화 및 멀티 엔진 시드 정리"
commit: "feat: (#311) 데이터베이스 스키마 정합화 및 멀티 엔진 시드 정리"
branch: "feat/#311/database-schema-sync-and-multi-engine-seed"
assignees: ""
---

## ✨ 기능 요약

> TypeScript + PostgreSQL 기준 구현에 맞춰 문서/DB SQL을 정합화하고, MySQL/MariaDB용 스키마 및 시드 베이스라인을 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 요구사항/명세/ERD와 TypeScript 구현 차이점 확인
- [x] 문서 정합화
  - [x] `Document/01_requirements.md` 보강
  - [x] `Document/02_api-specification.md` 실제 구현 기준으로 수정
  - [x] `Document/03_erd.md` 구현 테이블 매핑/보강 메모 반영
- [x] PostgreSQL 스키마 보강
  - [x] `Database/postgresql/postgres_table.sql` 에 현재 구현 기준 보강 테이블 추가
  - [x] 기존 초안 테이블 중 실제 Entity 구조와 다른 정의 수정
  - [x] `chat_rooms`
  - [x] `chat_messages`
  - [x] `deals`
  - [x] `crawler_jobs`
  - [x] `push_subscriptions`
  - [x] `image_variants`
  - [x] `user_badges`
- [x] PostgreSQL 샘플 데이터 보강
  - [x] `Database/postgresql/sample_data.sql` 를 현재 구현 테이블명 기준으로 수정
  - [x] 구현 테이블 insert 보강
  - [x] `recent_product_views`
  - [x] `search_histories`
  - [x] `chat_room_members`
  - [x] `crawler_runs`
  - [x] `push_preferences`
  - [x] `trust_score_histories`
  - [x] `social_accounts`
  - [x] `shortforms`
  - [x] `media_assets`
  - [x] `news`
  - [x] `news_products`
  - [x] `fraud_flags`
  - [x] `auction_bids`
  - [x] `system_settings`
  - [x] 현재 구현에 없는 구식 초안 테이블은 조건부 삽입 형태로 유지
- [x] PostgreSQL 적용 검증
  - [x] Docker PostgreSQL 컨테이너 기준 임시 검증 DB 생성
  - [x] `postgres_table.sql` 적용 성공
  - [x] `sample_data.sql` 적용 성공
- [x] MySQL 스키마/시드 초안 작성
  - [x] `Database/mysql/mysql_table.sql` 추가
  - [x] `Database/mysql/mysql_sample_data.sql` 추가
  - [x] TypeScript/PostgreSQL 기준 테이블명과 핵심 도메인 구조를 MySQL용으로 정리
- [x] Marie(MariaDB) 스키마/시드 초안 작성
  - [x] `Database/marie/marie_table.sql` 추가
  - [x] `Database/marie/marie_sample_data.sql` 추가
  - [x] 독립 실행 가능한 파일로 정리
  - [x] MariaDB 기준 헤더/주석 정리
- [x] MySQL/Marie 실행 검증
  - [x] Docker MySQL 컨테이너(`projectbible-mysql`) 기준 검증
  - [x] `pbdb_mysql_validation` 임시 DB에 MySQL 스키마/시드 적용 성공
  - [x] `pbdb_marie_validation` 임시 DB에 Marie 스키마/시드 적용 성공
- [x] 프로젝트 기준 경로 재정렬
  - [x] 기준 루트를 `C:\00_work\00_work\03_Project_Bible\Project_Bible` 로 통일
  - [x] 프로젝트 내부 기준으로 `mysql`, `marie` 파일 위치 재확인

## 📌 참고

- PostgreSQL은 TypeScript 기준 구현 검증용 기준본으로 사용한다.
- MySQL, Marie(MariaDB) SQL은 다음 백엔드 언어 트랙을 위한 베이스라인으로 사용한다.
