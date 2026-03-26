---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Activity Chat Push API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Activity Chat Push API 구현"
commit: "feat: (#555) Kotlin Ktor Exposed DAO Activity Chat Push API 구현"
branch: "feat/#555/kotlin-ktor-exposeddao-activity-chat-push-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 Activity, Chat, Push API를 다음 실제 구현 도메인으로 추가하고, stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin Activity 도메인에 최근 본 상품 목록/전체 삭제, 검색 기록 목록/전체 삭제/개별 삭제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Chat 도메인에 채팅방 생성, 채팅방 목록, 메시지 기록 조회, 채팅방 종료 엔드포인트를 실제 controller 기반으로 구현하고, 방 멤버 및 관리자 권한 검증을 반영한다.
- [x] Kotlin Push 도메인에 푸시 구독 등록/비활성화/목록 조회, 푸시 설정 조회/저장 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Activity, Chat, Push service에 사용자 식별, 입력 검증, 페이지네이션, 소유권 검사, 채팅방 종료 상태 처리, 구독 upsert 및 환경설정 갱신 흐름을 추가한다.
- [x] Activity, Chat, Push repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application 및 Routing wiring을 확장하고, OpenAPI 메타데이터에서 이번 단계 범위 밖인 chat 보조 stub 경로와 `POST /admin/push/send`를 정리한다.
- [x] Kotlin 테스트에서 activity, chat, push 실제 계약 및 권한 회귀 시나리오를 검증한다.
- [x] `.\gradlew.bat compileKotlin`, `.\gradlew.bat test --tests "com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ActivityChatPushApiTest" --rerun-tasks --console plain`, `.\gradlew.bat test --console plain`을 통과한다.
