---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] CI/E2E 운영 런북 동기화"
labels: document
issue: "[DOCS] CI/E2E 운영 런북 동기화"
commit: "docs: (#173) e2e-critical/CI 아티팩트 운영 런북 반영"
branch: "docs/#173/ci-e2e-runbook-sync"
assignees: ""
---

## 📌 관련 이슈

> 이 PR과 연관된 이슈 번호를 작성해주세요.

- #173

---

## 🧾 문서 요약

> 어떤 문서인지 한 줄로 설명해주세요.

최근 테스트 단계(#165, #171)에서 변경된 CI 아티팩트/핵심 E2E 범위를 운영 런북에 반영했습니다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

- CI 실패 시 운영자가 어디서 결과 파일을 확인해야 하는지 즉시 파악할 필요가 있음
- 핵심 E2E 검증 범위가 확장되면서 운영 체크리스트와 실제 구성이 달라질 수 있음

---

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 배포 전 체크리스트에 핵심 E2E 포함 시나리오 상세 반영
- [x] `test:e2e:critical`에 포함된 운영 API 범위 문서화
- [x] CI 아티팩트 확인 경로 추가 (`e2e-critical-report`, `perf-smoke-artifacts`)
- [x] 운영자가 즉시 확인 가능한 파일 경로 명시 (`test-results/*.json`, `perf-server.log`)
