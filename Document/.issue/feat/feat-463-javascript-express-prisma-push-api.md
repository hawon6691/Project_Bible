---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Push API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Push API 구현"
commit: "feat: (#463) JavaScript Express Prisma Push API 구현"
branch: "feat/#463/javascript-express-prisma-push-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 푸시 구독 및 알림 설정 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `push/subscriptions` 푸시 구독 등록 API 추가
- [x] `push/subscriptions/unsubscribe` 푸시 구독 해제 API 추가
- [x] `push/subscriptions` 내 활성 푸시 구독 목록 조회 API 추가
- [x] `push/preferences` 내 푸시 알림 설정 조회 API 추가
- [x] `push/preferences` 내 푸시 알림 설정 변경 API 추가
- [x] Prisma schema에 `push_subscriptions`, `push_preferences` 매핑 추가
- [x] 라우트 인덱스에 `push` 라우터 연결
- [x] README 노출 경로 요약 갱신
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
