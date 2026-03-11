---
name: "Java Maven Activity Chat Push API"
about: "Java Spring Boot Maven 활동/채팅/푸시 API 구현"
title: "[FEAT] Java Maven 활동/채팅/푸시 API 구현"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "activity", "chat", "push"]
assignees: []
issue: "[FEAT] Java Maven 활동/채팅/푸시 API 구현"
commit: "feat: (#403) Java Maven 활동 채팅 푸시 API 구현"
branch: "feat/#403/java-maven-activity-chat-push-api"
---

## ✨ 기능 요약
Java Maven 트랙에 활동 내역, 채팅 REST API, 푸시 구독/설정 API를 추가해 TypeScript/PHP 기준 구현 범위를 맞춘다.

## 📋 요구사항
- [x] Flyway 마이그레이션 추가
  - [x] `recent_product_views`
  - [x] `search_histories`
  - [x] `chat_rooms`
  - [x] `chat_room_members`
  - [x] `chat_messages`
  - [x] `push_subscriptions`
  - [x] `push_preferences`
- [x] Activity API 구현
  - [x] `GET /api/v1/activities`
  - [x] `GET /api/v1/activities/recent-products`
  - [x] `POST /api/v1/activities/recent-products/{productId}`
  - [x] `GET /api/v1/activities/searches`
  - [x] `POST /api/v1/activities/searches`
  - [x] `DELETE /api/v1/activities/searches/{id}`
  - [x] `DELETE /api/v1/activities/searches`
- [x] Chat API 구현
  - [x] `GET /api/v1/chat/rooms`
  - [x] `POST /api/v1/chat/rooms`
  - [x] `POST /api/v1/chat/rooms/{id}/join`
  - [x] `GET /api/v1/chat/rooms/{id}/messages`
  - [x] `POST /api/v1/chat/rooms/{id}/messages`
- [x] Push API 구현
  - [x] `POST /api/v1/push/subscriptions`
  - [x] `POST /api/v1/push/subscriptions/unsubscribe`
  - [x] `GET /api/v1/push/subscriptions`
  - [x] `GET /api/v1/push/preferences`
  - [x] `POST /api/v1/push/preferences`
- [x] Flyway migration 검증 갱신
- [x] 통합 테스트 추가
  - [x] 최근 본 상품/검색 기록 흐름
  - [x] 채팅방 생성/참여/메시지 흐름
  - [x] 푸시 구독/설정/해지 흐름
- [x] `mvnw.cmd test` 전체 통과

## 📌 참고
- 채팅은 이번 단계에서 REST API 기준으로 구현하고, 실시간 소켓 확장은 후속 단계에서 별도 검토
