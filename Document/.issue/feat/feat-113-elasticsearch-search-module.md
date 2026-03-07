---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Elasticsearch 기반 검색 모듈 고도화"
labels: feature
issue: "[FEAT] Elasticsearch 기반 검색 모듈 고도화"
commit: "feat: (#113) ES 검색/자동완성/인덱싱 관리 API 구현"
branch: "feat/#113/elasticsearch-search-module"
assignees: ""
---

## ✨ 기능 요약

> 기존 DB 검색을 Elasticsearch 기반으로 고도화하고, 자동완성/오타보정/관리자 재색인 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Search 모듈에 Elasticsearch 클라이언트 등록 (`@nestjs/elasticsearch`)
- [x] 검색 인덱스 생성 로직 구현 (`products_v1`, nori + completion 매핑)
- [x] 통합 검색 API를 Elasticsearch 쿼리 기반으로 전환 (`GET /search`)
- [x] 자동완성 API를 completion suggester 기반으로 전환 (`GET /search/autocomplete`)
- [x] 오타보정(fuzziness AUTO) 검색 로직 적용
- [x] 검색 가중치 설정값을 ES multi_match 필드 boost에 반영
- [x] 카테고리/가격/평점/판매처 필터를 ES bool filter로 반영
- [x] 관리자 인덱스 상태 조회 API 구현 (`GET /search/admin/index/status`)
- [x] 관리자 전체 재색인 API 구현 (`POST /search/admin/index/reindex`)
- [x] 관리자 단일 상품 재색인 API 구현 (`POST /search/admin/index/products/:id/reindex`)
- [x] ES 장애 시 DB 검색 폴백 로직 구현
- [x] API 라우트 상수 확장 (`SEARCH.ADMIN_INDEX_*`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
