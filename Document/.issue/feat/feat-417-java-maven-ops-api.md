---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Java Maven Ops API"
labels: feature
assignees: ""
issue: "[FEAT] Java Maven Ops API 구현"
commit: "feat: (#417) Java Maven Ops API 구현"
branch: "feat/#417/java-maven-ops-api"
---

## ✨ 기능 요약

> Spring Boot Maven 백엔드에 운영 계열 API를 추가하고, query/search sync/crawler 저장소와 운영 대시보드/관측성 응답을 함께 구성한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `V14__ops_admin_query_searchsync_crawler_support.sql` 마이그레이션 추가
- [x] `system_settings` 기반 `admin settings` 조회/수정 API 추가
- [x] `error codes` 공개 조회 API 추가
- [x] `resilience` 운영 API 추가
- [x] `queue admin` 운영 API 추가
- [x] `query` 공개 조회 및 관리자 동기화 API 추가
- [x] `search sync` 요약/재큐잉 API 추가
- [x] `crawler` 작업/실행/모니터링 API 추가
- [x] `ops dashboard` 요약 API 추가
- [x] `observability` metrics/traces/dashboard API 추가
- [x] `SystemController` 헬스 응답에 `language=java` 반영
- [x] `SecurityConfig`에 공개 GET 경로 반영
- [x] `FlywayMigrationTest`에 버전 `14` 및 신규 테이블 검증 반영
- [x] `JavaOpsApiTest` 통합 테스트 추가
- [x] `cmd /c mvnw.cmd -Dtest=JavaOpsApiTest test` 통과
- [x] `cmd /c mvnw.cmd test` 전체 테스트 통과 확인
