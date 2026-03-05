---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Chat API (Step 20) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Chat API (Step 20) 프론트엔드 연동"
commit: "feat: (#269) chat API 연동 및 Chat API 테스트 페이지 추가"
branch: "feat/#269/chat-api-step"
assignees: ""
---

## ✨ 기능 요약

> Activity API(19번) 다음 단계인 Chat API(20번)를 프론트엔드에 연동하고, 수동 검증용 Chat API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Chat 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `ChatRoomItem`
  - `ChatMessageItem`
- [x] Chat 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchChatRooms` (`GET /chat/rooms`)
  - `createChatRoom` (`POST /chat/rooms`)
  - `joinChatRoom` (`POST /chat/rooms/:id/join`)
  - `fetchChatMessages` (`GET /chat/rooms/:id/messages`)
  - `sendChatMessage` (`POST /chat/rooms/:id/messages`)
- [x] Chat API 테스트 페이지 추가 (`FrontEnd/src/pages/ChatApiPage.tsx`)
  - 채팅방 목록/생성/입장
  - 메시지 목록 조회/전송
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/chat-api` 경로 추가
  - 상단 메뉴 `ChatAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세 문서에는 `PATCH /chat/rooms/:id/close`가 포함되어 있으나
- [x] 현재 서버 `chat.controller.ts`에는 해당 REST endpoint가 없음 (WebSocket 중심 종료 흐름)
- [x] 이번 프론트 단계는 서버 구현 기준으로 우선 반영
