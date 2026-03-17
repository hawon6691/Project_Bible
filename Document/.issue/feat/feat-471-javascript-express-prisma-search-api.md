---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Search API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Search API 구현"
commit: "feat: (#471) JavaScript Express Prisma Search API 구현"
branch: "feat/#471/javascript-express-prisma-search-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 통합 검색, 자동완성, 최근 검색어, 인기 검색어, 검색 인덱스 관리 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `search` 통합 검색 API 추가
- [x] `search/autocomplete` 자동완성 API 추가
- [x] `search/popular` 인기 검색어 조회 API 추가
- [x] `search/recent` 최근 검색어 생성 API 추가
- [x] `search/recent` 최근 검색어 조회 API 추가
- [x] `search/recent/:keywordId` 최근 검색어 삭제 API 추가
- [x] `search/preferences` 검색 설정 갱신 API 추가
- [x] `search/admin/weights` 검색 가중치 조회 API 추가
- [x] `search/admin/weights` 검색 가중치 수정 API 추가
- [x] `search/admin/index/status` 검색 인덱스 상태 조회 API 추가
- [x] `search/admin/index/reindex` 전체 재색인 API 추가
- [x] `search/admin/index/products/:id/reindex` 개별 상품 재색인 API 추가
- [x] `search/admin/index/outbox/summary` 인덱스 아웃박스 요약 API 추가
- [x] `search/admin/index/outbox/requeue-failed` 실패 작업 재큐잉 API 추가
- [x] Prisma schema에 `search_synonyms`, `product_query_views`, `search_index_outbox`, `search_weight_settings`, `search_recent_keywords` 매핑 추가
- [x] 검색 로그 저장 및 집계 기반 반영
- [x] 관리자 검색 인덱스 기능 권한 검증 적용
- [x] 라우트에 `search` 경로 연결
- [x] README 노출 경로 요약 갱신
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
