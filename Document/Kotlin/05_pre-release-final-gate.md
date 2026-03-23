# Kotlin Pre-release Final Gate

## Release Checklist

- [ ] `./gradlew test`
- [ ] `./gradlew docsExport`
- [ ] `./gradlew dbInit`
- [ ] `./gradlew dbSmoke`
- [ ] `/docs/openapi` 산출물 확인
- [ ] `/docs/swagger` 렌더링 확인
- [ ] `/api/v1/admin/ops-dashboard/summary` 확인
- [ ] CI 자동 잡 통과 확인
- [ ] 필요한 수동 게이트 실행 여부 확인

## Manual Gates

- `contract-e2e`
- `migration-validation`
- `migration-roundtrip`
- `security-regression`
- `admin-boundary`
- `rate-limit-regression`
- `dependency-failure`
- `stability-check`
- `perf-smoke`
- `perf-extended`
- `live-smoke`
