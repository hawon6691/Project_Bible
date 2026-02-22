---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 역경매 모듈 구현"
labels: feature
issue: "[FEAT] 역경매 모듈 구현"
commit: "feat: (#79) 역경매/입찰/낙찰 선택 API 구현"
branch: "feat/#79/reverse-auction-module"
assignees: ""
---

## ✨ 기능 요약

> 역경매 등록/조회, 판매자 입찰, 등록자 낙찰 선택, 경매 취소, 입찰 수정/취소 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 역경매 엔티티 구현 (`auctions`)
- [x] 역경매 입찰 엔티티 구현 (`auction_bids`)
- [x] 경매/입찰 DTO 구현 (생성, 목록 쿼리, 입찰 수정)
- [x] Auction 모듈/서비스/컨트롤러 추가
- [x] 역경매 등록 API 구현 (`POST /auctions`)
- [x] 역경매 목록 API 구현 (`GET /auctions`)
- [x] 역경매 상세 API 구현 (`GET /auctions/:id`)
- [x] 입찰 등록 API 구현 (`POST /auctions/:id/bids`)
- [x] 낙찰 선택 API 구현 (`PATCH /auctions/:id/bids/:bidId/select`)
- [x] 역경매 취소 API 구현 (`DELETE /auctions/:id`)
- [x] 입찰 수정 API 구현 (`PATCH /auctions/:id/bids/:bidId`)
- [x] 입찰 취소 API 구현 (`DELETE /auctions/:id/bids/:bidId`)
- [x] 경매 상태(OPEN/CLOSED/CANCELLED) 검증 로직 구현
- [x] 경매 등록자/입찰자 소유권 검증 로직 구현
- [x] 앱 모듈 등록 (`AuctionModule`)
- [x] API 라우트 상수 추가 (`AUCTION`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
