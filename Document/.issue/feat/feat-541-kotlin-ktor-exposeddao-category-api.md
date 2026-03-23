---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Category API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Category API 구현"
commit: "feat: (#541) Kotlin Ktor Exposed DAO Category API 구현"
branch: "feat/#541/kotlin-ktor-exposeddao-category-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 Category API를 세 번째 실제 구현 도메인으로 추가하고, stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin Category 도메인에 카테고리 트리 조회, 단일 조회, 관리자 생성, 수정, 삭제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Category service에 트리 조립, 단건 응답 조립, 입력 검증, 하위 카테고리 및 연결 상품 삭제 가드 흐름을 추가한다.
- [x] Category repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL JDBC 구현과 테스트용 in-memory 구현을 분리한다.
- [x] Application 및 Routing wiring에서 Category repository 주입 구조를 반영하고, 테스트 환경에서는 in-memory category repository를 기본 사용하도록 정리한다.
- [x] Category OpenAPI 메타데이터를 현재 엔드포인트 계약에 맞게 갱신하고, Kotlin 테스트에서 목록, 단건, 생성, 수정, 삭제 회귀 시나리오를 검증한다.
- [x] `.\gradlew.bat test`를 통과한다.
