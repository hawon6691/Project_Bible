---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Review Wishlist Point API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Review Wishlist Point API 구현"
commit: "feat: (#551) Kotlin Ktor Exposed DAO Review Wishlist Point API 구현"
branch: "feat/#551/kotlin-ktor-exposeddao-review-wishlist-point-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 Review, Wishlist, Point API를 다음 실제 구현 도메인으로 추가하고, stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin Review 도메인에 상품 리뷰 목록, 리뷰 작성, 수정, 삭제 엔드포인트를 실제 controller 기반으로 구현하고 주문-상품 검증 및 중복 리뷰 방지 흐름을 반영한다.
- [x] Kotlin Wishlist 도메인에 위시리스트 목록, 상품 찜 토글, 찜 해제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Point 도메인에 포인트 잔액, 포인트 내역, 관리자 포인트 지급 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Review, Wishlist, Point service에 사용자 식별, 입력 검증, 포인트 적립/지급, 위시리스트 토글, 페이지네이션, 권한 처리 흐름을 추가한다.
- [x] Review, Wishlist, Point repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application 및 Routing wiring을 확장하고, Kotlin 테스트에서 review, wishlist, point 회귀 시나리오를 검증한다.
- [x] `.\gradlew.bat test`를 통과한다.
