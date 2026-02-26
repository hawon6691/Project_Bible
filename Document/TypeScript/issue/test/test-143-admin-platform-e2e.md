---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[TEST] 관리자 운영 API E2E 시나리오 추가"
labels: test
issue: "[TEST] 관리자 운영 API E2E 시나리오 추가"
commit: "test: (#143) search-outbox/crawler/resilience 관리자 API E2E 테스트 추가"
branch: "test/#143/admin-platform-e2e"
assignees: ""
---

## ✨ 기능 요약

> 관리자 운영 API(Search 인덱스/Outbox, Crawler 모니터링, Resilience) 중심 E2E 시나리오를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 관리자 운영 API E2E 스펙 파일 추가 (`test/e2e/admin-platform.e2e-spec.ts`)
- [x] Search 인덱스 상태/재색인 API E2E 검증 추가 (`/search/admin/index/*`)
- [x] Search Outbox 요약/재큐잉 API E2E 검증 추가 (`/search/admin/index/outbox/*`)
- [x] Crawler 모니터링 API E2E 검증 추가 (`/crawler/admin/monitoring`)
- [x] Crawler 목록 쿼리 검증 실패(400) 시나리오 추가 (`limit=0`)
- [x] Resilience 목록/상세/리셋 API E2E 검증 추가 (`/resilience/circuit-breakers*`)
- [x] 공통 응답 래핑(`success`, `data`) 및 호출 인자 검증 추가
