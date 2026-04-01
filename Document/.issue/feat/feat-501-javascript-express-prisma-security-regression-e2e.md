---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Security Regression E2E"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Security Regression E2E 구현"
commit: "feat: (#501) JavaScript Express Prisma Security Regression E2E 구현"
branch: "feat/#501/javascript-express-prisma-security-regression-e2e"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 공개 문서, 인증 경계, 관리자 권한 경계를 검증하는 security regression E2E를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `security-regression.test.js` 보안 회귀 E2E 추가
- [x] 공개 문서 경로 `/docs/openapi`, `/docs/swagger` 공개 접근 검증 추가
- [x] 보호된 auth 경로의 missing auth / invalid token 검증 추가
- [x] 일반 사용자 토큰의 admin 경로 접근 차단 검증 추가
- [x] 업로드 경로의 auth gate 검증 추가
- [x] `test:e2e:security-regression` 스크립트 추가
- [x] README 테스트 실행 가이드 갱신
- [x] `npm run test:e2e:security-regression` 통과 확인
- [x] `npm run test:e2e:critical` 회귀 통과 확인
- [x] security regression E2E 10건 통과 확인


