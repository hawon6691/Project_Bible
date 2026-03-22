---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO DB Connection and PostgreSQL Bootstrap"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO DB Connection and PostgreSQL Bootstrap 구현"
commit: "feat: (#529) Kotlin Ktor Exposed DAO DB Connection and PostgreSQL Bootstrap 구현"
branch: "feat/#529/kotlin-ktor-exposeddao-db-connection-and-postgresql-bootstrap"
---

## ✨ 기능 요약

> Kotlin 기준 구현체에서 공통 PostgreSQL SQL 자산 기반 DB 연결, bootstrap, seed, smoke 검증 흐름을 확정한다.

## 📋 요구사항

- [x] `build.gradle.kts`에 PostgreSQL JDBC, HikariCP, Exposed `core`, `jdbc`, `dao`, `java-time` 의존성을 추가한다.
- [x] Kotlin 설정에 PostgreSQL 기본 연결값과 `engine`, `database`, 풀 설정을 추가한다.
- [x] `DatabaseFactory`를 추가해 HikariCP와 Exposed `Database.connect` 초기화 흐름을 고정한다.
- [x] `DbHealthService`를 추가해 DB 연결 확인과 health 메타 조회를 제공한다.
- [x] `dbBootstrap`, `dbSeed`, `dbInit`, `dbSmoke` Gradle 작업을 추가하고 공통 SQL 자산 `setting.sql`, `postgres_table.sql`, `sample_data.sql` 적용 흐름을 연결한다.
- [x] bootstrap 재실행 가능성을 위해 `postgres_table.sql` 적용 전 `public` 스키마 초기화 흐름을 추가한다.
- [x] `/health`, `/api/v1/health` 응답에 `checks.db`를 추가하고 DB 성공 시 `200`, 실패 시 `503`을 반환하도록 정리한다.
- [x] Kotlin 테스트에서 DB health `UP/DOWN` 분기와 baseline 메타데이터를 검증한다.
- [x] `.\gradlew.bat test`, `.\gradlew.bat dbInit`, `.\gradlew.bat dbSmoke`로 Kotlin DB 연결 단계 검증을 완료한다.
