# Kotlin Operations Runbook

## Local Run

```bash
./gradlew run
```

## DB Bootstrap

```bash
./gradlew dbInit
./gradlew dbSmoke
```

## Test

```bash
./gradlew test
```

## Docs Export

```bash
./gradlew docsExport
```

## 주요 운영 경로

- `/health`
- `/api/v1/health`
- `/api/v1/docs-status`
- `/docs/openapi`
- `/docs/swagger`
- `/api/v1/admin/ops-dashboard/summary`
- `/api/v1/admin/observability/dashboard`
