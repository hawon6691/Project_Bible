---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 운영 회귀 E2E 스위트 스크립트 추가"
labels: feature
issue: "[FEAT] 운영 회귀 E2E 스위트 스크립트 추가"
commit: "feat: (#189) 운영 API 회귀 검증용 test:e2e:ops 스크립트 추가"
branch: "feat/#189/ops-regression-suite"
assignees: ""
---

## ✨ 기능 요약

> 운영 안정화 검증을 빠르게 수행할 수 있도록 운영 API 중심 E2E 묶음 스크립트를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 운영 회귀 E2E 스크립트 추가 (`test:e2e:ops`)
- [x] 스크립트에 Admin Platform/Queue Admin/Ops Dashboard 시나리오 포함
- [x] 릴리스 체크리스트에 운영 회귀 스크립트 항목 추가 (`03_release-checklist.md`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] 운영 회귀 E2E 통과 (`npm run test:e2e:ops`)



