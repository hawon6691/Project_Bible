# Kotlin Language API Specification

## Baseline

- Project: `kotlin-ktor-gradle-exposeddao-postgresql`
- Stack: `Ktor + Gradle + Exposed DAO + PostgreSQL`
- API prefix: `/api/v1`
- Docs: `/docs/openapi`, `/docs/swagger`

## Implemented Scope

- 공통 응답 contract
- Request ID / security headers / in-memory rate limit
- 공통 명세 기준 commerce, engagement, discovery, media, builder, ops stub API
- OpenAPI export / Swagger HTML
- Kotlin 도메인/운영 테스트 자산

## Notable Routes

- `GET /health`
- `GET /api/v1/health`
- `GET /api/v1/docs-status`
- `GET /docs/openapi`
- `GET /docs/swagger`
- `GET /api/v1/products`
- `POST /api/v1/orders`
- `GET /api/v1/search`
- `GET /api/v1/admin/ops-dashboard/summary`
- `GET /api/v1/admin/observability/dashboard`
