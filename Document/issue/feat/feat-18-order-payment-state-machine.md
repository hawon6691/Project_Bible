---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 주문-결제-환불 상태머신 및 정합성 보강"
labels: feature
assignees: ""
---

## ✨ 기능 요약

> 주문-결제-환불 흐름을 상태머신으로 강제하고, 취소/반품/환불 시 재고·포인트 정합성을 보장하도록 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 주문 상태 전이 규칙 구현 (`ORDER_PLACED -> PAYMENT_PENDING -> PAYMENT_CONFIRMED -> PREPARING -> SHIPPING -> DELIVERED -> CONFIRMED`)
- [x] 반품 상태 전이 규칙 구현 (`RETURN_REQUESTED -> RETURNED`)
- [x] 주문 취소 가능 상태 제한 (`ORDER_PLACED`, `PAYMENT_PENDING`)
- [x] 결제 API 구현 (`POST /payments`, `GET /payments/:id`)
- [x] 환불 API 구현 (`POST /payments/:id/refund`, `POST /admin/payments/:id/refund`)
- [x] 반품 요청 API 구현 (`POST /orders/:id/return-request`)
- [x] 환불 시 반품 주문 재고/판매량 롤백
- [x] 환불/취소 시 사용 포인트 환원
- [x] Admin 주문 상태 변경 시 동일 상태머신 검증 적용
- [x] 주문 상세 응답에 결제 이력 포함
- [x] 라우트 상수 보강 (`ORDERS.CANCEL`, `ORDERS.RETURN_REQUEST`, `PAYMENTS`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
