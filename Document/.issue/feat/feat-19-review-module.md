---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 리뷰 모듈 구현 및 리뷰 정합성 로직 추가"
labels: feature
assignees: ""
---

## ✨ 기능 요약

> 구매 확정 주문 기반 리뷰 기능(조회/작성/수정/삭제)을 구현하고, 리뷰 작성 시 포인트 적립 및 상품 평점 통계를 반영했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 리뷰 엔티티 추가 (`reviews`)
- [x] 리뷰 목록 조회 API 구현 (`GET /products/:productId/reviews`)
- [x] 리뷰 작성 API 구현 (`POST /products/:productId/reviews`)
- [x] 리뷰 수정 API 구현 (`PATCH /reviews/:id`)
- [x] 리뷰 삭제 API 구현 (`DELETE /reviews/:id`)
- [x] 구매 확정(`CONFIRMED`) 주문만 리뷰 작성 가능하도록 검증
- [x] 주문 항목(`order_items.isReviewed`) 기반 중복 리뷰 작성 방지
- [x] 리뷰 작성 시 사용자 포인트 500P 자동 적립
- [x] 리뷰 생성/수정/삭제 시 상품 `reviewCount`, `averageRating` 재계산
- [x] `ReviewModule` 앱 모듈 등록
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
