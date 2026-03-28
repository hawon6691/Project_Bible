---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO I18n Image Badge API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO I18n Image Badge API 구현"
commit: "feat: (#565) Kotlin Ktor Exposed DAO I18n Image Badge API 구현"
branch: "feat/#565/kotlin-ktor-exposeddao-i18n-image-badge-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 I18n, Image, Badge API를 다음 실제 구현 도메인으로 추가하고, 현재 stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin I18n 도메인에 번역 조회/등록/삭제, 환율 조회/등록, 금액 환산 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Image 도메인에 이미지 업로드, legacy upload, variants 조회, 이미지 삭제 엔드포인트를 실제 controller 기반으로 구현하고 Product/User 이미지 write 흐름과 공통 자산 저장 로직을 정렬한다.
- [x] Kotlin Badge 도메인에 뱃지 목록, 내 뱃지, 사용자 뱃지 조회, 관리자 생성/수정/삭제와 grant/revoke 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] I18n, Image, Badge service에 입력 검증, 응답 조립, 관리자 권한 검증, 환율 fallback, 이미지 자산 생성, badge holder count 정렬 흐름을 추가한다.
- [x] I18n, Image, Badge repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application, Routing, PbShopTestSupport wiring을 확장하고 OpenAPI 메타를 실제 계약 범위에 맞게 갱신한다.
- [x] Kotlin 테스트에서 i18n, image, badge 실제 계약 회귀 시나리오를 추가하고 `.\gradlew.bat compileKotlin`, `.\gradlew.bat compileTestKotlin`, `.\gradlew.bat test`를 통과한다.
