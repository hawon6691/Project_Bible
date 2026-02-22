---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 가격 신뢰성(Fraud Alerts) 스펙 확장"
labels: feature
issue: "[FEAT] 가격 신뢰성(Fraud Alerts) 스펙 확장"
commit: "feat: (#71) fraud alerts 승인/거절 및 real-price API 추가"
branch: "feat/#71/fraud-alerts-spec"
assignees: ""
---

## ✨ 기능 요약

> 가격 신뢰성 스펙에 맞춰 이상 가격 알림 목록/승인/거절과 배송비 포함 실제 가격 조회 API를 추가 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Fraud 알림 상태 enum 추가 (`PENDING`, `APPROVED`, `REJECTED`)
- [x] `fraud_flags` 엔티티 상태/검토자/검토시각 컬럼 추가
- [x] Fraud Alert 조회 DTO 구현 (`FraudAlertQueryDto`)
- [x] Real Price 조회 DTO 구현 (`RealPriceQueryDto`)
- [x] 이상 가격 알림 목록 API 구현 (`GET /fraud/alerts`)
- [x] 이상 가격 알림 승인 API 구현 (`PATCH /fraud/alerts/:id/approve`)
- [x] 이상 가격 알림 거절 API 구현 (`PATCH /fraud/alerts/:id/reject`)
- [x] 배송비 포함 실제 가격 조회 API 구현 (`GET /products/:id/real-price`)
- [x] 알림 거절 시 가격 엔트리 비활성화 로직 추가
- [x] 기존 이상탐지 API 유지 (`/fraud/products/...`)
- [x] API 라우트 상수 FRAUD 확장 (`ALERTS`, `ALERT_APPROVE`, `ALERT_REJECT`, `REAL_PRICE`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
