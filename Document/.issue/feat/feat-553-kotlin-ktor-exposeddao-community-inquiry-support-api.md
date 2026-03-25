---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Community Inquiry Support API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Community Inquiry Support API 구현"
commit: "feat: (#553) Kotlin Ktor Exposed DAO Community Inquiry Support API 구현"
branch: "feat/#553/kotlin-ktor-exposeddao-community-inquiry-support-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 Community, Inquiry, Support API를 다음 실제 구현 도메인으로 추가하고, stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin Community 도메인에 게시판 목록, 게시글 목록/상세, 게시글 작성/수정/삭제, 좋아요 토글, 댓글 목록/작성/삭제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Inquiry 도메인에 상품 문의 목록, 문의 작성, 내 문의 목록, 문의 답변, 문의 삭제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Support 도메인에 내 문의 목록, 1:1 문의 생성, 문의 상세, 답글 작성, 관리자 문의 목록/상태 변경, FAQ CRUD, 공지사항 CRUD 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Community, Inquiry, Support service에 사용자 식별, 권한 검증, 입력 검증, 소유권 검사, 페이지네이션, 상태 변경 및 스레드 처리 흐름을 추가한다.
- [x] Community, Inquiry, Support repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application 및 Routing wiring을 확장하고, Kotlin 테스트에서 community, inquiry, support 회귀 시나리오를 검증한다.
- [x] `.\gradlew.bat compileKotlin`과 `.\gradlew.bat test`를 통과하고, `CommunityInquirySupportApiTest` 재실행으로 새 실제 계약 시나리오를 검증한다.
