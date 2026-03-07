---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] E2E 테스트 스위트 베이스라인 구축"
labels: feature
issue: "[FEAT] E2E 테스트 스위트 베이스라인 구축"
commit: "test: (#133) e2e 테스트 부트스트랩 및 public/auth/search 시나리오 추가"
branch: "feat/#133/e2e-test-suite-bootstrap"
assignees: ""
---

## ✨ 기능 요약

> DB 의존 없이 빠르게 실행 가능한 E2E 테스트 기반을 추가하고, Public/Auth/Search 주요 흐름 시나리오를 작성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] E2E 앱 부트스트랩 헬퍼 추가 (`ValidationPipe`, `HttpExceptionFilter`, `ResponseInterceptor`)
- [x] 테스트용 HTTP 클라이언트 유틸 추가 (내장 fetch 기반)
- [x] Public API E2E 시나리오 추가 (`/health`, `/errors/codes`)
- [x] Auth/Search E2E 시나리오 추가 (`/auth/signup`, `/search`, `/search/autocomplete`)
- [x] 유효성 검증 실패 응답 포맷 확인 테스트 추가
- [x] 성공 응답 래핑(`success/data/meta`) 검증 테스트 추가
- [x] `npm run test:e2e -- --runInBand` 통과
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
