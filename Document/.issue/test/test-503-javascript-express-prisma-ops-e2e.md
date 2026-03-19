---
name: "🧪 Test Request"
about: 새로운 테스트 제안
title: "[TEST] JavaScript Express Prisma Ops E2E"
labels: test
assignees: ""
issue: "[TEST] JavaScript Express Prisma Ops E2E 구현"
commit: "test: (#503) JavaScript Express Prisma Ops E2E 구현"
branch: "test/#503/javascript-express-prisma-ops-e2e"
---

## 🧪 테스트 요약

> JavaScript Express Prisma 백엔드에 운영 API 묶음에 대한 E2E 검증을 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `ops.test.js` 운영 API E2E 추가
- [x] `queue-admin` 목록/통계/실패잡/재시도 검증 추가
- [x] `ops-dashboard` 요약 응답 검증 추가
- [x] `observability` metrics / traces / dashboard 검증 추가
- [x] `resilience` snapshot / policies / detail / reset 검증 추가
- [x] invalid pagination 및 non-failed retry 예외 케이스 검증 추가
- [x] `test:e2e:ops` 스크립트 추가
- [x] README 테스트 실행 가이드 갱신
- [x] `npm run test:e2e:ops` 통과 확인
- [x] `npm run test:e2e:critical` 회귀 통과 확인
- [x] ops E2E 16건 통과 확인
