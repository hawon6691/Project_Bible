---
name: "🧪 Feature Request"
about: 새로운 기능 제안
title: "[TEST] workflow_dispatch 입력 조합 유효성 검증 추가"
labels: test
issue: "[TEST] workflow_dispatch 입력 조합 유효성 검증 추가"
commit: "test: (#205) 수동 실행 입력값 fail-fast 검증 잡 추가"
branch: "test/#205/workflow-dispatch-input-validation"
assignees: ""
---

## 🧪 테스트 요약

> 수동 실행 시 `run_release_gate`/`run_perf_smoke`가 모두 `false`인 잘못된 입력 조합을 초기 단계에서 실패 처리하도록 CI를 보강했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 수동 실행 입력 검증 잡 추가 (`validate-dispatch-inputs`)
- [x] `run_release_gate=false` + `run_perf_smoke=false` 조합 fail-fast 처리
- [x] 입력 검증 결과를 Step Summary에 기록
- [x] 수동 잡(`release-gate`, `perf-smoke-manual`)에 검증 잡 의존성 추가
- [x] 기존 PR/Push CI 경로 영향 없이 수동 경로만 보강
