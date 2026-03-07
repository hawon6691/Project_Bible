---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 상품 이미지 미존재 시 고대비 플레이스홀더 UI 적용"
labels: feature
issue: "[FEAT] 상품 이미지 미존재 시 고대비 플레이스홀더 UI 적용"
commit: "feat: (#223) no-image fallback을 검은 배경/중앙 흰 텍스트로 통일"
branch: "feat/#223/no-image-placeholder-contrast-ui"
assignees: ""
---

## ✨ 기능 요약

> 상품 이미지가 없을 때 배경과 겹쳐 보이지 않도록 검은 배경 + 중앙 흰 글씨 플레이스홀더를 적용했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 상품 리스트 카드에서 이미지 미존재 시 `NO IMAGE` 고대비 플레이스홀더 적용 (`src/components/product/ProductCard.tsx`)
- [x] 상품 상세 메인 이미지 영역에서 이미지 미존재 시 검은 배경/중앙 흰 글씨 적용 (`src/app/products/[id]/page.tsx`)
- [x] 장바구니 상품 썸네일 미존재 상태 플레이스홀더 적용 (`src/app/cart/page.tsx`)
- [x] 주문 상세 상품 썸네일 미존재 상태 플레이스홀더 적용 (`src/app/orders/[id]/page.tsx`)
- [x] 타입 체크 통과 (FrontEnd `npx tsc -p tsconfig.json --noEmit --incremental false`)
