---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] workflow_dispatch 대상 ref 지정 입력값 추가"
labels: feature
issue: "[FEAT] workflow_dispatch 대상 ref 지정 입력값 추가"
commit: "feat: (#203) 수동 CI 실행 대상 브랜치/태그(target_ref) 지정 지원"
branch: "feat/#203/workflow-dispatch-target-ref"
assignees: ""
---

## ✨ 기능 요약

> 수동 실행(`workflow_dispatch`) 시 `target_ref`를 입력받아 release-gate/perf-smoke-manual 잡이 지정된 브랜치 또는 태그에서 실행되도록 개선했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `workflow_dispatch` 입력값 추가 (`target_ref`)
- [x] `release-gate` 체크아웃 ref를 `inputs.target_ref`로 고정
- [x] `perf-smoke-manual` 체크아웃 ref를 `inputs.target_ref`로 고정
- [x] Step Summary에 대상 ref 표시 (`Target ref`)
- [x] 기존 PR/Push CI 경로 영향 없이 수동 경로만 확장


