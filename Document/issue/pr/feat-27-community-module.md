---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 커뮤니티 게시글 모듈 구현"
labels: feature
issue: "[FEAT] 커뮤니티 게시글 모듈 구현"
commit: "feat: (#27) 커뮤니티 게시글 CRUD 및 게시판 조회 API 구현"
branch: "feat/#27/community-module"
assignees: ""
---

## ✨ 기능 요약

> 커뮤니티 게시판/게시글 기능의 핵심 API(게시판 목록, 게시글 목록/상세, 작성/수정/삭제)를 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 커뮤니티 게시글 엔티티 구현 (`community_posts`)
- [x] 게시판 타입 enum 정의 (`REVIEW`, `QNA`, `FREE`)
- [x] 커뮤니티 DTO 구현 (생성/수정/목록 쿼리)
- [x] 커뮤니티 모듈/서비스/컨트롤러 추가
- [x] 게시판 목록 조회 API 구현 (`GET /community/boards`)
- [x] 게시글 목록 조회 API 구현 (`GET /community/posts`)
- [x] 게시글 상세 조회 API 구현 (`GET /community/posts/:id`)
- [x] 게시글 작성 API 구현 (`POST /community/posts`)
- [x] 게시글 수정 API 구현 (`PATCH /community/posts/:id`)
- [x] 게시글 삭제 API 구현 (`DELETE /community/posts/:id`)
- [x] 작성자/관리자 권한 검증 로직 추가
- [x] 조회수 증가 로직 추가 (상세 조회 시 +1)
- [x] API 라우트 상수 파일 문법 오류 수정
- [x] 앱 모듈 등록 (`CommunityModule`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
