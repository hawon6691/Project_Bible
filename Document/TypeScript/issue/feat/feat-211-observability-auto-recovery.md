---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 관측성 대시보드 표준화 및 자동복구 정책 고도화"
labels: feature
issue: "[FEAT] 관측성 대시보드 표준화 및 자동복구 정책 고도화"
commit: "feat: (#211) observability 모듈 및 queue/circuit 자동복구 고도화"
branch: "feat/#211/observability-auto-recovery"
assignees: ""
---

## ✨ 기능 요약

> 메트릭/트레이스 기반 관측성 대시보드를 추가하고, 큐 자동 재시도 및 서킷브레이커 자동 튜닝 정책을 도입했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Observability 모듈/컨트롤러/서비스 추가 (`src/observability/*`)
- [x] 요청 트레이스 수집 인터셉터 추가 (`ObservabilityTraceInterceptor`)
- [x] 관측성 메트릭 조회 API 추가 (`GET /admin/observability/metrics`)
- [x] 최근 트레이스 조회 API 추가 (`GET /admin/observability/traces`)
- [x] 관측성 통합 대시보드 API 추가 (`GET /admin/observability/dashboard`)
- [x] Queue 자동 재시도 DTO/서비스/컨트롤러 추가 (`POST /admin/queues/auto-retry`)
- [x] Resilience 자동 튜닝 정책 추가 (`RESILIENCE_AUTO_TUNE_*`)
- [x] Resilience 정책 조회 API 추가 (`GET /resilience/circuit-breakers/policies`)
- [x] Rate Limit 회귀 E2E/관리자 권한 경계 E2E/관측성 E2E 보강
- [x] 실서비스 smoke 및 migration roundtrip 수동 CI 경로 보강
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
