---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] JavaScript Express Prisma Swagger OpenAPI"
labels: chore
assignees: ""
issue: "[CHORE] JavaScript Express Prisma Swagger OpenAPI 구현"
commit: "chore: (#497) JavaScript Express Prisma Swagger OpenAPI 구현"
branch: "chore/#497/javascript-express-prisma-swagger-openapi"
---

## 🛠️ 작업 요약

> JavaScript Express Prisma 백엔드에 OpenAPI JSON과 Swagger UI 노출 경로를 추가하고 docs-status를 실제 상태로 갱신한다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

- JavaScript 구현체는 기능, 기본 테스트, CI는 갖춰졌지만 API 문서 노출 경로가 아직 `pending` 상태였다.
- 다른 구현체처럼 `/docs/openapi`, `/docs/swagger`를 실제로 열어 문서 접근성을 맞출 필요가 있었다.
- `docs-status`를 placeholder에서 실제 운영 가능한 상태로 바꾸고, critical E2E에서도 문서 경로를 검증해야 했다.

## 📋 요구사항

- [x] `/docs/openapi` OpenAPI JSON 응답 추가
- [x] `/docs/swagger` Swagger UI 노출 경로 추가
- [x] `/docs/swagger-ui/index.html` Swagger UI HTML 노출 추가
- [x] 현재 Express 라우트 기준 OpenAPI `paths` 자동 수집 로직 추가
- [x] `docs-status`를 `pending`에서 `available` 상태로 갱신
- [x] README에 Swagger/OpenAPI 접근 경로 추가
- [x] critical E2E에 `docs/openapi`, `docs/swagger` 검증 케이스 추가
- [x] 앱 부팅 회귀 확인 완료
- [x] `npm run test:e2e:critical` 실행 검증 완료
- [x] critical 스모크 테스트 11건 통과 확인

## ✅ 산출물

- `Project_Bible/BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/src/docs/docs.service.js`
- `Project_Bible/BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/src/docs/docs.controller.js`
- `Project_Bible/BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/src/app.js`
- `Project_Bible/BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/src/routes/index.js`
- `Project_Bible/BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/src/health/health.service.js`
- `Project_Bible/BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/test/e2e/critical.test.js`
- `Project_Bible/BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/README.md`

## 검증 메모

- 앱 부팅 `router-ok` 확인
- `/docs/openapi` 응답 확인
- `/docs/swagger` 응답 확인
- `npm run test:e2e:critical` 재실행 통과
- `11 passed / 0 failed` 확인

## 메모

- 이번 변경은 비즈니스 API 추가보다는 문서 노출과 운영 편의성 정리에 초점을 둔 작업이다.
- 다음 단계는 테스트 확장과 그에 맞는 CI 확장으로 이어가는 것이 자연스럽다.
