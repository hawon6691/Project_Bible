---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Used Market + Auto API (Step 41-42) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Used Market + Auto API (Step 41-42) 프론트엔드 연동"
commit: "feat: (#295) used-market+auto API 연동 및 테스트 페이지 추가"
branch: "feat/#295/used-market-auto-api-step"
assignees: ""
---

## ✨ 기능 요약

> 요청하신 2단계 묶음 방식으로 Used Market API(41번), Auto API(42번)를 프론트엔드에 연동하고 수동 검증용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Used Market/Auto 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `UsedProductPriceResult`, `UsedCategoryPriceItem`
  - `AutoModelItem`, `AutoTrimItem`, `AutoEstimateResult`, `AutoLeaseOfferItem`
- [x] Used Market 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchUsedProductPrice` (`GET /used-market/products/:id/price`)
  - `fetchUsedCategoryPrices` (`GET /used-market/categories/:id/prices`)
  - `estimateUsedPcBuildPrice` (`POST /used-market/pc-builds/:buildId/estimate`)
- [x] Auto 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchAutoModels` (`GET /auto/models`)
  - `fetchAutoTrims` (`GET /auto/models/:id/trims`)
  - `estimateAuto` (`POST /auto/estimate`)
  - `fetchAutoLeaseOffers` (`GET /auto/models/:id/lease-offers`)
- [x] Used Market/Auto 테스트 페이지 추가
  - `FrontEnd/src/pages/UsedMarketApiPage.tsx`
  - `FrontEnd/src/pages/AutoApiPage.tsx`
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/used-market-api`, `/auto-api` 경로 추가
  - 상단 메뉴 `UsedMarketAPI`, `AutoAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] Used Market API는 서버 구현에 맞춰 `trend` 값을 `UP | DOWN | STABLE`로 반영
- [x] Auto API는 현재 예시 데이터 기반 서비스(`auto.service.ts`)를 그대로 기준으로 연동
