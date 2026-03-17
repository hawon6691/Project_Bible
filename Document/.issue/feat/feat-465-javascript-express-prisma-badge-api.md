---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Badge API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Badge API 구현"
commit: "feat: (#465) JavaScript Express Prisma Badge API 구현"
branch: "feat/#465/javascript-express-prisma-badge-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 배지 조회 및 관리자 배지 관리 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `badges` 전체 배지 목록 조회 API 추가
- [x] `badges/me` 내 배지 목록 조회 API 추가
- [x] `users/:id/badges` 특정 유저 배지 조회 API 추가
- [x] `admin/badges` 배지 생성 API 추가
- [x] `admin/badges/:id` 배지 수정 API 추가
- [x] `admin/badges/:id` 배지 삭제 API 추가
- [x] `admin/badges/:id/grant` 수동 배지 부여 API 추가
- [x] `admin/badges/:id/revoke/:userId` 배지 회수 API 추가
- [x] Prisma schema에 `badges`, `user_badges` 매핑 추가
- [x] 라우트 인덱스에 `badges` 라우터 연결
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
