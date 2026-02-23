---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[TEST] CI 품질 게이트 분리 및 핵심 E2E 고정"
labels: test
issue: "[TEST] CI 품질 게이트 분리 및 핵심 E2E 고정"
commit: "test: (#145) quality/e2e-critical 잡 분리 및 핵심 E2E 스크립트 추가"
branch: "test/#145/ci-quality-gates"
assignees: ""
---

## ✨ 기능 요약

> GitHub Actions CI를 품질 검증 단계와 핵심 E2E 단계로 분리하고, 핵심 E2E 세트를 고정 실행하도록 구성했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 핵심 E2E 실행 스크립트 추가 (`test:e2e:critical`)
- [x] CI `quality` 잡에서 lint/type/unit/build 검증 유지
- [x] CI `e2e-critical` 잡 추가 및 `quality` 성공 후 실행
- [x] 핵심 E2E 대상 고정 (`auth-search`, `public-api`, `admin-platform`)
- [x] 동일 브랜치 중복 실행 취소를 위한 concurrency 설정 추가
