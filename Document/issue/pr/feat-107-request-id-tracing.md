---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Request ID 기반 요청 추적"
labels: feature
issue: "[FEAT] Request ID 기반 요청 추적"
commit: "feat: (#107) 전역 Request ID 추적 및 응답 포함"
branch: "feat/#107/request-id-tracing"
assignees: ""
---

## ✨ 기능 요약

> 전역 Request ID를 발급/전파하여 로그-응답-예외를 한 키로 추적할 수 있도록 개선했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Request ID 공통 유틸 추가 (`getOrCreateRequestId`)
- [x] 클라이언트 `x-request-id` 헤더 전달 시 재사용
- [x] 미전달 시 서버 UUID 기반 Request ID 자동 발급
- [x] 모든 응답 헤더에 `X-Request-Id` 설정
- [x] HTTP 로깅 메시지에 `requestId` 포함
- [x] 예외 응답 바디에 `requestId` 포함
- [x] 성공 응답 바디에 `requestId` 포함
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
