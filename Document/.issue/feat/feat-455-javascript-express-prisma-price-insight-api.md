---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Price Insight API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Price Insight API 구현"
commit: "feat: (#455) JavaScript Express Prisma Price Insight API 구현"
branch: "feat/#455/javascript-express-prisma-price-insight-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 가격 신뢰성, 중고 시세, 비교함 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `fraud` 알림 목록 조회, 승인, 거절 API 추가
- [x] `products/:id/real-price` 실구매가 조회 API 추가
- [x] `used-market/products/:id/price` 특정 상품 중고 시세 API 추가
- [x] `used-market/categories/:id/prices` 카테고리별 중고 시세 API 추가
- [x] `used-market/pc-builds/:buildId/estimate` PC 견적 기반 중고 매입가 산정 API 추가
- [x] `compare/add`, `compare`, `compare/detail`, `compare/:productId` API 추가
- [x] Prisma schema에 `fraud_alerts`, `used_prices`, `pc_builds`, `build_parts`, `compare_items` 매핑 추가
- [x] 라우트 인덱스에 `fraud`, `used-market`, `compare` 라우터 연결
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
