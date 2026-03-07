---
name: "🐛 Bug Report"
about: 버그 신고
title: "[BUG] 장바구니(cart) 응답 지연(최대 31.9s) 개선"
labels: bug
issue: "[BUG] 장바구니(cart) 응답 지연(최대 31.9s) 개선"
commit: "bug: (#221) cart 조회/변경 경로 경량화 및 프론트 중복 요청 완화"
branch: "bug/#221/cart-response-latency-optimization"
assignees: ""
---

## 🐛 버그 요약

> 장바구니 진입/수량 변경 시 응답이 매우 느려져(최대 31.9초) 사용자 상호작용이 지연되는 문제를 개선했습니다.

## 🔍 재현 방법

> 버그를 재현하는 순서를 작성해주세요.

1. 로그인 후 장바구니 페이지 진입
2. 상품 수량을 연속으로 변경하거나 항목 추가/삭제 반복
3. 로딩 상태가 길게 지속되며 응답 체감이 크게 느려짐

## 🤔 예상 동작

> 원래 어떻게 동작해야 하나요?

장바구니 진입/수량 변경/삭제는 수 초 이내로 응답하고, 연속 조작에서도 UI가 즉시 반응해야 합니다.

## 😱 실제 동작

> 실제로는 어떻게 동작하고 있나요?

장바구니 요청이 느리게 응답하며, 연속 수량 조작 시 요청이 누적되어 체감 지연이 커졌습니다.

## 🌍 환경 정보

> 버그가 발생한 환경을 작성해주세요.

- OS: Windows
- Node.js 버전: (로컬 개발 환경)
- 브랜치: `bug/#221/cart-response-latency-optimization`

## 💡 원인 추정

> 원인이 무엇인지 추측이 있다면 작성해주세요.

- `cart` 조회가 `relations` 기반 로딩으로 불필요한 엔티티 로딩 비용 발생
- 수량 변경 시 프런트에서 연속 호출이 누적되어 서버/클라이언트 모두 대기 증가
- 변경 후 재조회 대기(`await fetchCart`)로 버튼 체감 지연 가중

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `getCart`를 경량 단일 조인 쿼리로 전환하고 필요한 컬럼만 조회 (`src/cart/cart.service.ts`)
- [x] 사용자별 단기 메모리 캐시(TTL 1.5s) 추가로 중복 조회 흡수 (`src/cart/cart.service.ts`)
- [x] `updateQuantity`를 `findOne + save`에서 단건 `update`로 전환 (`src/cart/cart.service.ts`)
- [x] `removeItem`을 `findOne + remove`에서 단건 `delete`로 전환 (`src/cart/cart.service.ts`)
- [x] 장바구니 변경(add/update/remove/clear/merge) 시 캐시 무효화 처리 (`src/cart/cart.service.ts`)
- [x] 프런트 수량 변경 디바운스(250ms) 적용으로 연속 조작 시 요청 폭주 완화 (`src/lib/stores/cartStore.ts`)
- [x] 프런트 수량/삭제 로직에 낙관적 업데이트 및 실패 복구 적용 (`src/lib/stores/cartStore.ts`)
- [x] 성공 요청 HTTP 로그 기본 비활성화로 개발 환경 I/O 오버헤드 완화 (`src/common/interceptors/logging.interceptor.ts`)
- [x] 타입 체크 통과 (FrontEnd/BackEnd `npx tsc -p tsconfig.json --noEmit --incremental false`)

## ✅ 완료 조건

> 이 버그가 해결됐다고 판단하는 기준을 작성해주세요.

- [x] 장바구니 화면 진입 및 수량 조작 체감 속도 개선 확인
- [x] 장바구니 관련 경로 타입 체크 오류 없음
