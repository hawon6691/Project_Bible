---
name: "♻️ Refactor Request"
about: 기능 수정 제안
title: "[REFACT] Kotlin Ktor Exposed DAO MVC File Split"
labels: refactor
assignees: ""
issue: "[REFACT] Kotlin Ktor Exposed DAO MVC File Split 수정"
commit: "refactor: (#535) Kotlin Ktor Exposed DAO MVC File Split 수정"
branch: "refactor/#535/kotlin-ktor-exposeddao-mvc-file-split"
---

## ♻️ 수정 요약

> Kotlin 구현체에서 feature 패키지는 유지하되, `XxxMvc.kt` 안에 같이 있던 `Controller`, `Service`, `Repository`를 파일 단위로 분리하고 언어 간 표준 구조에 맞춰 정리하는 작업입니다.

Kotlin 구현체에서 feature 패키지는 유지하되, `XxxMvc.kt` 안에 같이 있던 `Controller`, `Service`, `Repository`를 파일 단위로 분리한다. 다른 언어들과 정렬되도록 같은 feature 패키지 안에서 `XxxController.kt`, `XxxService.kt`, `XxxRepository.kt`, `XxxStubOperations.kt` 구조를 고정한다. 공개 API 계약과 라우팅 동작은 유지한 채 내부 구조만 정리한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin feature 패키지에서 `XxxMvc.kt`를 제거하고 `XxxController.kt`, `XxxService.kt`, `XxxRepository.kt`로 파일을 분리한다.
- [x] `platform`, `health`, `docs`도 동일한 원칙으로 controller/service 파일을 분리한다.
- [x] `Routing.kt`와 문서 registry wiring은 새 파일 구조에 맞게 정리하고 공개 API 경로와 응답 형식은 유지한다.
- [x] Kotlin 테스트에서 주요 API, health, docs, 인가 동작이 회귀되지 않도록 검증한다.
