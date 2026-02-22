---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] CQRS Query 모듈 구현"
labels: feature
issue: "[FEAT] CQRS Query 모듈 구현"
commit: "feat: (#101) 읽기 모델 분리 및 프로젝션 동기화 API 구현"
branch: "feat/#101/cqrs-query-module"
assignees: ""
---

## ✨ 기능 요약

> 쓰기 모델(`products`, `price_entries`)과 분리된 읽기 모델(`product_query_views`)을 도입하고, 조회 전용 API 및 프로젝션 동기화 API를 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] CQRS 읽기 모델 엔티티 구현 (`product_query_views`)
- [x] Query DTO 구현 (필터/정렬/페이징)
- [x] Query 모듈/서비스/컨트롤러 추가
- [x] 읽기 모델 기반 상품 목록 API 구현 (`GET /query/products`)
- [x] 읽기 모델 기반 상품 상세 API 구현 (`GET /query/products/:productId`)
- [x] 관리자 단건 프로젝션 동기화 API 구현 (`POST /admin/query/products/:productId/sync`)
- [x] 관리자 전체 프로젝션 재구축 API 구현 (`POST /admin/query/products/rebuild`)
- [x] 쓰기 모델 -> 읽기 모델 투영 로직 구현 (최저가/판매처수/평점/인기점수)
- [x] 정렬 로직 구현 (최신/가격/인기/평점)
- [x] 앱 모듈 등록 (`QueryModule`)
- [x] API 라우트 상수 확장 (`QUERY`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
