---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Critical E2E Test Base"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Critical E2E Test Base 구현"
commit: "feat: (#493) JavaScript Express Prisma Critical E2E Test Base 구현"
branch: "feat/#493/javascript-express-prisma-critical-e2e-test-base"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 핵심 API 스모크 검증용 E2E 테스트 기반을 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `test/e2e/critical.test.js` 핵심 API 스모크 E2E 테스트 추가
- [x] `package.json`에 `test:e2e:critical` 실행 스크립트 추가
- [x] 헬스체크, docs 상태, 공개 상품 조회 검증 케이스 추가
- [x] 인증 필요 엔드포인트의 `401` 검증 케이스 추가
- [x] 일반 사용자와 관리자 권한 분기 검증 케이스 추가
- [x] `pc-builds` 사용자 목록 조회 스모크 검증 케이스 추가
- [x] 없는 이미지 variant 조회의 `404` 검증 케이스 추가
- [x] README 테스트 실행 방법 갱신
- [x] `npm run test:e2e:critical` 실행 검증 완료
- [x] 대표 스모크 테스트 9건 통과 확인
