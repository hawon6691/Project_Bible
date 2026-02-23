---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Search 모듈 구현"
labels: feature
issue: "[FEAT] Search 모듈 구현"
commit: "feat: (#89) 통합검색/자동완성/최근검색어/가중치 관리 API 구현"
branch: "feat/#89/search-module"
assignees: ""
---

## ✨ 기능 요약

> 통합 검색, 자동완성, 인기 검색어, 최근 검색어 관리, 검색 가중치 관리 기능을 제공하는 Search 모듈을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 검색 로그 엔티티 구현 (`search_logs`)
- [x] 최근 검색어 엔티티 구현 (`search_recent_keywords`)
- [x] 검색 가중치 엔티티 구현 (`search_weight_settings`)
- [x] Search DTO 구현 (검색/자동완성/인기검색/최근검색/가중치)
- [x] Search 모듈/서비스/컨트롤러 추가
- [x] 상품 통합 검색 API 구현 (`GET /search`)
- [x] 자동완성 API 구현 (`GET /search/autocomplete`)
- [x] 인기 검색어 API 구현 (`GET /search/popular`)
- [x] 최근 검색어 저장/조회/개별삭제/전체삭제 API 구현 (`POST/GET/DELETE /search/recent`)
- [x] 검색어 자동 저장 설정 API 구현 (`PATCH /search/preferences`)
- [x] 검색 가중치 조회/수정 API 구현 (`GET/PATCH /search/admin/weights`)
- [x] 검색 로그 저장/연관검색어/하이라이트 처리 로직 구현
- [x] 앱 모듈 등록 (`SearchModule`)
- [x] API 라우트 상수 확장 (`SEARCH`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
