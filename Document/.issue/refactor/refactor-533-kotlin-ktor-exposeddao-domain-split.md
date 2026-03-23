---
name: "♻️ Refactor Request"
about: 기능 수정 제안
title: "[REFACT] Kotlin Ktor Exposed DAO Domain Split"
labels: refactor
assignees: ""
issue: "[REFACT] Kotlin Ktor Exposed DAO Domain Split 수정"
commit: "refactor: (#533) Kotlin Ktor Exposed DAO Domain Split 수정"
branch: "refactor/#533/kotlin-ktor-exposeddao-domain-split"
---

## ♻️ 수정 요약

> Kotlin 구현체 안에서 하나로 묶여 있는 기능 코드를 도메인 기준으로 분리하고 언어 간 표준 구조에 맞춰 정리하는 작업입니다.

Kotlin 구현체 안에서 하나로 묶여 있는 기능 코드를 도메인 기준으로 분리한다. 공개 API 계약은 유지한 채, 도메인별 `controller/service/repository` 구조가 더 분명하게 보이도록 정리한다. 문서 메타, 공통 계약, 도메인 스텁 책임이 뒤섞인 부분을 언어 간 표준 구조에 맞게 재배치한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin 구현체에서 기능이 과도하게 한 파일 또는 한 계층에 묶여 있는 구간을 도메인 기준으로 분리한다.
- [x] 도메인별 `controller/service/repository` 흐름이 드러나도록 Kotlin 패키지 구조를 정리한다.
- [x] OpenAPI 및 공통 계약 메타는 문서/공통 책임으로 분리하고, 공개 API 경로와 응답 형식은 유지한다.
- [x] 주요 Kotlin 라우트와 문서 노출 동작이 회귀되지 않도록 테스트로 검증한다.
