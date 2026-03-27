---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Ranking Recommendation Deal Search Crawler Prediction Trust API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Ranking Recommendation Deal Search Crawler Prediction Trust API 구현"
commit: "feat: (#561) Kotlin Ktor Exposed DAO Ranking Recommendation Deal Search Crawler Prediction Trust API 구현"
branch: "feat/#561/kotlin-ktor-exposeddao-ranking-recommendation-deal-search-crawler-prediction-trust-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 Ranking, Recommendation, Deal, Search, Crawler, Prediction, Trust API를 다음 실제 구현 도메인으로 추가하고, stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin Ranking 도메인에 인기 상품 랭킹과 인기 검색어 집계 엔드포인트를 실제 controller 기반으로 구현하고, 조회수·주문·리뷰·검색 로그 집계를 반영한다.
- [x] Kotlin Recommendation 도메인에 오늘의 추천, 개인화 추천, 관리자 추천 목록/생성/삭제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Deal 도메인에 특가 목록, 상세, 관리자 생성/수정/삭제 엔드포인트를 실제 controller 기반으로 구현하고, 대상 상품 및 활성 기간 조합 응답을 반영한다.
- [x] Kotlin Search 도메인에 통합 검색, 자동완성, 인기 검색어, 최근 검색어 저장/조회/개별삭제/전체삭제, 검색 설정, 관리자 가중치, 인덱스 상태, 전체/단일 상품 재색인 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Crawler 도메인에 관리자 작업 목록/생성/수정/삭제, 수동 실행, 트리거, 실행 이력, 모니터링 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Prediction 도메인에 상품 가격 추세 조회 엔드포인트를 실제 controller 기반으로 구현하고, 예측 데이터 부재 시 가격 이력 기반 fallback 계산을 반영한다.
- [x] Kotlin Trust 도메인에 판매처 신뢰도 상세, 리뷰 목록, 리뷰 작성/수정/삭제 엔드포인트를 실제 controller 기반으로 구현하고, 리뷰 변경 시 신뢰도 지표 재계산을 반영한다.
- [x] Ranking, Recommendation, Deal, Search, Crawler, Prediction, Trust service에 입력 검증, 권한 검증, 집계 계산, fallback 처리, 상태 변경, 재색인 및 모니터링 흐름을 추가한다.
- [x] 7개 도메인 repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application, Routing, PbShopTestSupport wiring을 확장하고, Search outbox/admin sync 보조 API는 이번 단계 범위에서 제외하도록 OpenAPI 메타를 정리한다.
- [x] Kotlin 테스트에서 ranking, recommendation, deal, search, crawler, prediction, trust 실제 계약 회귀 시나리오를 검증하고, 검색 `q` 필수 계약에 맞춰 기존 E2E를 정렬한다.
- [x] `.\gradlew.bat compileKotlin`과 `.\gradlew.bat test`를 통과한다.
