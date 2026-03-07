---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Prediction API (Step 24) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Prediction API (Step 24) 프론트엔드 연동"
commit: "feat: (#277) prediction API 연동 및 Prediction API 테스트 페이지 추가"
branch: "feat/#277/prediction-api-step"
assignees: ""
---

## ✨ 기능 요약

> Deal API(23번) 다음 단계인 Prediction API(24번)를 프론트엔드에 연동하고, 수동 검증용 Prediction API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Prediction 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `PricePredictionResult`
- [x] Prediction 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchPricePrediction` (`GET /predictions/products/:productId/price-trend`)
- [x] Prediction API 테스트 페이지 추가 (`FrontEnd/src/pages/PredictionApiPage.tsx`)
  - 상품 ID 기반 가격 추세 예측 조회
  - `horizonDays`, `lookbackDays` 쿼리 입력 지원
  - 결과 JSON 출력
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/prediction-api` 경로 추가
  - 상단 메뉴 `PredictionAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세 문서 예시는 `?days` 파라미터 중심으로 설명
- [x] 현재 서버 구현(`price-prediction-query.dto.ts`)은 `horizonDays`, `lookbackDays`를 사용
- [x] 이번 프론트 단계는 서버 구현 기준으로 우선 반영
