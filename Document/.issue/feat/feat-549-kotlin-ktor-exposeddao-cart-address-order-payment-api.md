---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Cart Address Order Payment API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Cart Address Order Payment API 구현"
commit: "feat: (#549) Kotlin Ktor Exposed DAO Cart Address Order Payment API 구현"
branch: "feat/#549/kotlin-ktor-exposeddao-cart-address-order-payment-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 Cart, Address, Order, Payment API를 다음 실제 구현 도메인으로 추가하고, stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin Cart 도메인에 장바구니 조회, 항목 추가, 수량 수정, 항목 삭제, 전체 비우기 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Address 도메인에 배송지 목록, 생성, 수정, 삭제 엔드포인트를 실제 controller 기반으로 구현하고 기본 배송지 처리 흐름을 반영한다.
- [x] Kotlin Order 도메인에 주문 생성, 내 주문 목록, 주문 상세, 주문 취소, 관리자 주문 목록, 관리자 상태 변경 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Payment 도메인에 결제 요청, 결제 상세, 환불 요청 엔드포인트를 실제 controller 기반으로 구현하고 주문 상태 연동 흐름을 반영한다.
- [x] Cart, Address, Order, Payment service에 사용자 식별, 입력 검증, 배송지/장바구니/가격 연계, 주문 금액 계산, 상태 변경 규칙을 추가한다.
- [x] Cart, Address, Order, Payment repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application 및 Routing wiring을 확장하고, Kotlin 테스트에서 cart, address, order, payment 회귀 시나리오를 검증한다.
- [x] `.\gradlew.bat test`를 통과한다.
