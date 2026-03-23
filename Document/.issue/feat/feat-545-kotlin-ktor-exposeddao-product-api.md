---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Product API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Product API 구현"
commit: "feat: (#545) Kotlin Ktor Exposed DAO Product API 구현"
branch: "feat/#545/kotlin-ktor-exposeddao-product-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 Product API를 네 번째 실제 구현 도메인으로 추가하고, stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin Product 도메인에 상품 목록 조회, 상세 조회, 관리자 생성, 수정, 삭제, 옵션 CRUD, 이미지 추가 및 삭제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Product service에 목록 필터링, 정렬, specs 파라미터 해석, 상세 응답 조립, 입력 검증, 옵션 및 이미지 처리 흐름을 추가한다.
- [x] Product repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] Product 관련 Exposed 매핑을 확장하고, Application 및 Routing wiring에서 Product repository 주입 구조를 반영한다.
- [x] Product OpenAPI 메타데이터를 현재 엔드포인트 계약에 맞게 갱신하고, Kotlin 테스트에서 목록, 상세, 관리자 상품/옵션/이미지 회귀 시나리오를 검증한다.
- [x] `.\gradlew.bat test`를 통과한다.
