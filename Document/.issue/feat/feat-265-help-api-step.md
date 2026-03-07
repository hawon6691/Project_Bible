---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Help API (Step 18) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Help API (Step 18) 프론트엔드 연동"
commit: "feat: (#265) help API 연동 및 Help API 테스트 페이지 추가"
branch: "feat/#265/help-api-step"
assignees: ""
---

## ✨ 기능 요약

> Support API(17번) 다음 단계인 Help API(18번: FAQ/공지사항)를 프론트엔드에 연동하고, 수동 검증용 Help API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Help 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `FaqItem`
  - `NoticeItem`
- [x] Help 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchFaqs` (`GET /faq`) - 서버 구현 기준
  - `createFaqAdmin` (`POST /admin/faq`) - 서버 구현 기준
  - `updateFaqAdmin` (`PATCH /admin/faq/:id`) - 서버 구현 기준
  - `removeFaqAdmin` (`DELETE /admin/faq/:id`) - 서버 구현 기준
  - `fetchNotices` (`GET /notices`)
  - `createNoticeAdmin` (`POST /admin/notices`) - 서버 구현 기준
  - `updateNoticeAdmin` (`PATCH /admin/notices/:id`) - 서버 구현 기준
  - `removeNoticeAdmin` (`DELETE /admin/notices/:id`) - 서버 구현 기준
- [x] Help API 테스트 페이지 추가 (`FrontEnd/src/pages/HelpApiPage.tsx`)
  - FAQ 목록/검색
  - FAQ 등록/수정/삭제(Admin)
  - 공지사항 목록
  - 공지사항 등록/수정/삭제(Admin)
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/help-api` 경로 추가
  - 상단 메뉴 `HelpAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세 문서는 `/faqs`, `/faqs/:id`, `/notices/:id` 형태를 정의
- [x] 현재 서버 구현은 FAQ가 `/faq`, `/admin/faq/:id` 형태이며 `GET /notices/:id`는 제공하지 않음
- [x] 이번 프론트 단계는 서버 구현 기준으로 우선 반영
