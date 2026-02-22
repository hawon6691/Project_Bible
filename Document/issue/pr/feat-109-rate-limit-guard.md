---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 전역 Rate Limit 가드 구현"
labels: feature
issue: "[FEAT] 전역 Rate Limit 가드 구현"
commit: "feat: (#109) 인메모리 레이트리밋 가드 적용"
branch: "feat/#109/rate-limit-guard"
assignees: ""
---

## ✨ 기능 요약

> 전역 인메모리 Rate Limit 가드를 추가하여 일반 API 분당 60회, 인증 API 분당 10회 제한을 적용했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 전역 RateLimitGuard 구현 (`common/guards/rate-limit.guard.ts`)
- [x] 고정 윈도우 기반 요청 카운팅 로직 구현 (60초)
- [x] 일반 API 분당 60회 제한 적용
- [x] 인증 API(`/auth`) 분당 10회 제한 적용
- [x] 제한 초과 시 `429 Too Many Requests` + `COMMON_004` 예외 반환
- [x] Swagger/docs 경로 제한 제외 처리
- [x] 전역 가드 체인에 RateLimitGuard 추가 (`CommonModule`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
