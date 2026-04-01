---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] Kotlin Ktor Exposed DAO Ops Admin Actualization"
labels: chore
assignees: ""
issue: "[CHORE] Kotlin Ktor Exposed DAO Ops Admin Actualization 구현"
commit: "chore: (#573) Kotlin Ktor Exposed DAO Ops Admin Actualization 구현"
branch: "chore/#573/kotlin-ktor-exposeddao-ops-admin-actualization"
---

## 🛠️ 작업 요약

Kotlin 기준 구현체에 아직 stub로 남아 있는 Admin Settings, Observability, Queue Admin, Query, Resilience, Error Code, Ops Dashboard를 실제 controller service repository 흐름으로 전환해 운영/관리 계열 API 정렬을 마무리한다.

## 📋 요구사항

- [x] Kotlin Admin Settings 도메인에 관리자 설정 조회 및 수정 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Observability 도메인에 메트릭, 로그, 트레이스, 상태 점검 등 운영 관측 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Queue Admin 도메인에 큐 상태 조회, 재처리, 실패 작업 관리 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Query 도메인에 문서 기준 조회 보조/검색성 운영 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Resilience 도메인에 서킷 브레이커, 재시도, 장애 복구 상태 관련 운영 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Error Code 도메인에 공통 에러 코드 조회 및 운영 보조 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Ops Dashboard 도메인에 운영 대시보드 집계와 관리자 요약 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] 7개 도메인 service에 입력 검증, 관리자 권한 검증, 상태 조회 및 운영 액션 흐름을 추가한다.
- [x] 7개 도메인 repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application, Routing, PbShopTestSupport wiring을 확장하고 OpenAPI 메타를 실제 계약 범위로 갱신한다.
- [x] Kotlin 테스트에서 운영/관리 계열 실제 계약 회귀 시나리오를 추가하고 `.\gradlew.bat compileKotlin`, `.\gradlew.bat test`를 검증 기준으로 둔다.
