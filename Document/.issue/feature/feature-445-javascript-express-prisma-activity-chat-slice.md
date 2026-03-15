---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Activity Chat Slice"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Activity Chat Slice 문서 작성"
commit: "feat: (#445) JavaScript Express Prisma 활동·채팅 API 구현"
branch: "feature/#445/javascript-express-prisma-activity-chat-slice"
---

## ✨ 기능 요약

> JavaScript Express Prisma 구현체에 활동 이력과 채팅 API를 추가하고, 구조 정리 과정에서 남은 레거시 폴더를 제거한다.

## 📋 요구사항

- [x] Prisma schema에 `RecentProductView` 모델 추가
- [x] Prisma schema에 `SearchHistory` 모델 추가
- [x] Prisma schema에 `ChatRoom` 모델 추가
- [x] Prisma schema에 `ChatMessage` 모델 추가
- [x] Prisma schema에 `ChatRoomMember` 모델 추가
- [x] 기존 `User`, `Product` 관계에 activity/chat relation 보강
- [x] `activity` feature 폴더 추가
- [x] `chat` feature 폴더 추가
- [x] `routes/activities.js` 추가
- [x] `routes/chats.js` 추가
- [x] `GET /api/v1/activity/views` 구현
- [x] `DELETE /api/v1/activity/views` 구현
- [x] `GET /api/v1/activity/searches` 구현
- [x] `DELETE /api/v1/activity/searches` 구현
- [x] `DELETE /api/v1/activity/searches/:id` 구현
- [x] `POST /api/v1/chat/rooms` 구현
- [x] `GET /api/v1/chat/rooms` 구현
- [x] `GET /api/v1/chat/rooms/:id/messages` 구현
- [x] `PATCH /api/v1/chat/rooms/:id/close` 구현
- [x] Prisma client 재생성
- [x] `src/commerce`, `src/community`, `src/engagement`, `src/support` 레거시 폴더 제거
- [x] 샘플 데이터 기준 activity/chat 주요 API 응답 검증

## ✅ 산출물

- `BackEnd/JavaScript/expressshop_prismaorm/prisma/schema.prisma`
- `BackEnd/JavaScript/expressshop_prismaorm/src/activity/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/chat/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/activities.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/chats.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/index.js`

## 검증 메모

- `GET /api/v1/activity/views` 성공
- `GET /api/v1/activity/searches` 성공
- `GET /api/v1/chat/rooms` 성공
- `POST /api/v1/chat/rooms` 성공
- `GET /api/v1/chat/rooms/1/messages` 성공
- `PATCH /api/v1/chat/rooms/1/close` 성공
- `/health` 정상 응답 확인
- Prisma schema 변경 후 `npm run prisma:generate` 재실행 완료

## 메모

- 공통 SQL에는 activity/chat 관련 테이블 정의가 일부 이중으로 존재하므로, 이번 구현은 샘플 데이터와 호환되는 `recent_product_views`, `search_histories`, `chat_rooms`, `chat_messages`, `chat_room_members` 기준으로 맞췄다.
- `chat room close`는 공통 SQL에 별도 종료 상태 컬럼이 없어 현재는 접근 권한 검증 후 `updatedAt` 갱신 방식으로 처리한다.
- 다음 단계는 체크리스트 순서대로 `ranking`, `recommendation`, `deal` 슬라이스로 이어가는 것이 자연스럽다.
