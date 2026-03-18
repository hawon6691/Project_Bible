---
name: "🧪 Test Request"
about: 새로운 테스트 제안
title: "[TEST] JavaScript Express Prisma Contract Admin E2E"
labels: test
assignees: ""
issue: "[TEST] JavaScript Express Prisma Contract Admin E2E 구현"
commit: "test: (#499) JavaScript Express Prisma Contract Admin E2E 구현"
branch: "test/#499/javascript-express-prisma-contract-admin-e2e"
---

## 🧪 테스트 요약

> JavaScript Express Prisma 백엔드에 공개 API 계약 검증과 관리자 권한 경계 검증 E2E를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 공통 E2E harness 추출
- [x] `critical.test.js`를 공통 harness 기반으로 정리
- [x] `contract.test.js` 공개 읽기 API 계약 검증 추가
- [x] `admin-boundary.test.js` 관리자 권한 경계 검증 추가
- [x] `test:e2e:contract` 스크립트 추가
- [x] `test:e2e:admin-boundary` 스크립트 추가
- [x] `test:e2e:platform` 통합 실행 스크립트 추가
- [x] README 테스트 실행 가이드 갱신
- [x] `npm run test:e2e:critical` 통과 확인
- [x] `npm run test:e2e:contract` 통과 확인
- [x] `npm run test:e2e:admin-boundary` 통과 확인
- [x] `npm run test:e2e:platform` 통과 확인
