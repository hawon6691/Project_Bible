---
name: "🧪 Feature Request"
about: 새로운 기능 제안
title: "[TEST] CI 테스트 결과 아티팩트 업로드 강화"
labels: test
issue: "[TEST] CI 테스트 결과 아티팩트 업로드 강화"
commit: "test: (#165) e2e/perf-smoke 결과 아티팩트 업로드 추가"
branch: "test/#165/ci-artifact-reporting"
assignees: ""
---

## 🧪 테스트 요약

> CI 실패 원인 파악 속도를 높이기 위해 e2e/perf-smoke 결과 파일을 아티팩트로 업로드하도록 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `e2e-critical` 실행 결과 JSON 리포트 생성 (`test-results/e2e-critical-report.json`)
- [x] `e2e-critical` 잡에 결과 아티팩트 업로드 단계 추가
- [x] `perf-smoke` 실행 결과 요약 JSON 생성 (`test-results/perf-smoke-summary.json`)
- [x] `perf-smoke` 잡에 요약/서버 로그 아티팩트 업로드 단계 추가
- [x] 아티팩트 업로드 조건을 `if: always()`로 설정해 실패 시에도 수집되도록 구성
