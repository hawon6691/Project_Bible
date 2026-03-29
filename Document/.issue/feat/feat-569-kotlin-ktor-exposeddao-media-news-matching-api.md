---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Media News Matching API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Media News Matching API 구현"
commit: "feat: (#569) Kotlin Ktor Exposed DAO Media News Matching API 구현"
branch: "feat/#569/kotlin-ktor-exposeddao-media-news-matching-api"
---

## ✨ 기능 요약

Kotlin 기준 구현체에 Media, News, Matching API를 다음 실제 구현 도메인으로 추가하고, 현재 stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

- [x] Kotlin Media 도메인에 `upload`, `presigned-url`, `stream`, `metadata`, `delete` 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin News 도메인에 카테고리 목록, 뉴스 목록/상세, 관리자 뉴스 생성/수정/삭제, 카테고리 생성/삭제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Matching 도메인에 `pending`, `approve`, `reject`, `auto-match`, `stats` 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Media, News, Matching service에 입력 검증, 관리자 권한 검증, 관련 상품 검증, view count 증가, auto-match 및 stats 집계 흐름을 추가한다.
- [x] Media, News, Matching repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application, Routing, PbShopTestSupport wiring을 확장하고 OpenAPI 메타를 실제 계약 범위에 맞게 갱신한다.
- [x] Kotlin 테스트에서 media, news, matching 실제 계약 회귀 시나리오를 검증하고 `.\gradlew.bat compileKotlin`, `.\gradlew.bat compileTestKotlin`, `.\gradlew.bat test` 통과를 기준으로 둔다.
