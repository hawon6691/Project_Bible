---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] JavaScript Express Prisma CI"
labels: chore
assignees: ""
issue: "[CHORE] JavaScript Express Prisma CI 구현"
commit: "chore: (#495) JavaScript Express Prisma CI 구현"
branch: "chore/#495/javascript-express-prisma-ci"
---

## 🛠️ 작업 요약

> JavaScript Express Prisma 백엔드에 critical E2E 기반 GitHub Actions CI를 추가한다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

- JavaScript 구현체는 핵심 스모크 E2E 테스트 기반은 생겼지만, 이를 자동으로 검증하는 CI 파이프라인이 아직 없었다.
- 다른 PHP, TypeScript, Java 구현체처럼 저장소 차원의 자동 검증 흐름을 맞춰야 변경 안정성을 높일 수 있다.
- shared PostgreSQL SQL 기준으로 CI DB를 준비해, 로컬 검증과 최대한 같은 조건으로 critical E2E를 실행할 필요가 있었다.

## 📋 요구사항

- [x] `.github/workflows/javascript-express-prisma-ci.yml` GitHub Actions 워크플로 추가
- [x] `push` / `pull_request` 기준 자동 critical E2E 실행 구성 추가
- [x] `workflow_dispatch` 수동 실행 경로 추가
- [x] CI에서 PostgreSQL service 컨테이너 구성 추가
- [x] shared PostgreSQL SQL 기준 DB bootstrap 단계 추가
- [x] `npm ci`, `npm run prisma:generate`, `npm run test:e2e:critical` 실행 흐름 추가
- [x] critical E2E 로그 artifact 업로드 단계 추가
- [x] README CI 실행 흐름 요약 갱신
- [x] 로컬 `npm run test:e2e:critical` 회귀 검증 완료
- [x] critical 스모크 테스트 9건 통과 상태 유지 확인

## ✅ 산출물

- `Project_Bible/.github/workflows/javascript-express-prisma-ci.yml`
- `Project_Bible/BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/README.md`

## 검증 메모

- 로컬 `npm run test:e2e:critical` 재실행 통과
- `9 passed / 0 failed` 확인
- CI 워크플로에서 shared SQL 기반 PostgreSQL bootstrap 경로 확인

## 메모

- 이번 변경은 사용자 기능 추가가 아니라 테스트 자동화와 운영 파이프라인 정리에 초점을 둔 작업이다.
- 다음 단계는 Swagger/OpenAPI 또는 테스트 확장으로 이어가는 것이 자연스럽다.
