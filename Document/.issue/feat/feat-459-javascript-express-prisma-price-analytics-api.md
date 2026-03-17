---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Price Analytics API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Price Analytics API 구현"
commit: "feat: (#459) JavaScript Express Prisma Price Analytics API 구현"
branch: "feat/#459/javascript-express-prisma-price-analytics-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 가격 분석 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `GET /analytics/products/:id/lowest-ever` 역대 최저가 여부 확인 API 추가
- [x] `GET /analytics/products/:id/unit-price` 용량/수량당 단가 계산 API 추가
- [x] `price_history` 기준 역대 최저가 조회 로직 추가
- [x] 상품명/설명 기준 수량·단위 파싱 로직 추가
- [x] 라우트 인덱스에 `analytics` 라우터 연결
- [x] 앱 로딩 확인 완료
- [x] 대표 엔드포인트 수동 검증 완료
