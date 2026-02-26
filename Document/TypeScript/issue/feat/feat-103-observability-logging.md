---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 로깅/예외 관측성 강화"
labels: feature
issue: "[FEAT] 로깅/예외 관측성 강화"
commit: "feat: (#103) HTTP 로깅 레벨 분리 및 예외 로그 구조화"
branch: "feat/#103/observability-logging"
assignees: ""
---

## ✨ 기능 요약

> 환경별 로그 레벨 분리, HTTP 응답 상태 기반 로그 레벨링, 예외 로그 구조화를 통해 운영 관측성을 강화했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 앱 부트스트랩 환경별 로거 레벨 분리 구현 (`main.ts`)
- [x] HTTP 인터셉터 응답 상태 코드 기반 로그 레벨 분리 (`debug/warn/error`)
- [x] 인터셉터 로그에 유저 식별자(`userId`) 포함
- [x] 전역 예외 필터 HTTP 예외 구조화 로그 구현
- [x] 4xx/5xx 예외 로그 레벨 분리 (`warn/error`)
- [x] 예외 응답 포맷 표준 유지 (`success/errorCode/message/timestamp/path`)
- [x] Validation 배열 메시지 통합 처리 유지
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
