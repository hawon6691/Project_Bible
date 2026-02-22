---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 가격 예측 모듈 구현"
labels: feature
issue: "[FEAT] 가격 예측 모듈 구현"
commit: "feat: (#47) 상품 가격 추세 예측 API 구현"
branch: "feat/#47/prediction-module"
assignees: ""
---

## ✨ 기능 요약

> 가격 이력 데이터를 기반으로 상품의 미래 가격 추세를 예측하는 API를 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 예측 DTO 구현 (`PricePredictionQueryDto`)
- [x] 예측 모듈/서비스/컨트롤러 추가
- [x] 가격 추세 예측 API 구현 (`GET /predictions/products/:productId/price-trend`)
- [x] 선형회귀 기반 단순 추세 예측 로직 구현
- [x] 데이터 부족 시 현재가 기반 폴백 로직 구현
- [x] 추세(UP/DOWN/FLAT), 예상 변화량, 신뢰도 계산 포함
- [x] 앱 모듈 등록 (`PredictionModule`)
- [x] API 라우트 상수 추가 (`PREDICTION`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
