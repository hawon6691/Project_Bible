---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 비교함 모듈 구현"
labels: feature
issue: "[FEAT] 비교함 모듈 구현"
commit: "feat: (#81) 비교함 추가/삭제/목록/상세 API 구현"
branch: "feat/#81/product-compare-bar"
assignees: ""
---

## ✨ 기능 요약

> 비교함에 상품을 추가/삭제하고 현재 목록 및 비교 상세(차이 강조)를 조회하는 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 비교함 엔티티 구현 (`compare_items`)
- [x] 비교함 추가 DTO 구현 (`AddCompareItemDto`)
- [x] Compare 모듈/서비스/컨트롤러 추가
- [x] 비교함 상품 추가 API 구현 (`POST /compare/add`)
- [x] 비교함 상품 제거 API 구현 (`DELETE /compare/:productId`)
- [x] 비교함 목록 조회 API 구현 (`GET /compare`)
- [x] 비교 상세 조회 API 구현 (`GET /compare/detail`)
- [x] 비교함 최대 4개 제한 로직 구현
- [x] 중복 상품 추가 방지 로직 구현
- [x] 핵심 필드 차이 강조 계산 로직 구현 (`price`, `categoryId`, `rating`, `reviewCount` 등)
- [x] 게스트 식별용 compareKey 처리 로직 구현 (헤더 `x-compare-key`, 기본 `guest`)
- [x] 앱 모듈 등록 (`CompareModule`)
- [x] API 라우트 상수 추가 (`COMPARE_BAR`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
