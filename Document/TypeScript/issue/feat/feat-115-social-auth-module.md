---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 소셜 로그인/연동 모듈 구현"
labels: feature
issue: "[FEAT] 소셜 로그인/연동 모듈 구현"
commit: "feat: (#115) 소셜 콜백/가입완료/연동/해제 API 구현"
branch: "feat/#115/social-auth-module"
assignees: ""
---

## ✨ 기능 요약

> 소셜 로그인 요청 URL, 콜백 처리, 신규 소셜 유저 가입 완료, 소셜 계정 연동/해제 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 소셜 계정 엔티티 구현 (`social_accounts`)
- [x] 소셜 제공자 enum 추가 (`google`, `naver`, `kakao`, `facebook`, `instagram`)
- [x] 소셜 인증 DTO 구현 (callback, signup-complete, link)
- [x] 소셜 로그인 URL 생성 API 구현 (`GET /auth/:provider`)
- [x] 소셜 콜백 처리 API 구현 (`POST /auth/:provider/callback`)
- [x] 신규 소셜 유저 가입 완료 API 구현 (`POST /auth/social/signup-complete`)
- [x] 소셜 계정 연동 API 구현 (`POST /auth/social/link`)
- [x] 소셜 계정 연동 해제 API 구현 (`DELETE /auth/social/:provider`)
- [x] 일반 로그인 수단 없는 계정의 마지막 소셜 해제 방지 로직 구현
- [x] 소셜 계정 연결 정보 저장/조회 로직 구현
- [x] Auth 모듈 TypeORM 등록 확장 (`SocialAccount`)
- [x] API 라우트 상수 확장 (`AUTH.SOCIAL_*`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
