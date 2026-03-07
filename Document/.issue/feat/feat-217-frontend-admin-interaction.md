---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 관리자 프론트엔드 기능 상호작용 연동"
labels: feature
issue: "[FEAT] 관리자 프론트엔드 기능 상호작용 연동"
commit: "feat: (#217) admin frontend faq/notice/settings/stats api 연동"
branch: "feat/#217/frontend-admin-interaction"
assignees: ""
---

## ✨ 기능 요약

> 관리자 페이지의 FAQ/공지/설정/통계 화면을 실제 백엔드 API와 연동해 CRUD/조회 상호작용이 가능하도록 개선했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] FAQ 관리 페이지 상호작용 구현 (`/admin/faqs`)
- [x] FAQ 목록/검색/등록/수정/삭제 연동 (`GET /faq`, `POST/PATCH/DELETE /admin/faq`)
- [x] 공지사항 관리 페이지 상호작용 구현 (`/admin/notices`)
- [x] 공지 목록/등록/수정/삭제 연동 (`GET /notices`, `POST/PATCH/DELETE /admin/notices`)
- [x] 시스템 설정 페이지 상호작용 구현 (`/admin/settings`)
- [x] 허용 확장자/업로드 제한/리뷰 정책 조회 및 저장 연동 (`/admin/settings/*`)
- [x] 통계 페이지 상호작용 구현 (`/admin/stats`)
- [x] 운영 요약/관측성 메트릭/트레이스 조회 연동 (`/admin/ops-dashboard/summary`, `/admin/observability/*`)
- [x] 프론트 API 엔드포인트 확장 (`src/lib/api/endpoints.ts`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
