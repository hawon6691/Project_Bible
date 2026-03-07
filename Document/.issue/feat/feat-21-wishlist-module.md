---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 위시리스트 모듈 구현"
labels: feature
issue: "[FEAT] 위시리스트 모듈 구현"
commit: "feat: (#21) 위시리스트 조회/토글/삭제 API 구현"
branch: "feat/#21/wishlist-module"
assignees: ""
---

## ✨ 기능 요약

> 사용자 위시리스트 조회/추가(토글)/삭제 기능을 구현하고 앱 모듈 및 라우트 상수에 연동했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 위시리스트 엔티티 구현 (`wishlists`, user-product unique)
- [x] 위시리스트 모듈/서비스/컨트롤러 추가
- [x] 내 위시리스트 조회 API 구현 (`GET /wishlist`)
- [x] 위시리스트 토글 API 구현 (`POST /wishlist/:productId`)
- [x] 위시리스트 삭제 API 구현 (`DELETE /wishlist/:productId`)
- [x] 상품 존재 여부 검증 및 예외 처리 추가
- [x] 앱 모듈 등록 (`WishlistModule`)
- [x] API 라우트 상수 추가 (`WISHLIST`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
