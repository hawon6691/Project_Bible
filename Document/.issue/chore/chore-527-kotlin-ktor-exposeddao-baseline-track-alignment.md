---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] Kotlin Ktor Exposed DAO Baseline Track Alignment"
labels: chore
assignees: ""
issue: "[CHORE] Kotlin Ktor Exposed DAO Baseline Track Alignment 구현"
commit: "chore: (#527) Kotlin Ktor Exposed DAO Baseline Track Alignment 구현"
branch: "chore/#527/kotlin-ktor-exposeddao-baseline-track-alignment"
---

## 🛠️ 작업 요약

> Kotlin 1차 기준 구현체를 단일 baseline 트랙으로 고정하고 운영 기본값을 공통 규칙에 맞춰 정렬하는 작업입니다.

Kotlin 1차 기준 구현체를 `kotlin-ktor-gradle-exposeddao-postgresql` 단일 트랙으로 고정하고, Kotlin 트랙의 운영 기본값과 기준 경로를 공통 규칙에 맞춰 정렬한다. 이후 Kotlin API, 테스트, CI, 문서 작업은 이 기준 트랙 하나를 기준으로 이어지게 만든다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

- Kotlin은 아직 다중 트랙 병행 단계가 아니라 기준 구현체 고정이 먼저 필요했다.
- 다른 완료 트랙과 달리 Kotlin은 baseline 선언이 약해서 후속 DB/API/테스트/CI 작업 기준이 흔들릴 수 있었다.
- 공통 포트, health 경로, docs 경로, baseline 식별자를 먼저 고정해야 다음 단계가 일관되게 진행된다.

## 📋 요구사항

> 수행해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin 기준 구현체를 `BackEnd/Kotlin/kotlin-ktor-gradle-exposeddao-postgresql`로 고정한다.
- [x] Kotlin 루트 README에 단일 기준 트랙, 공통 기준 자산, 개발 순서를 명시한다.
- [x] 구현체 README에 baseline 결정, 운영 기본값, 후속 트랙 착수 조건을 명시한다.
- [x] Ktor 설정에서 기본 포트를 `8000`으로 정렬하고 `docsPath`, `baselineTrack` 설정을 추가한다.
- [x] 루트, `/health`, `/api/v1/health`, `/api/v1/docs-status` 응답에 baseline 메타데이터와 정렬된 docs 경로 정보를 반영한다.
- [x] Kotlin 테스트에서 baseline 메타데이터와 docs 경로 노출을 검증한다.
