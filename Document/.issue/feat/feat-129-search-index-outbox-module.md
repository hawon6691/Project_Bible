---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 검색 인덱스 동기화 Outbox/워커 모듈 구현"
labels: feature
issue: "[FEAT] 검색 인덱스 동기화 Outbox/워커 모듈 구현"
commit: "feat: (#129) search index outbox event + bull worker 동기화 파이프라인 추가"
branch: "feat/#129/search-index-outbox-module"
assignees: ""
---

## ✨ 기능 요약

> 상품/가격 변경 이벤트를 Outbox 테이블에 적재하고, Bull 워커가 Elasticsearch 재색인을 비동기로 수행하도록 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 검색 인덱스 Outbox 엔티티 구현 (`search_index_outbox`)
- [x] Outbox 이벤트/상태 enum 추가 (`PRODUCT_UPSERT`, `PRODUCT_DELETE`, `PRICE_CHANGED`, `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`)
- [x] `search-index-sync` Bull Queue 등록
- [x] Outbox 처리 워커 프로세서 추가 (`SearchSyncProcessor`)
- [x] Outbox 서비스 구현 (`enqueue*`, `processOutbox`, `requeueFailed`, `summary`)
- [x] 상품 생성/수정/삭제 시 Outbox 이벤트 발행 연결
- [x] 가격 등록/수정/삭제 시 Outbox 이벤트 발행 연결
- [x] SearchService에 단일 상품 인덱스 삭제 메서드 추가 (`removeProductDocument`)
- [x] 관리자 Outbox 모니터링/재큐잉 API 구현
- [x] API 라우트 상수 확장 (`SEARCH.ADMIN_INDEX_OUTBOX_*`)
- [x] DB 마이그레이션 추가 (`SearchIndexOutboxMigration`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
