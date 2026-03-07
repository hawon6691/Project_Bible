---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Shortform + Media API (Step 35-36) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Shortform + Media API (Step 35-36) 프론트엔드 연동"
commit: "feat: (#289) shortform+media API 연동 및 테스트 페이지 추가"
branch: "feat/#289/shortform-media-api-step"
assignees: ""
---

## ✨ 기능 요약

> 요청하신 2단계 묶음 방식으로 Shortform API(35번), Media API(36번)를 프론트엔드에 연동하고 수동 검증용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Shortform/Media 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `ShortformItem`, `ShortformCommentItem`, `MediaAssetItem`
- [x] Shortform 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `createShortform` (`POST /shortforms`, multipart/form-data)
  - `fetchShortformFeed`, `fetchShortformDetail`
  - `toggleShortformLike`
  - `createShortformComment`, `fetchShortformComments`
  - `fetchShortformRanking`
  - `removeShortform`
  - `fetchShortformTranscodeStatus`, `retryShortformTranscode`
  - `fetchUserShortforms`
- [x] Media 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `uploadMedia` (`POST /media/upload`, multipart/form-data)
  - `createMediaPresignedUrl` (`POST /media/presigned-url`)
  - `fetchMediaStreamInfo` (`GET /media/stream/:id`)
  - `removeMedia` (`DELETE /media/:id`)
  - `fetchMediaMetadata` (`GET /media/:id/metadata`)
- [x] Shortform/Media 테스트 페이지 추가
  - `FrontEnd/src/pages/ShortformApiPage.tsx`
  - `FrontEnd/src/pages/MediaApiPage.tsx`
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/shortform-api`, `/media-api` 경로 추가
  - 상단 메뉴 `ShortformAPI`, `MediaAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세는 `shortform` 섹션이지만 서버 구현은 `video` 모듈에서 `/shortforms` 라우트를 제공
- [x] 파일 업로드(숏폼/미디어)는 `multipart/form-data`가 필요해 전용 업로드 함수를 사용
