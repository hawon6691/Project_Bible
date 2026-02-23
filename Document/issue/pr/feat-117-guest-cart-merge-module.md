---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 비회원 장바구니 Redis 세션 병합 구현"
labels: feature
issue: "[FEAT] 비회원 장바구니 Redis 세션 병합 구현"
commit: "feat: (#117) 게스트 장바구니 저장/조회/병합 API 구현"
branch: "feat/#117/guest-cart-merge-module"
assignees: ""
---

## ✨ 기능 요약

> 비회원 장바구니를 Redis 세션(24시간 TTL)에 저장하고, 로그인 사용자 장바구니로 병합하는 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 비회원 장바구니 병합 DTO 추가 (`MergeGuestCartDto`)
- [x] Cart 모듈 의존성 확장 (`Product`, `Seller` Repository)
- [x] CartService에 Redis 클라이언트 추가 (`ioredis`, lazyConnect)
- [x] 비회원 장바구니 조회 API 구현 (`GET /cart/guest`, `x-cart-key`)
- [x] 비회원 장바구니 추가 API 구현 (`POST /cart/guest`, `x-cart-key`)
- [x] 비회원 장바구니 수량 변경 API 구현 (`PATCH /cart/guest/:itemId`, `x-cart-key`)
- [x] 비회원 장바구니 항목 삭제 API 구현 (`DELETE /cart/guest/:itemId`, `x-cart-key`)
- [x] 비회원 장바구니 전체 비우기 API 구현 (`DELETE /cart/guest`, `x-cart-key`)
- [x] 로그인 후 비회원 장바구니 병합 API 구현 (`POST /cart/guest/merge`)
- [x] 장바구니 병합 후 게스트 세션 삭제 로직 구현
- [x] Redis 세션 TTL 24시간 적용 (`guest:cart:{key}`)
- [x] API 라우트 상수 확장 (`CART.GUEST*`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
