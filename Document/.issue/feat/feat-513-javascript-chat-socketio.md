---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Chat Socket.IO 구현"
labels: feature
issue: "[FEAT] JavaScript Chat Socket.IO 구현"
commit: "feat: (#513) JavaScript Chat Socket.IO 이벤트 및 E2E 검증 추가"
branch: "feat/#513/javascript-chat-socketio"
assignees: ""
---

## ✨ 기능 요약

> `BackEnd/JavaScript/expressshop_prismaorm`에 JWT 인증 기반 `Socket.IO` 채팅 이벤트를 추가하고, critical E2E에서 실시간 이벤트 흐름을 검증했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Chat Socket.IO 서버 추가 (`src/chat/chat.socket.js`)
- [x] JWT 기반 소켓 연결 인증 처리
- [x] 채팅방 접근 권한 확인 로직 연결
- [x] Socket.IO 이벤트 구현
- [x] `joinRoom`
- [x] `leaveRoom`
- [x] `sendMessage`
- [x] `messageRead`
- [x] `typing`
- [x] 서버 브로드캐스트 이벤트 구현
- [x] `newMessage`
- [x] `readReceipt`
- [x] `userTyping`
- [x] HTTP 서버에 Socket.IO 연결 (`src/server.js`)
- [x] 테스트 하네스가 Socket.IO 서버까지 함께 기동하도록 확장 (`test/e2e/_support/harness.js`)
- [x] Socket.IO 클라이언트 의존성 추가 (`package.json`, `package-lock.json`)
- [x] critical E2E에 실시간 채팅 검증 추가 (`test/e2e/critical.test.js`)
- [x] 검증 항목 포함
- [x] JWT 인증 소켓 연결 확인
- [x] 채팅방 입장 확인
- [x] 실시간 메시지 송수신 확인
- [x] 타이핑 이벤트 확인
- [x] 읽음 알림 이벤트 확인
- [x] 채팅방 퇴장 확인
- [x] critical E2E 통과 (`node --env-file=.env --test test/e2e/critical.test.js`)
