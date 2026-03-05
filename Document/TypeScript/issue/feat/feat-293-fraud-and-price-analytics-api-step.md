---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Fraud Detection + Price Analytics API (Step 39-40) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Fraud Detection + Price Analytics API (Step 39-40) 프론트엔드 연동"
commit: "feat: (#293) fraud 보강 + price-analytics API 연동 및 테스트 페이지 추가"
branch: "feat/#293/fraud-price-analytics-api-step"
assignees: ""
---

## ✨ 기능 요약

> 요청하신 2단계 묶음 방식으로 Fraud Detection API(39번)는 보강하고, Price Analytics API(40번)를 신규 연동했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Price Analytics 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `LowestEverAnalyticsResult`
  - `UnitPriceAnalyticsResult`
- [x] Price Analytics 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchLowestEverAnalytics` (`GET /analytics/products/:id/lowest-ever`)
  - `fetchUnitPriceAnalytics` (`GET /analytics/products/:id/unit-price`)
- [x] Price Analytics 테스트 페이지 추가 (`FrontEnd/src/pages/PriceAnalyticsApiPage.tsx`)
  - 역대 최저가 여부 조회
  - 단위 가격 조회
- [x] Fraud API 보강 (`FrontEnd/src/pages/FraudApiPage.tsx`)
  - 이상 탐지/스캔 요청 시 `lowerBoundRatio`, `upperBoundRatio`, `limit` 입력 지원
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/price-analytics-api` 경로 추가
  - 상단 메뉴 `PriceAnalyticsAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] Fraud Detection API는 기존 구현을 유지하고 운영 파라미터 입력만 보강
- [x] Price Analytics API는 서버 `analytics.controller.ts` 기준으로 `lowest-ever`, `unit-price`를 그대로 반영
