---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[TEST] CI 성능 스모크 게이트 추가"
labels: test
issue: "[TEST] CI 성능 스모크 게이트 추가"
commit: "test: (#147) perf mock server + k6 smoke CI 게이트 추가"
branch: "test/#147/performance-smoke-ci-gate"
assignees: ""
---

## ✨ 기능 요약

> GitHub Actions CI에 k6 기반 성능 스모크 게이트를 추가하고, 인프라 의존성 없이 실행 가능한 mock perf server를 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 성능 스모크용 mock 서버 추가 (`test/performance/mock-perf-server.ts`)
- [x] mock 서버 실행 스크립트 추가 (`test:perf:mock-server`)
- [x] CI에 `perf-smoke` 잡 추가 (quality 성공 후 실행)
- [x] CI에서 mock 서버 기동/헬스 체크 대기 로직 추가
- [x] CI에서 Docker k6로 smoke 시나리오 실행 (`smoke.perf.js`)
- [x] 기존 품질/핵심 E2E 게이트와 병행 동작 유지
