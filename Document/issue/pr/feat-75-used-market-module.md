---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 중고 마켓 모듈 구현"
labels: feature
issue: "[FEAT] 중고 마켓 모듈 구현"
commit: "feat: (#75) 상품/카테고리 중고 시세 및 PC견적 매입가 API 구현"
branch: "feat/#75/used-market-module"
assignees: ""
---

## ✨ 기능 요약

> 상품 중고 시세 조회, 카테고리별 중고 시세 목록 조회, PC 견적 기반 중고 매입가 산정 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Used Market 모듈/서비스/컨트롤러 추가
- [x] 카테고리 시세 페이징 DTO 구현 (`UsedMarketCategoryQueryDto`)
- [x] 특정 상품 중고 시세 API 구현 (`GET /used-market/products/:id/price`)
- [x] 카테고리별 중고 시세 API 구현 (`GET /used-market/categories/:id/prices`)
- [x] PC 견적 기반 매입가 API 구현 (`POST /used-market/pc-builds/:buildId/estimate`)
- [x] 상품 현재가 기반 평균/최저/최고 중고가 계산 로직 구현
- [x] 가격 이력 기반 시세 추세(`UP/DOWN/STABLE`) 계산 로직 구현
- [x] PC 부품 타입별 감가율 적용 매입가 산정 로직 구현
- [x] 본인 PC 견적만 산정 가능하도록 소유권 검증 추가
- [x] 앱 모듈 등록 (`UsedMarketModule`)
- [x] API 라우트 상수 추가 (`USED_MARKET`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
