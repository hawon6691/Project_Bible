---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 고객센터 문의 모듈 구현"
labels: feature
issue: "[FEAT] 고객센터 문의 모듈 구현"
commit: "feat: (#31) 고객센터 1:1 문의 작성/조회/답변 API 구현"
branch: "feat/#31/support-module"
assignees: ""
---

## ✨ 기능 요약

> 고객센터 1:1 문의 작성, 내 문의 목록/상세 조회, 관리자 답변/전체 문의 관리 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 고객센터 문의 엔티티 구현 (`support_tickets`)
- [x] 문의 상태 enum 정의 (`OPEN`, `ANSWERED`)
- [x] 고객센터 DTO 구현 (작성/답변/조회 쿼리)
- [x] 고객센터 모듈/서비스/컨트롤러 추가
- [x] 1:1 문의 작성 API 구현 (`POST /support/tickets`)
- [x] 내 문의 목록 API 구현 (`GET /support/tickets/me`)
- [x] 내 문의 상세 API 구현 (`GET /support/tickets/me/:id`)
- [x] 관리자 문의 답변 API 구현 (`POST /admin/support/tickets/:id/answer`)
- [x] 관리자 전체 문의 목록 API 구현 (`GET /admin/support/tickets`)
- [x] 앱 모듈 등록 (`SupportModule`)
- [x] API 라우트 상수 추가 (`SUPPORT`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
