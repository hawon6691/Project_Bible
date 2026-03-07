---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Reverse Auction + Compare API (Step 43-44) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Reverse Auction + Compare API (Step 43-44) 프론트엔드 연동"
commit: "feat: (#297) auction+compare API 연동 및 테스트 페이지 추가"
branch: "feat/#297/auction-compare-api-step"
assignees: ""
---

## ✨ 기능 요약

> 요청하신 2단계 묶음 방식으로 Reverse Auction API(43번), Compare Bar API(44번)를 프론트엔드에 연동하고 수동 검증용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Auction/Compare 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `AuctionSummaryItem`, `AuctionDetailItem`, `AuctionBidItem`
  - `CompareListItem`, `CompareDetailResult`
- [x] Auction 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `createAuction`, `fetchAuctions`, `fetchAuctionDetail`
  - `createAuctionBid`, `updateAuctionBid`, `removeAuctionBid`
  - `selectAuctionBid`, `cancelAuction`
- [x] Compare 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `addCompareItem`, `removeCompareItem`
  - `fetchCompareList`, `fetchCompareDetail`
- [x] Auction/Compare 테스트 페이지 추가
  - `FrontEnd/src/pages/AuctionApiPage.tsx`
  - `FrontEnd/src/pages/CompareApiPage.tsx`
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/auction-api`, `/compare-api` 경로 추가
  - 상단 메뉴 `AuctionAPI`, `CompareAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] Auction의 입찰 생성/수정/삭제는 Seller 권한이 필요하므로 테스트 시 Seller 토큰 기준으로 검증 필요
- [x] Compare API는 인증 없이 사용하며 `x-compare-key` 헤더 기반으로 비교함 세션을 분리하므로 해당 헤더 입력을 지원
