---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FAQ/공지사항 모듈 구현"
labels: feature
issue: "[FEAT] FAQ/공지사항 모듈 구현"
commit: "feat: (#33) FAQ 조회/검색 및 공지사항 관리 API 구현"
branch: "feat/#33/faq-module"
assignees: ""
---

## ✨ 기능 요약

> FAQ 목록/검색과 관리자 FAQ 관리, 공지사항 목록 조회와 관리자 공지 관리 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] FAQ 엔티티 구현 (`faqs`)
- [x] 공지사항 엔티티 구현 (`notices`)
- [x] FAQ DTO 구현 (생성/수정/조회 쿼리)
- [x] 공지사항 DTO 구현 (생성/수정)
- [x] FAQ 모듈/서비스/컨트롤러 추가
- [x] FAQ 목록/검색 API 구현 (`GET /faq`)
- [x] 관리자 FAQ 등록/수정/삭제 API 구현 (`POST/PATCH/DELETE /admin/faq`)
- [x] 공지사항 목록 API 구현 (`GET /notices`)
- [x] 관리자 공지 등록/수정/삭제 API 구현 (`POST/PATCH/DELETE /admin/notices`)
- [x] 앱 모듈 등록 (`FaqModule`)
- [x] API 라우트 상수 추가 (`FAQ`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
