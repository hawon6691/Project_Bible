---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Search Sync Chat Socket"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Search Sync Chat Socket 구현"
commit: "feat: (#563) Kotlin Ktor Exposed DAO Search Sync Chat Socket 구현"
branch: "feat/#563/kotlin-ktor-exposeddao-search-sync-chat-socket"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 Search admin sync 보조 API와 Chat websocket 최소 이벤트 셋을 추가해, 다른 완료 언어들과의 정렬 범위를 실제 controller service repository 및 socket 흐름으로 마감한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin Search 도메인에 `GET /search/admin/index/outbox/summary`, `POST /search/admin/index/outbox/requeue-failed` 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Search repository에 `search_index_outbox` 기반 summary 집계와 failed row requeue 흐름을 추가하고, 전체/단일 상품 재색인 호출 시 outbox row를 생성하도록 정리한다.
- [x] Kotlin Chat 도메인에 `/api/v1/chat/ws` websocket 경로를 추가하고 `joinRoom`, `sendMessage`, `newMessage` 최소 이벤트 셋을 실제 런타임으로 구현한다.
- [x] Chat websocket 인증은 `Authorization: Bearer <accessToken>`을 기본으로 사용하고, 테스트 및 기존 헤더 기반 흐름 호환을 위해 `X-User-Id`, `X-Role` fallback을 허용한다.
- [x] Chat service와 repository에 room membership 검증, 메시지 저장, room subscriber broadcast 흐름을 추가하고, REST chat 경로는 유지한 채 socket coordinator를 통해 room별 연결을 관리한다.
- [x] WebSocket plugin, Routing wiring, OpenAPI `x-socket-events` 메타를 현재 실제 구현 범위인 `joinRoom`, `sendMessage`, `newMessage`에 맞게 정리한다.
- [x] Kotlin 테스트에서 Search sync summary/requeue, Chat websocket 인증/join/sendMessage/newMessage 시나리오를 검증하고 `.\gradlew.bat compileKotlin`, `.\gradlew.bat compileTestKotlin`, 대상 테스트 실행을 통과한다.
- [x] 전체 테스트 리포트 기준으로 Kotlin 테스트가 `38 tests / 0 failures / 100%` 상태로 완료되도록 정리한다.
