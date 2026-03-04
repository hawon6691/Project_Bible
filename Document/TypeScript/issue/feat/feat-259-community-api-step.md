---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 커뮤니티 API (Step 15) 프론트엔드 연동"
labels: feature
issue: "[FEAT] 커뮤니티 API (Step 15) 프론트엔드 연동"
commit: "feat: (#259) boards/posts/comments API 연동 및 Community API 테스트 페이지 추가"
branch: "feat/#259/community-api-step"
assignees: ""
---

## ✨ 기능 요약

> 포인트 API(14번) 다음 단계인 커뮤니티 API(15번)를 프론트엔드에 연동하고, 수동 검증용 Community API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 커뮤니티 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `BoardItem`
  - `PostSummaryItem`
  - `PostDetailItem`
  - `PostCommentItem`
- [x] 커뮤니티 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchBoards`
  - `fetchBoardPosts`
  - `fetchPost`
  - `createBoardPost`
  - `updatePost`
  - `removePost`
  - `togglePostLike`
  - `fetchPostComments`
  - `createPostComment`
  - `removeComment`
- [x] 커뮤니티 API 테스트 페이지 추가 (`FrontEnd/src/pages/CommunityApiPage.tsx`)
  - 게시판 목록 조회
  - 게시글 목록/상세 조회
  - 게시글 작성/수정/삭제
  - 게시글 좋아요 토글
  - 댓글 목록/작성/삭제
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/community-api` 경로 추가
  - 상단 메뉴 `CommunityAPI` 링크 추가

## ⚠ 검증 메모

- [ ] `npm run build` 전체 통과  
  - 현재 저장소 기준으로 기존 타입/의존성 이슈(`react-router-dom` 타입/`import.meta.env`)가 있어 전체 빌드가 실패하며, 이번 작업 파일 외 기존 오류로 확인됨
