---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Query API 구현"
labels: feature
issue: "[FEAT] JavaScript Query API 구현"
commit: "feat: (#509) JavaScript Query API 및 OpenAPI 반영"
branch: "feat/#509/javascript-query-api"
assignees: ""
---

## ✨ 기능 요약

> `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql`에 읽기 모델 기반 `Query API`를 추가하고, OpenAPI 문서와 critical E2E 검증에 연결했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] JavaScript Query 읽기 모델 조회 리포지토리 추가 (`src/query/query.repository.js`)
- [x] Query 목록/상세/동기화/재구축 서비스 추가 (`src/query/query.service.js`)
- [x] Query 컨트롤러 추가 (`src/query/query.controller.js`)
- [x] Query 목록 요청 검증 추가 (`src/query/query.validator.js`)
- [x] 공개 API 추가
- [x] `GET /api/v1/query/products`
- [x] `GET /api/v1/query/products/:productId`
- [x] 관리자 API 추가
- [x] `POST /api/v1/admin/query/products/:productId/sync`
- [x] `POST /api/v1/admin/query/products/rebuild`
- [x] 라우터 등록 추가 (`src/routes/query.js`, `src/routes/index.js`)
- [x] 기존 search 재색인 로직을 재사용하도록 연동 (`src/search/search.service.js` 재사용)
- [x] OpenAPI `/docs/openapi`에 Query API 경로 및 스키마 반영 (`src/docs/docs.service.js`)
- [x] critical E2E에 Query API 검증 추가 (`test/e2e/critical.test.js`)
- [x] 검증 항목 포함
- [x] Query 목록 공개 조회 확인
- [x] Query 단건 공개 조회 확인
- [x] 관리자 단건 sync 확인
- [x] 관리자 전체 rebuild 확인
- [x] `/docs/openapi` 경로 반영 확인
- [x] critical E2E 통과 (`node --env-file=.env --test test/e2e/critical.test.js`)
