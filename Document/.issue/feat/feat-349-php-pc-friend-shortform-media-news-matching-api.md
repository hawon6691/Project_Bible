---
name: "✨ Feature Request"
about: "새 기능 추가 및 구현 작업"
title: "[FEAT] PHP PC Builder Friend Shortform Media News Matching API 구현"
labels: ["feature"]
issue: "[FEAT] PHP PC Builder Friend Shortform Media News Matching API 구현"
commit: "feat: (#349) PHP PC Builder Friend Shortform Media News Matching API 구현"
branch: "feat/#349/php-pc-friend-shortform-media-news-matching-api"
---

## ✨ 기능 요약
PHP Laravel 백엔드에 PC Builder, Friend, Shortform, Media, News, Matching API를 추가해 PBShop 공통 API 명세를 확장한다.

## 📋 요구사항
- [x] PC Builder API 구현
  - [x] `GET /api/v1/pc-builds`
  - [x] `POST /api/v1/pc-builds`
  - [x] `GET /api/v1/pc-builds/{id}`
  - [x] `PATCH /api/v1/pc-builds/{id}`
  - [x] `DELETE /api/v1/pc-builds/{id}`
  - [x] `POST /api/v1/pc-builds/{id}/parts`
  - [x] `DELETE /api/v1/pc-builds/{id}/parts/{partId}`
  - [x] `GET /api/v1/pc-builds/{id}/compatibility`
  - [x] `GET /api/v1/pc-builds/{id}/share`
  - [x] `GET /api/v1/pc-builds/shared/{shareCode}`
  - [x] `GET /api/v1/pc-builds/popular`
  - [x] `GET/POST/PATCH/DELETE /api/v1/admin/compatibility-rules`
- [x] Friend API 구현
  - [x] 친구 요청/수락/거절
  - [x] 친구 목록/요청 목록/활동 피드
  - [x] 친구 차단/해제/삭제
- [x] Shortform API 구현
  - [x] 업로드/피드/상세
  - [x] 좋아요/댓글
  - [x] 랭킹/트랜스코딩 상태/재시도
  - [x] 유저별 숏폼 목록/삭제
- [x] Media API 구현
  - [x] 업로드
  - [x] pre-signed URL 발급
  - [x] 스트림 메타 응답
  - [x] 메타데이터 조회/삭제
- [x] News API 구현
  - [x] 목록/카테고리/상세
  - [x] 관리자 작성/수정/삭제
  - [x] 뉴스 카테고리 생성/삭제
- [x] Matching API 구현
  - [x] 대기 목록
  - [x] 승인/거절
  - [x] 자동 매칭
  - [x] 통계
- [x] 라우트 등록 (`routes/api_v1.php`)
- [x] Feature 테스트 추가 (`tests/Feature/Api/PcFriendShortformMediaNewsMatchingApiTest.php`)
- [x] 라우트/테스트 검증 통과
