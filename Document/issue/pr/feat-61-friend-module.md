---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 친구/팔로우 모듈 구현"
labels: feature
issue: "[FEAT] 친구/팔로우 모듈 구현"
commit: "feat: (#61) 친구 요청/수락/거절/차단/피드 API 구현"
branch: "feat/#61/friend-module"
assignees: ""
---

## ✨ 기능 요약

> 친구 신청/수락/거절, 친구 목록/요청 목록/활동 피드, 유저 차단/해제, 친구 삭제 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 친구 관계 엔티티 구현 (`friendships`)
- [x] 친구 차단 엔티티 구현 (`friend_blocks`)
- [x] 친구 활동 피드 엔티티 구현 (`friend_activities`)
- [x] 친구 목록 페이징 DTO 구현 (`FriendPaginationQueryDto`)
- [x] Friend 모듈/서비스/컨트롤러 추가
- [x] 친구 신청 API 구현 (`POST /friends/request/:userId`)
- [x] 친구 수락 API 구현 (`PATCH /friends/request/:friendshipId/accept`)
- [x] 친구 거절 API 구현 (`PATCH /friends/request/:friendshipId/reject`)
- [x] 내 친구 목록 API 구현 (`GET /friends`)
- [x] 받은 요청 목록 API 구현 (`GET /friends/requests/received`)
- [x] 보낸 요청 목록 API 구현 (`GET /friends/requests/sent`)
- [x] 친구 활동 피드 API 구현 (`GET /friends/feed`)
- [x] 유저 차단/차단 해제 API 구현 (`POST/DELETE /friends/block/:userId`)
- [x] 친구 삭제 API 구현 (`DELETE /friends/:userId`)
- [x] 친구 수락 시 활동 피드 생성 로직 구현
- [x] 요청/차단/자기자신 처리 검증 로직 구현
- [x] 앱 모듈 등록 (`FriendModule`)
- [x] API 라우트 상수 추가 (`FRIEND`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
