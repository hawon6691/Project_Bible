# PBShop Laravel Migration Strategy

## 기준

- 기준 스키마는 `Document/03_erd.md` 와 `Database/mysql/mysql_table.sql` 을 따른다.
- Laravel migration은 PHP 트랙의 실행 가능한 스키마 기준본으로 유지한다.
- 엔진 기본값은 `MySQL(pbdb)` 이고, `MariaDB`, `PostgreSQL` 은 환경 변수로 전환 가능하게 유지한다.

## 원칙

1. 공통 인프라 테이블(`cache`, `jobs`, `sessions`, `failed_jobs`)은 Laravel 기본 migration을 유지한다.
2. PBShop 도메인 테이블은 기능 단계별로 추가한다.
3. 초기 단계에서는 핵심 도메인부터 작성한다.
   - `users`
   - `categories`
   - `sellers`
   - `products`
   - `product_specs`
   - `price_entries`
   - `system_settings`
4. 각 migration은 TypeScript 기준 테이블명과 최대한 동일한 이름을 사용한다.
5. 프론트 호환을 위해 API 구현 전에 필수 테이블 구조를 먼저 고정한다.
