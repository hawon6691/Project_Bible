---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript 검증/CI 완료선 정리"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript 검증/CI 완료선 정리"
commit: "feat: (#515) JavaScript 검증 자산 및 CI 게이트 확장"
branch: "feat/#515/javascript-validation-ci-completion"
---

## ✨ 기능 요약

> JavaScript `javascript-express-npm-prisma-postgresql` 트랙의 검증 자산, CI 게이트, 상태 문서, README/OpenAPI 보강을 완료선 기준으로 정리합니다.

JavaScript 트랙의 릴리즈 준비도를 높이기 위한 검증/CI 확장 작업을 반영한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `package.json`에 `quality`, `contract-doc`, `perf-smoke`, `release-gate` 등 검증 명령을 추가한다.
- [x] GitHub Actions에 자동 게이트 `quality`, `critical-e2e`, `contract-doc`, `perf-smoke`를 반영한다.
- [x] GitHub Actions에 수동 게이트 `migration-validation-manual`, `migration-roundtrip-manual`, `security-regression-manual`, `admin-boundary-manual`, `release-gate`를 반영한다.
- [x] `AuthSearchE2E`, `QueueAdminE2E`, `ObservabilityE2E`, `RateLimitRegressionE2E`와 도메인 통합 테스트를 추가한다.
- [x] 성능 자산 `smoke`, `search-ranking`과 스크립트 자산 `validate-migrations`, `migration-roundtrip`, `live-smoke`, `analyze-stability`, `release-gate`를 추가한다.
- [x] `Document/JavaScript` 상태 문서 3종과 `README`, OpenAPI 설명을 최신 구현 기준으로 보강한다.
- [x] `npm run test:quality`, `test:e2e:platform`, `test:api:domain`, `test:contract:doc`, `test:script:validate-migrations`, `test:script:migration-roundtrip`, `test:perf:smoke`, `test:script:analyze-stability`, `test:release:gate` 로컬 검증을 통과한다.
