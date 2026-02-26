---
name: "🧪 Feature Request"
about: 새로운 기능 제안
title: "[TEST] CI 수동 실행 Release Gate 잡 추가"
labels: test
issue: "[TEST] CI 수동 실행 Release Gate 잡 추가"
commit: "test: (#195) workflow_dispatch 기반 release-gate 잡 구성"
branch: "test/#195/ci-workflow-dispatch-release-gate"
assignees: ""
---

## 🧪 테스트 요약

> GitHub Actions에서 수동 트리거로 릴리스 게이트 검증을 실행할 수 있도록 `release-gate` 잡을 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `workflow_dispatch` 트리거 추가
- [x] 수동 실행 입력값 추가 (`run_release_gate`)
- [x] 수동 실행 시 기존 `quality/e2e-critical/perf-smoke` 잡 비활성화 분기 추가
- [x] `release-gate` 잡 추가 및 `npm run test:release:gate` 실행 구성
- [x] 기존 PR/Push CI 경로 유지
