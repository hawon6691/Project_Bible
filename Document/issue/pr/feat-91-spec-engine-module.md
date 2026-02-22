---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Spec Engine 고도화 모듈 구현"
labels: feature
issue: "[FEAT] Spec Engine 고도화 모듈 구현"
commit: "feat: (#91) 스펙 상속/그룹핑/수치비교/점수산출/유사상품추천 API 구현"
branch: "feat/#91/spec-engine-module"
assignees: ""
---

## ✨ 기능 요약

> 스펙 비교 엔진 고도화를 위해 카테고리 상속 스키마, 스펙 그룹핑, 수치형 하이라이팅 비교, 종합 성능 점수, 유사 상품 추천 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 스펙 정의 확장 컬럼 추가 (`group_name`, `parent_definition_id`, `higher_is_better`)
- [x] Spec Engine DTO 추가 (수치 비교, 카테고리 점수, 유사상품 쿼리)
- [x] 카테고리 상속 스펙 정의 조회 API 구현 (`GET /specs/definitions/resolved/:categoryId`)
- [x] 스펙 그룹핑 조회 API 구현 (`GET /products/:id/specs/grouped`)
- [x] 수치형 스펙 비교 API 구현 (`POST /specs/compare/numeric`)
- [x] 카테고리 기준 종합 점수 API 구현 (`POST /specs/score`)
- [x] 스펙 유사 상품 추천 API 구현 (`GET /products/:id/similar-spec-products`)
- [x] 기존 스펙 CRUD/비교 API와 호환 유지
- [x] 카테고리 체인 기반 정의 병합 로직 구현 (동적 스키마 상속)
- [x] 수치형 best/worst 하이라이트 판정 로직 구현
- [x] 가중치/점수 매핑 기반 종합 점수 계산 로직 보강
- [x] 유사도(수치 스펙 거리 + 동일 값 매칭) 계산 로직 구현
- [x] 스펙 모듈 의존성 확장 (`Category` Repository 주입)
- [x] API 라우트 상수 확장 (`SPECS`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
