---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 실시간 채팅 모듈 구현"
labels: feature
issue: "[FEAT] 실시간 채팅 모듈 구현"
commit: "feat: (#37) 채팅방/메시지 API 및 WebSocket 게이트웨이 구현"
branch: "feat/#37/chat-module"
assignees: ""
---

## ✨ 기능 요약

> 채팅방 생성/조회, 채팅방 입장, 메시지 조회/전송 API와 WebSocket 실시간 메시지 브로드캐스트를 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 채팅방 엔티티 구현 (`chat_rooms`)
- [x] 채팅방 멤버 엔티티 구현 (`chat_room_members`)
- [x] 채팅 메시지 엔티티 구현 (`chat_messages`)
- [x] 채팅 DTO 구현 (방 생성/메시지 전송/WS payload)
- [x] 채팅 모듈/서비스/컨트롤러 추가
- [x] 내 채팅방 목록 API 구현 (`GET /chat/rooms`)
- [x] 채팅방 생성 API 구현 (`POST /chat/rooms`)
- [x] 채팅방 입장 API 구현 (`POST /chat/rooms/:id/join`)
- [x] 메시지 목록 조회 API 구현 (`GET /chat/rooms/:id/messages`)
- [x] 메시지 전송 API 구현 (`POST /chat/rooms/:id/messages`)
- [x] WebSocket 게이트웨이 구현 (`/chat`, joinRoom/sendMessage/newMessage)
- [x] WebSocket JWT 가드 연동 (`WsAuthGuard`)
- [x] 앱 모듈 등록 (`ChatModule`)
- [x] API 라우트 상수 보강 (`CHAT.JOIN_ROOM`, `CHAT.MESSAGES`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
