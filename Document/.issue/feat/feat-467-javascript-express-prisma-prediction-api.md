---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Prediction API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Prediction API 구현"
commit: "feat: (#467) JavaScript Express Prisma Prediction API 구현"
branch: "feat/#467/javascript-express-prisma-prediction-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 상품 가격 추세 예측 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `predictions/products/:productId/price-trend` 상품 가격 추세 예측 조회 API 추가
- [x] Prisma schema에 `price_predictions` 매핑 추가
- [x] 저장된 `price_predictions` 기반 응답 로직 추가
- [x] `price_history` 또는 현재 가격 기반 fallback 예측 로직 추가
- [x] 라우트 인덱스에 `predictions` 라우터 연결
- [x] README 노출 경로 요약 갱신
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
