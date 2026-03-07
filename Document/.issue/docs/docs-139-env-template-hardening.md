---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[DOCS] 운영 환경 변수 템플릿 보강"
labels: documentation
issue: "[DOCS] 운영 환경 변수 템플릿 보강"
commit: "docs: (#139) .env.example 운영/테스트 플래그 누락 키 보강"
branch: "docs/#139/env-template-hardening"
assignees: ""
---

## ✨ 기능 요약

> 코드에서 실제 참조하는 환경변수를 기준으로 `.env.example` 누락 항목을 보강했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 런타임 환경 변수 추가 (`NODE_ENV`)
- [x] OAuth 리다이렉트 기본 변수 추가 (`SOCIAL_REDIRECT_URI`)
- [x] WebSocket CORS 변수 추가 (`WS_CORS_ORIGIN`)
- [x] 모의 결제 실패 플래그 추가 (`PAYMENT_GATEWAY_FORCE_FAIL`)
- [x] Bull Redis 비밀번호 사용 정책 주석 보강 (`REDIS_PASSWORD` 재사용)
- [x] 기존 섹션 구조 유지 및 설명 주석 보강
