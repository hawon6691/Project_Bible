---
name: "✨ Feature Request"
about: "새로운 기능 제안"
title: "[FEAT] Java Maven 테스트, CI, Swagger 정렬"
labels: ["feature"]
issue: "[FEAT] Java Maven 테스트, CI, Swagger 정렬"
commit: "feat: (#421) Java Maven 테스트, CI, Swagger 정렬"
branch: "feat/#421/java-maven-test-ci-swagger-alignment"
assignees: ""
---

## ✨ 기능 요약
Java Maven 백엔드가 `Document/05_test-specification.md` 기준의 공통 테스트 축을 따라가도록 운영/공개 API E2E를 보강하고, 전용 GitHub Actions CI 및 Swagger/OpenAPI 검증 경로를 추가한다.

## 📋 요구사항
- [x] Java Maven 테스트 전용 공통 지원 클래스 추가 (`AuthenticatedApiIntegrationSupport`)
- [x] 운영/플랫폼 E2E 테스트 추가
- [x] `PublicApiE2ETest` 추가
- [x] `ContractPublicApiE2ETest` 추가
- [x] `AdminAuthorizationBoundaryE2ETest` 추가
- [x] `AdminPlatformE2ETest` 추가
- [x] `AuthSearchE2ETest` 추가
- [x] `OpsDashboardE2ETest` 추가
- [x] `OpsDashboardDependencyFailuresE2ETest` 추가
- [x] `OpsDashboardResilienceE2ETest` 추가
- [x] `OpsDashboardThresholdsE2ETest` 추가
- [x] `ObservabilityE2ETest` 추가
- [x] `QueueAdminE2ETest` 추가
- [x] `RateLimitRegressionE2ETest` 추가
- [x] `ResilienceAutoTuneE2ETest` 추가
- [x] `SecurityRegressionE2ETest` 추가
- [x] `SwaggerDocsE2ETest` 추가
- [x] 스크립트 성격 테스트 추가
  - [x] `AnalyzeStabilityScriptTest`
  - [x] `LiveSmokeScriptTest`
  - [x] `MigrationRoundtripScriptTest`
  - [x] `ValidateMigrationsScriptTest`
- [x] 성능 자산 추가
  - [x] `smoke.perf`
  - [x] `soak.perf`
  - [x] `spike-search.perf`
  - [x] `price-compare.perf`
  - [x] `search-ranking.perf`
  - [x] `assert-summary`
  - [x] `mock-perf-server`
- [x] bash 스크립트 자산 추가
  - [x] `test/scripts/analyze-stability.sh`
  - [x] `test/scripts/live-smoke.sh`
  - [x] `test/scripts/migration-roundtrip.sh`
  - [x] `test/scripts/validate-migrations.sh`
- [x] Java Maven 전용 테스트 프로필 추가 (`src/test/resources/application-test.properties`)
- [x] CI 실행용 H2 프로필 추가 (`src/main/resources/application-ci.properties`)
- [x] Swagger/OpenAPI 의존성 추가 (`springdoc-openapi-starter-webmvc-ui`)
- [x] Swagger UI 경로 `/docs/swagger` 구성
- [x] OpenAPI 경로 `/docs/openapi` 구성
- [x] Swagger/OpenAPI 경로에 대한 보안 예외 추가
- [x] Java Maven GitHub Actions 워크플로 추가 (`.github/workflows/java-maven-ci.yml`)
- [x] 자동 실행 `quality` 잡 추가
- [x] 자동 실행 `platform-e2e` 잡 추가
- [x] 자동 실행 `swagger-export` 잡 추가
- [x] 수동 실행 `release-gate` 잡 추가
- [x] 수동 실행 `migration-validation` 잡 추가
- [x] 수동 실행 `contract-e2e` 잡 추가
- [x] 수동 실행 `live-smoke` 잡 추가
- [x] Swagger export 시 redirect 대응(`curl -L`) 반영
- [x] 최소 집중 검증 실행
  - [x] `FlywayMigrationTest`
  - [x] `PublicApiE2ETest`
  - [x] `SwaggerDocsE2ETest`
  - [x] `AuthSearchE2ETest`
  - [x] `ResilienceAutoTuneE2ETest`
  - [x] `SecurityRegressionE2ETest`
  - [x] `MigrationRoundtripScriptTest`
  - [x] `ValidateMigrationsScriptTest`

## 📌 메모
- Java Maven 백엔드에 문서 기준 운영/플랫폼 E2E 축이 추가되었다.
- Java Maven 백엔드에 남아 있던 운영/보안/회복성/스크립트/성능 자산 축이 추가되었다.
- Java Maven 전용 CI 워크플로가 생겨 자동 검증과 수동 릴리즈 게이트를 분리해 운영할 수 있게 되었다.
- `/docs/openapi`, `/docs/swagger` 경로가 실제로 동작하고, CI에서 OpenAPI/Swagger 산출물을 아티팩트로 수집할 수 있게 되었다.


