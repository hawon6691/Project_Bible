---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[TEST] Nestshop CI 파이프라인 구축"
labels: test
issue: "[TEST] Nestshop CI 파이프라인 구축"
commit: "test: (#135) github actions nestshop ci (lint/tsc/test/e2e/build) 추가"
branch: "test/#135/ci-pipeline"
assignees: ""
---

## ✨ 기능 요약

> Nestshop 백엔드에 대해 PR/Push 시 자동으로 정적 검증, 테스트, 빌드를 수행하는 GitHub Actions CI를 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Nestshop 전용 CI 워크플로우 파일 추가 (`.github/workflows/nestshop-ci.yml`)
- [x] 트리거 범위 설정 (PR/Push, `TypeScript/nestshop/**` 변경 시)
- [x] Node 20 + npm cache 설정
- [x] 의존성 설치 단계 추가 (`npm ci`)
- [x] 린트 검증 단계 추가 (`npm run lint:check`)
- [x] 타입 체크 단계 추가 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] 단위 테스트 단계 추가 (`npm test -- --runInBand`)
- [x] E2E 테스트 단계 추가 (`npm run test:e2e -- --runInBand`)
- [x] 빌드 단계 추가 (`npm run build`)
- [x] CI용 lint non-fix 스크립트 추가 (`lint:check`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
