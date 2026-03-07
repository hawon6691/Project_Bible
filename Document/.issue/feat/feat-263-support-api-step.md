---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 고객센터 Support API (Step 17) 프론트엔드 연동"
labels: feature
issue: "[FEAT] 고객센터 Support API (Step 17) 프론트엔드 연동"
commit: "feat: (#263) support API 연동 및 Support API 테스트 페이지 추가"
branch: "feat/#263/support-api-step"
assignees: ""
---

## ✨ 기능 요약

> Inquiry API(16번) 다음 단계인 고객센터 Support API(17번)를 프론트엔드에 연동하고, 수동 검증용 Support API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 고객센터 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `SupportTicketItem`
- [x] 고객센터 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `createSupportTicket` (`POST /support/tickets`)
  - `fetchMySupportTickets` (`GET /support/tickets/me`)
  - `fetchMySupportTicket` (`GET /support/tickets/me/:id`)
  - `answerSupportTicketAdmin` (`POST /admin/support/tickets/:id/answer`)
  - `fetchAdminSupportTickets` (`GET /admin/support/tickets`)
- [x] 고객센터 API 테스트 페이지 추가 (`FrontEnd/src/pages/SupportApiPage.tsx`)
  - 문의 작성
  - 내 문의 목록/상세 조회
  - 관리자 문의 목록 조회
  - 관리자 답변 등록
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/support-api` 경로 추가
  - 상단 메뉴 `SupportAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세(문서)에는 `GET /support/tickets`, `GET /support/tickets/:id`, `POST /support/tickets/:id/reply`, `PATCH /admin/support/tickets/:id/status`가 기술되어 있으나
- [x] 현재 서버 구현은 `GET /support/tickets/me`, `GET /support/tickets/me/:id`, `POST /admin/support/tickets/:id/answer`, `GET /admin/support/tickets`를 제공
- [x] 이번 프론트 단계는 서버 구현 기준으로 우선 반영
