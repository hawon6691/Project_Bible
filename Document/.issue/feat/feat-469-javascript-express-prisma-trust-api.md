---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Trust API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Trust API 구현"
commit: "feat: (#469) JavaScript Express Prisma Trust API 구현"
branch: "feat/#469/javascript-express-prisma-trust-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 판매처 신뢰도 조회 및 판매처 리뷰 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `sellers/:id/trust` 판매처 신뢰도 상세 조회 API 추가
- [x] `sellers/:id/reviews` 판매처 리뷰 목록 조회 API 추가
- [x] `sellers/:id/reviews` 판매처 리뷰 작성 API 추가
- [x] `seller-reviews/:id` 판매처 리뷰 수정 API 추가
- [x] `seller-reviews/:id` 판매처 리뷰 삭제 API 추가
- [x] Prisma schema에 `seller_trust_metrics`, `seller_reviews` 매핑 추가
- [x] 판매처 리뷰 생성, 수정, 삭제 후 trust summary 재계산 로직 추가
- [x] 라우트에 `trust` 및 `seller review` 경로 연결
- [x] README 노출 경로 요약 갱신
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
