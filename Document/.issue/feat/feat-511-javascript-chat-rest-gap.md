---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Chat REST gap 구현"
labels: feature
issue: "[FEAT] JavaScript Chat REST gap 구현"
commit: "feat: (#511) JavaScript Chat join/send REST 및 OpenAPI 반영"
branch: "feat/#511/javascript-chat-rest-gap"
assignees: ""
---

## ✨ 기능 요약

> `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql`의 채팅 REST API에 빠져 있던 채팅방 입장과 메시지 전송 엔드포인트를 추가하고, OpenAPI 문서와 critical E2E 검증에 연결했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 채팅 멤버 조회/생성 리포지토리 추가 (`src/chat/chat.repository.js`)
- [x] 채팅 메시지 생성 리포지토리 추가 (`src/chat/chat.repository.js`)
- [x] 채팅방 입장 서비스 추가 (`src/chat/chat.service.js`)
- [x] 채팅 메시지 전송 서비스 추가 (`src/chat/chat.service.js`)
- [x] 채팅 컨트롤러 확장 (`src/chat/chat.controller.js`)
- [x] 메시지 전송 요청 검증 추가 (`src/chat/chat.validator.js`)
- [x] 인증 사용자용 채팅 REST API 추가
- [x] `POST /api/v1/chat/rooms/:id/join`
- [x] `POST /api/v1/chat/rooms/:id/messages`
- [x] 라우터 등록 갱신 (`src/routes/chats.js`)
- [x] OpenAPI `/docs/openapi`에 Chat join/send 경로 및 스키마 반영 (`src/docs/docs.service.js`)
- [x] critical E2E에 Chat REST 검증 추가 (`test/e2e/critical.test.js`)
- [x] 검증 항목 포함
- [x] 채팅방 입장 확인
- [x] 채팅 메시지 전송 확인
- [x] `/docs/openapi` 경로 반영 확인
- [x] 기존 Query/Error/권한 검증과 함께 critical E2E 통과 (`node --env-file=.env --test test/e2e/critical.test.js`)
