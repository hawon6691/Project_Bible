---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 활동 내역 모듈 구현"
labels: feature
issue: "[FEAT] 활동 내역 모듈 구현"
commit: "feat: (#35) 활동 내역 통합/최근 본 상품/검색 기록 API 구현"
branch: "feat/#35/activity-module"
assignees: ""
---

## ✨ 기능 요약

> 활동 내역 통합 조회, 최근 본 상품 조회, 검색 기록 조회/추가/삭제 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 최근 본 상품 엔티티 구현 (`recent_product_views`)
- [x] 검색 기록 엔티티 구현 (`search_histories`)
- [x] 활동 내역 DTO 구현 (검색어 추가)
- [x] 활동 내역 모듈/서비스/컨트롤러 추가
- [x] 활동 내역 통합 조회 API 구현 (`GET /activities`)
- [x] 최근 본 상품 조회 API 구현 (`GET /activities/recent-products`)
- [x] 최근 본 상품 기록 추가 API 구현 (`POST /activities/recent-products/:productId`)
- [x] 검색 기록 조회 API 구현 (`GET /activities/searches`)
- [x] 검색 기록 추가 API 구현 (`POST /activities/searches`)
- [x] 검색 기록 개별/전체 삭제 API 구현 (`DELETE /activities/searches/:id`, `DELETE /activities/searches`)
- [x] 검색 기록 비활성화 유저(`searchHistoryEnabled=false`) 처리 로직 추가
- [x] 앱 모듈 등록 (`ActivityModule`)
- [x] API 라우트 상수 추가 (`ACTIVITY`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
