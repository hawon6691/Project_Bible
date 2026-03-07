---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 가격 분석 모듈 구현"
labels: feature
issue: "[FEAT] 가격 분석 모듈 구현"
commit: "feat: (#73) lowest-ever 및 unit-price 분석 API 구현"
branch: "feat/#73/price-analytics-module"
assignees: ""
---

## ✨ 기능 요약

> 상품의 역대 최저가 여부와 용량/수량당 단가를 계산하는 가격 분석 API를 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Analytics 모듈/서비스/컨트롤러 추가
- [x] 역대 최저가 여부 API 구현 (`GET /analytics/products/:id/lowest-ever`)
- [x] 용량/수량당 단가 API 구현 (`GET /analytics/products/:id/unit-price`)
- [x] 현재 최저가 계산 로직 구현 (`lowestPrice` 우선, 없으면 할인/정가 fallback)
- [x] `price_history` 기반 역대 최저가/최저일 조회 로직 구현
- [x] 상품명/설명 파싱 기반 수량 단위 추출 로직 구현 (kg/g/l/ml/tb/gb/ea)
- [x] 분석용 API 라우트 상수 추가 (`ANALYTICS`)
- [x] 앱 모듈 등록 (`AnalyticsModule`)
- [x] 라우트 상수 누락/포맷 복구 (`MATCHING`, `MEDIA`, `SHORTFORM` 유지)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
