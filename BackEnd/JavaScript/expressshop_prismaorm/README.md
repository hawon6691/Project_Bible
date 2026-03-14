# PBShop JavaScript Express Prisma ORM

공통 문서와 공통 DB 자산을 기준으로 진행하는 JavaScript ORM 구현체입니다.

## Run

```bash
npm install
cp .env.example .env
npm run prisma:generate
npm run dev
```

공통 Docker DB를 사용할 때:

```bash
npm run db:docker:up
```

## Routes

- `GET /health`
- `GET /api/v1/health`
- `GET /api/v1/categories`
- `GET /api/v1/products`
- `GET /api/v1/docs-status`

## Stack

- Express
- Prisma ORM
- PostgreSQL

## Shared Assets

- `Database/docker/docker-compose.yml`
- `Database/postgresql/postgres_table.sql`
- `Database/postgresql/sample_data.sql`
