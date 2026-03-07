---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 숏폼 모듈 구현"
labels: feature
issue: "[FEAT] 숏폼 모듈 구현"
commit: "feat: (#63) 숏폼 업로드/피드/좋아요/댓글/랭킹 API 구현"
branch: "feat/#63/shortform-module"
assignees: ""
---

## ✨ 기능 요약

> 숏폼 업로드, 피드/상세 조회, 좋아요 토글, 댓글 작성/조회, 랭킹 조회, 삭제, 유저별 목록 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 숏폼 엔티티 구현 (`shortforms`)
- [x] 숏폼 좋아요 엔티티 구현 (`shortform_likes`)
- [x] 숏폼 댓글 엔티티 구현 (`shortform_comments`)
- [x] 숏폼-상품 매핑 엔티티 구현 (`shortform_products`)
- [x] 숏폼 DTO 구현 (업로드/피드/댓글/랭킹)
- [x] Video 모듈/서비스/컨트롤러 추가
- [x] 숏폼 업로드 API 구현 (`POST /shortforms`)
- [x] 숏폼 피드 API 구현 (`GET /shortforms`)
- [x] 숏폼 상세 조회 API 구현 (`GET /shortforms/:id`) 및 조회수 증가
- [x] 좋아요 토글 API 구현 (`POST /shortforms/:id/like`)
- [x] 댓글 작성/조회 API 구현 (`POST /shortforms/:id/comments`, `GET /shortforms/:id/comments`)
- [x] 숏폼 랭킹 API 구현 (`GET /shortforms/ranking/list`)
- [x] 숏폼 삭제 API 구현 (`DELETE /shortforms/:id`)
- [x] 특정 유저 숏폼 조회 API 구현 (`GET /shortforms/user/:userId`)
- [x] 동영상 파일 타입/크기 및 길이(최대 60초) 검증 로직 구현
- [x] 앱 모듈 등록 (`VideoModule`)
- [x] API 라우트 상수 추가 (`SHORTFORM`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
