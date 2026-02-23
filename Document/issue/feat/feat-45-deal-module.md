---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 특가 딜 모듈 구현"
labels: feature
issue: "[FEAT] 특가 딜 모듈 구현"
commit: "feat: (#45) 특가 조회 및 관리자 딜 CRUD API 구현"
branch: "feat/#45/deal-module"
assignees: ""
---

## ✨ 기능 요약

> 특가 딜 목록 조회와 관리자 특가 등록/수정/삭제 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 딜 엔티티 구현 (`deals`)
- [x] 딜 DTO 구현 (조회/생성/수정)
- [x] 딜 모듈/서비스/컨트롤러 추가
- [x] 특가 목록 조회 API 구현 (`GET /deals`)
- [x] 관리자 특가 등록 API 구현 (`POST /deals/admin`)
- [x] 관리자 특가 수정 API 구현 (`PATCH /deals/admin/:id`)
- [x] 관리자 특가 삭제 API 구현 (`DELETE /deals/admin/:id`)
- [x] 상품 존재 검증 및 딜 기간 검증 로직 추가
- [x] 앱 모듈 등록 (`DealModule`)
- [x] API 라우트 상수 추가 (`DEAL`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
