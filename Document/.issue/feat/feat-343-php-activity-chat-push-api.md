---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Activity Chat Push API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Activity Chat Push API 구현"
commit: "feat: (#343) PHP Activity Chat Push API 구현"
branch: "feat/#343/php-activity-chat-push-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 활동 내역, 채팅, 푸시 알림 API를 구현한다.

## 📋 요구사항

- [x] 활동/채팅/푸시 테이블 추가
  - [x] `recent_product_views` migration 추가
  - [x] `search_histories` migration 추가
  - [x] `chat_rooms` migration 추가
  - [x] `chat_room_members` migration 추가
  - [x] `chat_messages` migration 추가
  - [x] `push_subscriptions` migration 추가
  - [x] `push_preferences` migration 추가
- [x] 도메인 모델 추가
  - [x] `RecentProductView`
  - [x] `SearchHistory`
  - [x] `ChatRoom`
  - [x] `ChatRoomMember`
  - [x] `ChatMessage`
  - [x] `PushSubscription`
  - [x] `PushPreference`
- [x] Activity 요청 검증/서비스/컨트롤러 추가
  - [x] `CreateSearchHistoryRequest`
  - [x] `ActivityService`
  - [x] `ActivityController`
- [x] Activity API 구현
  - [x] `GET /api/v1/activities`
  - [x] `GET /api/v1/activities/recent-products`
  - [x] `POST /api/v1/activities/recent-products/{productId}`
  - [x] `GET /api/v1/activities/searches`
  - [x] `POST /api/v1/activities/searches`
  - [x] `DELETE /api/v1/activities/searches/{id}`
  - [x] `DELETE /api/v1/activities/searches`
  - [x] 최근 본 상품 upsert 처리
  - [x] 활동 요약 응답 제공
- [x] Chat 요청 검증/서비스/컨트롤러 추가
  - [x] `CreateChatRoomRequest`
  - [x] `SendChatMessageRequest`
  - [x] `ChatService`
  - [x] `ChatController`
- [x] Chat API 구현
  - [x] `GET /api/v1/chat/rooms`
  - [x] `POST /api/v1/chat/rooms`
  - [x] `POST /api/v1/chat/rooms/{id}/join`
  - [x] `GET /api/v1/chat/rooms/{id}/messages`
  - [x] `POST /api/v1/chat/rooms/{id}/messages`
  - [x] 채팅방 멤버십 검증
  - [x] 메시지 전송 시 `last_message_at` 갱신
- [x] Push 요청 검증/서비스/컨트롤러 추가
  - [x] `RegisterPushSubscriptionRequest`
  - [x] `UnregisterPushSubscriptionRequest`
  - [x] `UpdatePushPreferenceRequest`
  - [x] `PushService`
  - [x] `PushController`
- [x] Push API 구현
  - [x] `POST /api/v1/push/subscriptions`
  - [x] `POST /api/v1/push/subscriptions/unsubscribe`
  - [x] `GET /api/v1/push/subscriptions`
  - [x] `GET /api/v1/push/preferences`
  - [x] `POST /api/v1/push/preferences`
  - [x] 구독 upsert 처리
  - [x] 사용자 푸시 설정 기본값 자동 생성
- [x] 라우트 파일 분리 및 등록
  - [x] `routes/api_v1/activity.php`
  - [x] `routes/api_v1/chat.php`
  - [x] `routes/api_v1/push.php`
  - [x] `routes/api_v1.php`에 loader 연결
- [x] 통합 테스트 추가
  - [x] `tests/Feature/Api/ActivityChatPushApiTest.php` 추가
  - [x] 활동 내역 기록/조회/삭제 검증
  - [x] 채팅방 생성/입장/메시지 송수신 검증
  - [x] 푸시 구독 등록/해제/설정 변경 검증
  - [x] `php artisan test tests/Feature/Api/ActivityChatPushApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list --path=api/v1/activities` 통과
  - [x] `php artisan route:list --path=api/v1/chat` 통과
  - [x] `php artisan route:list --path=api/v1/push` 통과
