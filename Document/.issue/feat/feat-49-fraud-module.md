---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 이상 가격 탐지 모듈 구현"
labels: feature
issue: "[FEAT] 이상 가격 탐지 모듈 구현"
commit: "feat: (#49) 이상 가격 탐지 및 배송비 포함 체감가 계산 API 구현"
branch: "feat/#49/fraud-module"
assignees: ""
---

## ✨ 기능 요약

> 평균 대비 비정상 가격 탐지와 배송비 포함 체감가 계산 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 이상 가격 탐지 로그 엔티티 구현 (`fraud_flags`)
- [x] 탐지 쿼리 DTO 구현 (`FraudScanQueryDto`)
- [x] Fraud 모듈/서비스/컨트롤러 추가
- [x] 배송비 포함 체감가 조회 API 구현 (`GET /fraud/products/:productId/effective-prices`)
- [x] 이상 가격 탐지 API 구현 (`GET /fraud/products/:productId/anomalies`)
- [x] 관리자 탐지 실행/저장 API 구현 (`POST /fraud/admin/products/:productId/scan`)
- [x] 관리자 탐지 로그 조회 API 구현 (`GET /fraud/admin/products/:productId/flags`)
- [x] 평균 대비 하한/상한 비율 기반 이상값 판정 로직 구현
- [x] 앱 모듈 등록 (`FraudModule`)
- [x] API 라우트 상수 추가 (`FRAUD`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
