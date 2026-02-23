---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Redis 캐싱 정책 정리 및 핵심 모듈 적용"
labels: feature
issue: "[FEAT] Redis 캐싱 정책 정리 및 핵심 모듈 적용"
commit: "feat: (#127) 공통 캐시 정책/서비스 추가 및 product price ranking news 캐시 적용"
branch: "feat/#127/redis-cache-policy-module"
assignees: ""
---

## ✨ 기능 요약

> Redis 캐시 정책(TTL/키 규칙)을 공통화하고 상품/가격/랭킹/뉴스 모듈에 캐시 조회 및 무효화 로직을 적용했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 공통 캐시 TTL 정책 상수 추가 (`CACHE_TTL_SECONDS`)
- [x] 공통 캐시 키 규칙 상수 추가 (`CACHE_KEY_PREFIX`, `CACHE_KEYS`)
- [x] 공통 Redis 캐시 서비스 추가 (`CacheService`)
- [x] 패턴 기반 캐시 무효화 헬퍼 추가 (`delByPattern`)
- [x] Product 목록/상세 캐시 적용 및 변경 시 무효화
- [x] Price 비교/이력 캐시 적용 및 가격 변경 시 무효화
- [x] Ranking 인기상품/인기검색어 캐시 적용 및 재계산 시 무효화
- [x] News 카테고리/목록/상세 캐시 적용 및 CRUD 시 무효화
- [x] CommonModule 전역 provider/export 등록 (`CacheService`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
