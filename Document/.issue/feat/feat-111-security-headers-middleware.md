---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 보안 헤더 전역 미들웨어 적용"
labels: feature
issue: "[FEAT] 보안 헤더 전역 미들웨어 적용"
commit: "feat: (#111) Helmet 대체 보안 헤더 미들웨어 추가"
branch: "feat/#111/security-headers-middleware"
assignees: ""
---

## ✨ 기능 요약

> 전역 보안 헤더 미들웨어를 추가해 클릭재킹/스니핑/XSS 등 기본 웹 보안 위협에 대한 방어 설정을 적용했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 전역 보안 헤더 미들웨어 구현 (`security-headers.middleware.ts`)
- [x] `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` 적용
- [x] `Permissions-Policy`, `Cross-Origin-*` 헤더 적용
- [x] `Strict-Transport-Security` 헤더 적용
- [x] 앱 부트스트랩 시 전역 미들웨어 등록 (`main.ts`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
