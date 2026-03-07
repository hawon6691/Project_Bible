---
name: "🧪 Feature Request"
about: 새로운 기능 제안
title: "[TEST] Release Gate 결과 아티팩트 업로드 추가"
labels: test
issue: "[TEST] Release Gate 결과 아티팩트 업로드 추가"
commit: "test: (#197) release-gate 실행 결과 JSON 아티팩트 업로드 구성"
branch: "test/#197/release-gate-artifact-report"
assignees: ""
---

## 🧪 테스트 요약

> 수동 `release-gate` 실행 시 핵심/운영 E2E 결과 JSON을 아티팩트로 업로드하도록 CI를 확장했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 릴리스 게이트 리포트 스크립트 추가 (`test:release:gate:report`)
- [x] 리포트 파일 생성 경로 통일 (`test-results/release-gate-*.json`)
- [x] `release-gate` 잡 실행 명령을 리포트 스크립트로 전환
- [x] `release-gate-report` 아티팩트 업로드 단계 추가
- [x] 기존 PR/Push CI 동작 영향 없이 수동 실행 경로만 확장
