---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] workflow_dispatch 수동 perf-smoke 실행 경로 추가"
labels: feature
issue: "[FEAT] workflow_dispatch 수동 perf-smoke 실행 경로 추가"
commit: "feat: (#201) CI 수동 실행에서 perf-smoke 선택 트리거 추가"
branch: "feat/#201/workflow-dispatch-perf-smoke"
assignees: ""
---

## ✨ 기능 요약

> GitHub Actions 수동 실행 시 `run_perf_smoke` 입력값으로 perf-smoke를 선택적으로 실행할 수 있도록 확장했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `workflow_dispatch` 입력값 추가 (`run_perf_smoke`)
- [x] 수동 실행 전용 perf-smoke 잡 추가 (`perf-smoke-manual`)
- [x] 수동 perf-smoke 아티팩트 업로드 단계 추가 (`perf-smoke-manual-artifacts`)
- [x] 수동 perf-smoke Step Summary 작성 단계 추가
- [x] 기존 PR/Push CI 경로 영향 없이 수동 경로만 확장


