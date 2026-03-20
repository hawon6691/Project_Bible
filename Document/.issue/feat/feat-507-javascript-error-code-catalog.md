---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Error Code Catalog API 구현"
labels: feature
issue: "[FEAT] JavaScript Error Code Catalog API 구현"
commit: "feat: (#507) JavaScript Error Code Catalog 공개 API 및 OpenAPI 반영"
branch: "feat/#507/javascript-error-code-catalog"
assignees: ""
---

## ✨ 기능 요약

> `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql`에 공개 `Error Code Catalog` API를 추가하고, OpenAPI 문서와 critical E2E 검증에 연결했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] JavaScript 공통 에러 코드 카탈로그 소스 분리 (`src/error-codes/error-code.catalog.js`)
- [x] 에러 코드 목록/단건 조회 서비스 추가 (`src/error-codes/error-code.service.js`)
- [x] 공개 API 추가
- [x] `GET /api/v1/errors/codes`
- [x] `GET /api/v1/errors/codes/:key`
- [x] 라우터 등록 추가 (`src/routes/errors.js`, `src/routes/index.js`)
- [x] 기존 `HttpError` 유틸이 카탈로그 메시지를 재사용하도록 정리 (`src/utils/http-error.js`)
- [x] 404/500 공통 응답이 카탈로그 메시지와 정합되도록 보강 (`src/app.js`)
- [x] OpenAPI `/docs/openapi`에 Error Code Catalog 경로 및 스키마 반영 (`src/docs/docs.service.js`)
- [x] critical E2E에 Error Code API 검증 추가 (`test/e2e/critical.test.js`)
- [x] 검증 항목 포함
- [x] `/errors/codes` 목록 구조 확인
- [x] `/errors/codes/:key` 정상 키 조회 확인
- [x] `/errors/codes/:key` 미존재 키 `null` 응답 확인
- [x] `/docs/openapi` 경로 반영 확인
- [x] critical E2E 통과 (`node --env-file=.env --test test/e2e/critical.test.js`)
