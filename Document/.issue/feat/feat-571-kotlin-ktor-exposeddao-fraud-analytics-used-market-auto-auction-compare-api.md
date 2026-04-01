---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Fraud Analytics Used Market Auto Auction Compare API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Fraud Analytics Used Market Auto Auction Compare API 구현"
commit: "feat: (#571) Kotlin Ktor Exposed DAO Fraud Analytics Used Market Auto Auction Compare API 구현"
branch: "feat/#571/kotlin-ktor-exposeddao-fraud-analytics-used-market-auto-auction-compare-api"
---

## ✨ 기능 요약

Kotlin 기준 구현체에 Fraud, Analytics, Used Market, Auto, Auction, Compare API를 다음 실제 구현 도메인으로 추가하고, 현재 stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

- [x] Kotlin Fraud 도메인에 이상 가격 알림 목록, 승인, 거절, 상품 실제 구매가 조회 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Analytics 도메인에 상품 최저가 이력과 가격 분석 관련 공개 조회 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Used Market 도메인에 상품별 중고 시세, 카테고리별 중고 시세, PC 견적 기반 중고 매입가 산정 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Auto 도메인에 자동차 모델 목록, 트림 조회, 신차 견적 계산, 리스/렌트 비교 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Auction 도메인에 역경매 등록, 목록, 상세, 입찰 등록, 낙찰 선택, 입찰 수정/삭제, 경매 취소 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Compare 도메인에 비교함 추가, 제거, 현재 목록, 비교 상세 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Fraud, Analytics, Used Market, Auto, Auction, Compare service에 입력 검증, 권한 검증, 상태 전이, 가격 계산, 비교 응답 조립 흐름을 추가한다.
- [x] 6개 도메인 repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application, Routing, PbShopTestSupport wiring을 확장하고 OpenAPI 메타를 실제 계약 범위로 갱신한다.
- [x] Kotlin 테스트에서 fraud, analytics, used market, auto, auction, compare 실제 계약 회귀 시나리오를 추가하고 `.\gradlew.bat compileKotlin`, `.\gradlew.bat test`를 검증 기준으로 둔다.

## 📌 가정

- `571`은 방금 완료한 `Fraud / Analytics / Used Market / Auto / Auction / Compare` 실제 구현 범위를 기록한다.
- 다음 배치는 `Fraud + Analytics + Used Market + Auto + Auction + Compare` 6개 도메인으로 고정한다.
- 공개 계약은 `02_api-specification.md` 우선으로 잡고, 문서에 없는 보조 경로는 이번 이슈 범위에 포함하지 않는다.
- 운영 계열인 `Admin Settings / Health / Resilience / Queue Admin / Ops Dashboard / Observability`는 다음 별도 이슈로 분리한다.
