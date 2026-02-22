---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 상품 매핑 모듈 구현"
labels: feature
issue: "[FEAT] 상품 매핑 모듈 구현"
commit: "feat: (#69) 상품 매핑 대기/승인/거절/자동매칭/통계 API 구현"
branch: "feat/#69/product-matching-module"
assignees: ""
---

## ✨ 기능 요약

> 관리자 상품 매핑 대기 목록 조회, 수동 승인/거절, 자동 매칭 실행, 매핑 통계 조회 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 상품 매핑 엔티티 구현 (`product_mappings`)
- [x] 매핑 상태 enum 구현 (PENDING/APPROVED/REJECTED)
- [x] 매핑 DTO 구현 (대기 목록 쿼리, 승인, 거절)
- [x] Matching 모듈/서비스/컨트롤러 추가
- [x] 매핑 대기 목록 API 구현 (`GET /matching/pending`)
- [x] 매핑 승인 API 구현 (`PATCH /matching/:id/approve`)
- [x] 매핑 거절 API 구현 (`PATCH /matching/:id/reject`)
- [x] 자동 매칭 실행 API 구현 (`POST /matching/auto-match`)
- [x] 매핑 통계 조회 API 구현 (`GET /matching/stats`)
- [x] sourceName 기반 유사 이름 자동 매칭 로직 구현
- [x] 대기 상태 매핑만 처리 가능한 상태 검증 로직 구현
- [x] 앱 모듈 등록 (`MatchingModule`)
- [x] API 라우트 상수 추가 (`MATCHING`)
- [x] 누락된 API 라우트 상수 복구 (`MEDIA`, `SHORTFORM`, `NEWS` 포함 정렬)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
