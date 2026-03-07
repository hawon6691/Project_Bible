---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PC Builder + Friend API (Step 33-34) 프론트엔드 연동"
labels: feature
issue: "[FEAT] PC Builder + Friend API (Step 33-34) 프론트엔드 연동"
commit: "feat: (#287) pc-builder+friend API 연동 및 테스트 페이지 추가"
branch: "feat/#287/pc-builder-friend-api-step"
assignees: ""
---

## ✨ 기능 요약

> 요청하신 2단계 묶음 방식으로 PC Builder API(33번), Friend API(34번)를 프론트엔드에 연동하고 수동 검증용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] PC Builder/Friend 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `PcBuildSummaryItem`, `PcBuildDetailItem`, `PcCompatibilityRuleItem`
  - `FriendListItem`, `FriendFeedItem`
- [x] PC Builder 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchMyPcBuilds`, `createPcBuild`, `fetchPcBuildDetail`, `updatePcBuild`, `removePcBuild`
  - `addPcBuildPart`, `removePcBuildPart`, `fetchPcBuildCompatibility`
  - `createPcBuildShareLink`, `fetchSharedPcBuild`, `fetchPopularPcBuilds`
  - `fetchCompatibilityRulesAdmin`, `createCompatibilityRuleAdmin`, `updateCompatibilityRuleAdmin`, `removeCompatibilityRuleAdmin`
- [x] Friend 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `requestFriend`, `acceptFriendRequest`, `rejectFriendRequest`
  - `fetchFriends`, `fetchReceivedFriendRequests`, `fetchSentFriendRequests`, `fetchFriendFeed`
  - `blockUser`, `unblockUser`, `removeFriend`
- [x] PC Builder/Friend 테스트 페이지 추가
  - `FrontEnd/src/pages/PcBuilderApiPage.tsx`
  - `FrontEnd/src/pages/FriendApiPage.tsx`
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/pc-builder-api`, `/friend-api` 경로 추가
  - 상단 메뉴 `PcBuilderAPI`, `FriendAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] PC Builder는 명세와 서버 라우트가 대부분 동일하여 그대로 반영
- [x] Friend는 서버가 `/friends` 하위 경로로 제공하며 명세와 동일한 동작을 기준으로 반영
