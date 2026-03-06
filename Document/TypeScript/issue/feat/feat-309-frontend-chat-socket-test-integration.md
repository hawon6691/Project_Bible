---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Chat Socket.IO 테스트 페이지 보강"
labels: feature
issue: "[FEAT] Chat Socket.IO 테스트 페이지 보강"
commit: "feat: (#309) chat API 테스트 페이지에 socket.io 검증 추가"
branch: "feat/#309/frontend-chat-socket-test-integration"
assignees: ""
---

## ✨ 기능 요약

> 기존 Chat API 테스트 페이지가 HTTP 요청만 검증하던 상태에서, 백엔드에 이미 구현되어 있던 Socket.IO 게이트웨이까지 프론트엔드에서 직접 연결하고 테스트할 수 있도록 채팅 테스트 페이지를 보강했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 백엔드 채팅 모듈의 소켓 구현 여부 확인
  - [x] `ChatGateway` 존재 확인 (`BackEnd/TypeScript/nestshop/src/chat/chat.gateway.ts`)
  - [x] `Socket.IO` 기반(`@WebSocketGateway`, `socket.io`) 사용 확인
  - [x] `joinRoom`, `sendMessage`, `newMessage` 이벤트 구조 확인
  - [x] `WsAuthGuard`의 인증 토큰 전달 방식(`handshake.auth.token`) 확인
- [x] 프론트엔드 소켓 클라이언트 의존성 추가
  - [x] `socket.io-client` 설치 (`FrontEnd/package.json`)
- [x] Chat API 테스트 페이지 보강 (`FrontEnd/src/pages/ChatApiPage.tsx`)
  - [x] 기존 HTTP 테스트 기능 유지
    - [x] 채팅방 목록 조회
    - [x] 채팅방 생성
    - [x] 채팅방 입장
    - [x] 메시지 목록 조회
    - [x] HTTP 메시지 전송
  - [x] Socket.IO 테스트 기능 추가
    - [x] 소켓 연결/해제
    - [x] 소켓 연결 상태 표시
    - [x] `joinRoom` emit 테스트
    - [x] `sendMessage` emit 테스트
    - [x] `newMessage` 실시간 수신 로그 표시
  - [x] API base URL 기준으로 Socket namespace(`/chat`) 연결 처리
  - [x] 로그인 access token 기반 소켓 인증 연결 처리
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 채팅 모듈은 HTTP 컨트롤러와 Socket.IO 게이트웨이가 동시에 존재하므로 테스트 페이지도 두 방식을 함께 검증하도록 구성
- [x] 소켓 인증은 쿠키가 아니라 `handshake.auth.token` 기준이므로 프론트에서 로컬 저장 access token을 직접 전달하도록 반영
- [x] Socket namespace는 백엔드 `@WebSocketGateway({ namespace: '/chat' })` 설정 기준으로 연결
