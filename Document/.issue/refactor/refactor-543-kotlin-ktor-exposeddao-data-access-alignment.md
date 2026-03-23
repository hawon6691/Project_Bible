---
name: "♻️ Refactor Request"
about: 기능 수정 제안
title: "[REFACT] Kotlin Ktor Exposed DAO Data Access Alignment"
labels: refactor
assignees: ""
issue: "[REFACT] Kotlin Ktor Exposed DAO Data Access Alignment 수정"
commit: "refactor: (#543) Kotlin Ktor Exposed DAO Data Access Alignment 수정"
branch: "refactor/#543/kotlin-ktor-exposeddao-data-access-alignment"
---

## ♻️ 수정 요약

> Kotlin 기준 구현체의 트랙 이름과 실제 데이터 접근 구현을 맞추기 위해 `Auth`, `User`, `Category` 런타임 저장소를 `JDBC`에서 `Exposed DAO/DSL` 기준으로 정렬하는 작업입니다.

Kotlin 기준 구현체의 트랙 이름과 실제 데이터 접근 구현을 맞추기 위해 `Auth`, `User`, `Category` 런타임 저장소를 `JDBC`에서 `Exposed DAO/DSL` 기준으로 정렬한다. 공용 Exposed 매핑 계층과 transaction 진입점을 추가하고, 기본 runtime wiring을 새 `ExposedDao*Repository`로 교체한다. 공개 API 경로, 요청/응답 형식, 권한 규칙, 테스트 기본 흐름은 유지한 채 내부 데이터 접근 방식만 정리한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin `kotlin-ktor-gradle-exposeddao-postgresql` 트랙의 실구현 런타임 저장소 기준을 `JDBC`에서 `Exposed DAO/DSL`로 정렬한다.
- [x] `Auth`, `User`, `Category` 도메인에 `ExposedDaoAuthRepository`, `ExposedDaoUserRepository`, `ExposedDaoCategoryRepository`를 추가하고 기존 `Jdbc*Repository`를 제거한다.
- [x] `users`, `email_verifications`, `categories`, `products`, `badges`, `user_badges`를 위한 공용 Exposed schema/entity 계층과 PostgreSQL enum 매핑을 추가한다.
- [x] `DatabaseFactory`에 Exposed `Database` 재사용과 transaction helper를 추가하고, 기본 runtime wiring을 새 Exposed repository 구현으로 교체한다.
- [x] `DbHealthService`와 `DbCli` 같은 인프라용 직접 SQL은 유지하고, 이번 단계 범위는 `Auth`, `User`, `Category` 및 해당 wiring으로 제한한다.
- [x] Kotlin 테스트에서 기존 auth, user, category 회귀 시나리오와 전체 API 계약이 유지되도록 검증하고 `.\gradlew.bat test`를 통과한다.
