---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Ops Dashboard 경보(alerts) 지표 추가"
labels: feature
issue: "[FEAT] Ops Dashboard 경보(alerts) 지표 추가"
commit: "feat: (#175) ops-dashboard alerts/alertCount 파생 지표 추가"
branch: "feat/#175/ops-dashboard-alerts"
assignees: ""
---

## ✨ 기능 요약

> 운영 통합 대시보드 응답에 경보 목록(`alerts`)과 경보 개수(`alertCount`)를 추가해 이상 징후를 즉시 파악할 수 있도록 개선했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Ops Dashboard 요약 응답에 `alerts`, `alertCount` 필드 추가
- [x] 헬스 비정상/검색 동기화 실패/크롤러 실패/큐 실패 기준 경보 생성 로직 추가
- [x] 부분 실패 발생 시 `partial_failure` 경보 생성 로직 추가
- [x] Ops Dashboard 정상 응답 E2E 스펙 갱신 (`ops-dashboard.e2e-spec.ts`)
- [x] Ops Dashboard 부분 실패 E2E 스펙 갱신 (`ops-dashboard-resilience.e2e-spec.ts`)
- [x] API 명세 문서 반영 (`02_api-specification.md`)
- [x] 운영 런북 점검 절차 반영 (`02_operations-runbook.md`)

