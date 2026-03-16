# PBShop JavaScript Express Prisma ORM

공통 문서와 공통 DB 자산을 기준으로 진행하는 JavaScript ORM 구현체입니다.

## Run

```bash
npm install
cp .env.example .env
npm run db:docker:up
npm run db:reset
npm run prisma:generate
npm run dev
```

## Shared Database Flow

```bash
npm run db:check
npm run db:init
```

빈 DB가 아니라 이미 공통 테이블이 들어 있는 경우에는 아래를 사용합니다.

```bash
npm run db:reset
```

## Routes

- `GET /health`
- `GET /api/v1/health`
- `POST /api/v1/auth/verify-email`
- `POST /api/v1/auth/password-reset/request`
- `GET /api/v1/categories`
- `POST /api/v1/categories`
- `GET /api/v1/products`
- `POST /api/v1/products`
- `GET /api/v1/sellers`
- `POST /api/v1/sellers`
- `POST /api/v1/specs/compare`
- `GET /api/v1/price-alerts`
- `GET /api/v1/docs-status`

## Stack

- Express
- Prisma ORM
- PostgreSQL

## Shared Assets

- `Database/docker/docker-compose.yml`
- `Database/postgresql/postgres_table.sql`
- `Database/postgresql/sample_data.sql`

## Notes

- 현재 Prisma schema는 공통 PostgreSQL 스키마 기준으로 1차 정렬된 상태입니다.
- 실제 DB 초기화는 Prisma migration이 아니라 공통 SQL import를 우선 사용합니다.
