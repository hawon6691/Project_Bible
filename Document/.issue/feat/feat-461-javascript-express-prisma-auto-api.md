---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Auto API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Auto API 구현"
commit: "feat: (#461) JavaScript Express Prisma Auto API 구현"
branch: "feat/#461/javascript-express-prisma-auto-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 자동차 모델 및 리스 상품 조회 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `auto/models` 자동차 모델 목록 조회 API 추가
- [x] `auto/models` `brand`, `type` 필터 지원
- [x] `auto/models/:id/lease-offers` 모델별 리스 상품 조회 API 추가
- [x] Prisma schema에 `car_models`, `lease_offers` 매핑 추가
- [x] 라우트 인덱스에 `auto` 라우터 연결
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
