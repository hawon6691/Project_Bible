---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] Kotlin Ktor Exposed DAO API Docs Test CI Completion"
labels: chore
assignees: ""
issue: "[CHORE] Kotlin Ktor Exposed DAO API Docs Test CI Completion 구현"
commit: "chore: (#531) Kotlin Ktor Exposed DAO API Docs Test CI Completion 구현"
branch: "chore/#531/kotlin-ktor-exposeddao-api-docs-test-ci-completion"
---

## 🛠️ 작업 요약

> Kotlin 기준 구현체의 공통 API 명세 범위, OpenAPI/Swagger 문서, 테스트 자산, CI, Kotlin 문서 세트를 완료 수준으로 정렬하는 운영 작업입니다.

Kotlin 기준 구현체의 API, 문서, 테스트, CI, Kotlin 전용 문서 세트를 한 번에 정렬해 completion baseline을 고정한다. 이후 Kotlin 트랙은 이 정렬 상태를 기준으로 후속 보완 작업을 이어가게 만든다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

- Kotlin은 baseline 이후 API, docs, test, CI, 문서 세트를 completion 수준으로 묶어 정렬하는 단계가 필요했다.
- API, docs, test, CI, 문서 세트를 분리해서 닫으면 Kotlin 완료 기준과 추적 단위가 흔들릴 수 있다.
- 사용자 기능 추가보다 내부 구현체 완성도와 운영 일관성 정리가 중심이므로 `feat`보다 `chore`로 기록하는 것이 맞다.

## 📋 요구사항

> 수행해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin 공통 응답 계약을 `success`, `data`, `meta`, `error.code`, `error.message` 형식으로 정렬하고 예외 응답 처리, Request ID, 보안 헤더, 인메모리 rate limit, 인증/권한 유틸을 추가한다.
- [x] `/health`, `/api/v1/health`, `/api/v1/docs-status`, `/docs/openapi`, `/docs/swagger`를 포함한 Kotlin 공개 및 운영 인터페이스를 정렬하고 docs 활성 상태를 실제 산출물 기준으로 반영한다.
- [x] Kotlin 기준 구현체에 공통 명세와 완료 언어 parity 범위를 반영한 API route catalog를 추가하고 auth, user, category, product, order, community, activity, ranking, analytics, ops, query/search sync 계열 엔드포인트를 baseline 수준으로 연결한다.
- [x] Ktor 라우트와 DTO 기준 OpenAPI 산출, Swagger UI 제공, docs export Gradle task를 추가하고 chat REST 및 WebSocket 이벤트 문서를 반영한다.
- [x] Kotlin 테스트 자산을 공통 테스트 명세 이름 기준으로 확장하고 도메인 통합 테스트, 운영 및 플랫폼 E2E, docs export 검증 helper, role/auth helper, seeded fixture를 정리한다.
- [x] Kotlin 기준 CI 워크플로를 추가하고 quality, api-regression, db-integration, contract-doc, perf-smoke, 수동 gate 잡 구성을 정렬한다.
- [x] Kotlin 전용 문서 세트 `language-api-specification`, `folder-structure`, `operations-runbook`, `implementation-status`, `completion-report`, `pre-release-final-gate`, `requirements-api-gap-analysis`를 작성한다.
- [x] `.\gradlew.bat compileKotlin`, `.\gradlew.bat test`, `.\gradlew.bat docsExport` 기준으로 Kotlin completion baseline 검증을 완료한다.
