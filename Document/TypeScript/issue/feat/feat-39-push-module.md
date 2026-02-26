---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 푸시 알림 모듈 구현"
labels: feature
issue: "[FEAT] 푸시 알림 모듈 구현"
commit: "feat: (#39) 푸시 구독/해제 및 알림 설정 API 구현"
branch: "feat/#39/push-module"
assignees: ""
---

## ✨ 기능 요약

> 푸시 구독 등록/해제, 내 활성 구독 조회, 알림 설정 조회/변경 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 푸시 구독 엔티티 구현 (`push_subscriptions`)
- [x] 푸시 설정 엔티티 구현 (`push_preferences`)
- [x] 푸시 DTO 구현 (구독 등록/해제, 설정 변경)
- [x] 푸시 모듈/서비스/컨트롤러 추가
- [x] 푸시 구독 등록 API 구현 (`POST /push/subscriptions`)
- [x] 푸시 구독 해제 API 구현 (`POST /push/subscriptions/unsubscribe`)
- [x] 내 활성 구독 조회 API 구현 (`GET /push/subscriptions`)
- [x] 알림 설정 조회 API 구현 (`GET /push/preferences`)
- [x] 알림 설정 변경 API 구현 (`POST /push/preferences`)
- [x] 앱 모듈 등록 (`PushModule`)
- [x] API 라우트 상수 추가 (`PUSH`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
