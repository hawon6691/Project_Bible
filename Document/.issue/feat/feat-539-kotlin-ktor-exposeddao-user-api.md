---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO User API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO User API 구현"
commit: "feat: (#539) Kotlin Ktor Exposed DAO User API 구현"
branch: "feat/#539/kotlin-ktor-exposeddao-user-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 User API를 두 번째 실제 구현 도메인으로 추가하고, stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin User 도메인에 내 정보 조회/수정/탈퇴, 관리자 회원 목록/상태 변경, 공개 프로필 조회, 프로필 수정, 프로필 이미지 변경/삭제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] User service에 현재 사용자 해석, 사용자 정보 응답 조립, 목록 필터링, 프로필 수정, 상태 변경, 회원 탈퇴 흐름을 추가한다.
- [x] User repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL JDBC 구현과 테스트용 in-memory 구현을 분리한다.
- [x] Application 및 Routing wiring에서 User repository 주입 구조를 반영하고, 테스트 환경에서는 in-memory user repository를 기본 사용하도록 정리한다.
- [x] User OpenAPI 메타데이터를 현재 엔드포인트 계약에 맞게 갱신하고, Kotlin 테스트에서 current user, profile, admin list 회귀 시나리오를 검증한다.
- [x] `.\gradlew.bat test`를 통과한다.
