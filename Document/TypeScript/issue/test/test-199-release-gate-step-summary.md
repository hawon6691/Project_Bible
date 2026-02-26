---
name: "🧪 Feature Request"
about: 새로운 기능 제안
title: "[TEST] Release Gate 실행 요약(Step Summary) 추가"
labels: test
issue: "[TEST] Release Gate 실행 요약(Step Summary) 추가"
commit: "test: (#199) release-gate 실행 결과를 GITHUB_STEP_SUMMARY에 기록"
branch: "test/#199/release-gate-step-summary"
assignees: ""
---

## 🧪 테스트 요약

> 수동 `release-gate` 실행 시 결과를 GitHub Actions Summary에 자동 기록하도록 개선했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `release-gate` 실행 스텝에 `id` 부여 (`run_release_gate`)
- [x] Step Summary 작성 단계 추가 (`Write Release Gate Summary`)
- [x] 요약 정보에 잡 상태/게이트 단계 상태/아티팩트명/실행 URL 포함
- [x] 실패 상황에서도 요약이 남도록 `if: always()` 적용
- [x] 기존 수동 실행/아티팩트 업로드 흐름 유지
