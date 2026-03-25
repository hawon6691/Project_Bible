---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Spec Seller Price API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Spec Seller Price API 구현"
commit: "feat: (#547) Kotlin Ktor Exposed DAO Spec Seller Price API 구현"
branch: "feat/#547/kotlin-ktor-exposeddao-spec-seller-price-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 Spec, Seller, Price API를 다음 실제 구현 도메인으로 추가하고, stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin Spec 도메인에 스펙 정의 목록, 정의 생성/수정/삭제, 상품 스펙 조회/설정, 스펙 비교, 점수 매핑 설정 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Seller 도메인에 판매처 목록, 상세, 관리자 생성/수정/삭제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Price 도메인에 상품별 가격 목록, 가격 등록/수정/삭제, 가격 이력 조회, 가격 알림 생성/목록 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Spec, Seller, Price service에 입력 검증, 비교/점수 계산, 삭제 가드, 페이지네이션, 가격 및 알림 처리 흐름을 추가한다.
- [x] Spec, Seller, Price repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application 및 Routing wiring을 확장하고, Kotlin 테스트에서 spec, seller, price 회귀 시나리오를 검증한다.
- [x] `.\gradlew.bat test`를 통과한다.
