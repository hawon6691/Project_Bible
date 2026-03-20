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

## Test

```bash
npm run test:quality
npm run test:e2e:critical
npm run test:e2e:contract
npm run test:e2e:auth-search
npm run test:e2e:queue-admin
npm run test:e2e:observability
npm run test:e2e:admin-boundary
npm run test:e2e:rate-limit-regression
npm run test:e2e:security-regression
npm run test:e2e:ops
npm run test:e2e:platform
npm run test:api:domain
npm run test:perf:smoke
npm run test:script:validate-migrations
```

## CI

- GitHub Actions workflow: `.github/workflows/javascript-express-npm-prisma-postgresql-ci.yml`
- 자동 잡: `quality`, `critical-e2e`, `contract-doc`, `perf-smoke`
- 수동 잡: `migration-validation-manual`, `migration-roundtrip-manual`, `security-regression-manual`, `admin-boundary-manual`, `release-gate`
- Shared PostgreSQL SQL을 올린 뒤 `npm run prisma:generate`, `npm run test:e2e:critical`, `npm run test:contract:doc`, `npm run test:perf:smoke`를 실행합니다.

## Docs

- OpenAPI JSON: `/docs/openapi`
- Swagger UI: `/docs/swagger`
- OpenAPI artifact export: `npm run docs:export`
- Socket.IO chat events are documented in the OpenAPI `x-socket-events` extension.

## Routes

- `GET /health`
- `GET /api/v1/health`
- `POST /api/v1/auth/verify-email`
- `POST /api/v1/auth/password-reset/request`
- `GET /api/v1/badges`
- `GET /api/v1/categories`
- `POST /api/v1/categories`
- `GET /api/v1/products`
- `POST /api/v1/products`
- `GET /api/v1/query/products`
- `GET /api/v1/query/products/:productId`
- `POST /api/v1/admin/query/products/:productId/sync`
- `POST /api/v1/admin/query/products/rebuild`
- `GET /api/v1/sellers`
- `POST /api/v1/sellers`
- `GET /api/v1/errors/codes`
- `GET /api/v1/errors/codes/:key`
- `GET /api/v1/predictions/products/:productId/price-trend`
- `GET /api/v1/i18n/translations`
- `GET /api/v1/i18n/exchange-rates`
- `GET /api/v1/i18n/convert`
- `POST /api/v1/admin/i18n/translations`
- `GET /api/v1/admin/settings/extensions`
- `PATCH /api/v1/admin/settings/upload-limits`
- `PATCH /api/v1/admin/settings/review-policy`
- `GET /api/v1/resilience/circuit-breakers`
- `GET /api/v1/resilience/circuit-breakers/policies`
- `POST /api/v1/resilience/circuit-breakers/:name/reset`
- `GET /api/v1/admin/queues/supported`
- `GET /api/v1/admin/queues/stats`
- `POST /api/v1/admin/queues/auto-retry`
- `GET /api/v1/admin/ops-dashboard/summary`
- `POST /api/v1/images/upload`
- `POST /api/v1/upload/image`
- `GET /api/v1/images/:id/variants`
- `DELETE /api/v1/images/:id`
- `POST /api/v1/users/me/profile-image`
- `DELETE /api/v1/users/me/profile-image`
- `GET /api/v1/pc-builds`
- `POST /api/v1/pc-builds`
- `GET /api/v1/pc-builds/:id`
- `POST /api/v1/pc-builds/:id/parts`
- `GET /api/v1/pc-builds/:id/compatibility`
- `GET /api/v1/pc-builds/:id/share`
- `GET /api/v1/pc-builds/shared/:shareCode`
- `GET /api/v1/pc-builds/popular`
- `POST /api/v1/products/:id/images`
- `DELETE /api/v1/products/:id/images/:imageId`
- `GET /api/v1/admin/compatibility-rules`
- `GET /api/v1/admin/observability/metrics`
- `GET /api/v1/admin/observability/traces`
- `GET /api/v1/admin/observability/dashboard`
- `GET /api/v1/crawler/admin/jobs`
- `POST /api/v1/crawler/admin/jobs`
- `GET /api/v1/crawler/admin/runs`
- `GET /api/v1/search`
- `GET /api/v1/search/autocomplete`
- `GET /api/v1/search/popular`
- `POST /api/v1/search/recent`
- `GET /api/v1/search/admin/index/status`
- `POST /api/v1/search/admin/index/reindex`
- `GET /api/v1/search/admin/index/outbox/summary`
- `POST /api/v1/chat/rooms/:id/join`
- `POST /api/v1/chat/rooms/:id/messages`
- `GET /api/v1/sellers/:id/trust`
- `POST /api/v1/sellers/:id/reviews`
- `POST /api/v1/admin/badges`
- `POST /api/v1/push/subscriptions`
- `GET /api/v1/push/preferences`
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
- Socket.IO 채팅 이벤트는 JWT 인증 소켓 연결을 전제로 합니다.
- 최근 추가된 parity 기능은 `Error Code Catalog`, `Query API`, `Chat REST gap`, `Chat Socket.IO`입니다.
