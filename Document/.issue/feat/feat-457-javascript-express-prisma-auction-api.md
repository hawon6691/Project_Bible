---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Auction API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Auction API 구현"
commit: "feat: (#457) JavaScript Express Prisma Auction API 구현"
branch: "feat/#457/javascript-express-prisma-auction-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 역경매 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `GET /auctions` 역경매 목록 API 추가
- [x] `GET /auctions/:id` 역경매 상세 및 입찰 목록 API 추가
- [x] `POST /auctions` 역경매 등록 API 추가
- [x] `POST /auctions/:id/bids` 입찰 등록 API 추가
- [x] `PATCH /auctions/:id/bids/:bidId/select` 낙찰 선택 API 추가
- [x] `DELETE /auctions/:id` 역경매 취소 API 추가
- [x] `PATCH /auctions/:id/bids/:bidId` 입찰 수정 API 추가
- [x] `DELETE /auctions/:id/bids/:bidId` 입찰 삭제 API 추가
- [x] Prisma schema에 `auction_status`, `auctions`, `bids` 매핑 추가
- [x] 라우트 인덱스에 `auctions` 라우터 연결
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
