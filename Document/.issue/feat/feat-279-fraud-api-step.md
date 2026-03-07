---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Fraud API (다음 단계) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Fraud API (다음 단계) 프론트엔드 연동"
commit: "feat: (#279) fraud API 연동 및 Fraud API 테스트 페이지 추가"
branch: "feat/#279/fraud-api-step"
assignees: ""
---

## ✨ 기능 요약

> Prediction API 다음 단계로 Fraud API를 프론트엔드에 연동하고, 수동 검증용 Fraud API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Fraud 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `FraudAlertItem`
  - `FraudRealPriceResult`
- [x] Fraud 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchFraudAlertsAdmin` (`GET /fraud/alerts`)
  - `approveFraudAlertAdmin` (`PATCH /fraud/alerts/:id/approve`)
  - `rejectFraudAlertAdmin` (`PATCH /fraud/alerts/:id/reject`)
  - `fetchProductRealPrice` (`GET /products/:id/real-price`)
  - `fetchEffectivePrices` (`GET /fraud/products/:productId/effective-prices`)
  - `detectFraudAnomalies` (`GET /fraud/products/:productId/anomalies`)
  - `scanFraudAnomaliesAdmin` (`POST /fraud/admin/products/:productId/scan`)
  - `fetchFraudFlagsAdmin` (`GET /fraud/admin/products/:productId/flags`)
- [x] Fraud API 테스트 페이지 추가 (`FrontEnd/src/pages/FraudApiPage.tsx`)
  - 실배송비 포함 실가격 조회
  - 이상 가격 탐지(실시간)
  - 관리자 알림 목록 조회/승인/거절
  - 관리자 스캔/저장 로그 조회
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/fraud-api` 경로 추가
  - 상단 메뉴 `FraudAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] API 명세 문서의 Fraud Detection 섹션은 `/fraud/alerts`, `/products/:id/real-price` 중심으로 기술
- [x] 현재 서버는 이전 단계 호환 API(`/fraud/products/:productId/effective-prices`, `/fraud/products/:productId/anomalies`, `/fraud/admin/products/:productId/scan`, `/fraud/admin/products/:productId/flags`)도 함께 제공
- [x] 이번 프론트 단계는 현재 서버 구현 기준으로 확장 반영
