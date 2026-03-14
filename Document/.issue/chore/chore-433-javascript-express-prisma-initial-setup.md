---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] JavaScript Express Prisma Initial Setup"
labels: chore
assignees: ""
issue: "[CHORE] JavaScript Express Prisma Initial Setup 문서 작성"
commit: "chore: (#433) JavaScript Express Prisma 초기 세팅 정리"
branch: "chore/#433/javascript-express-prisma-initial-setup"
---

## 🛠️ 작업 요약

> JavaScript ORM 구현체의 초기 세팅을 `Express + Prisma ORM`과 공통 DB 자산 기준으로 정리한다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

- JavaScript 백엔드 구현을 다른 언어와 같은 기준으로 시작하기 위한 최소 실행 기반이 필요했다.
- 공통 문서 `01~06` 범위를 벗어나지 않도록 프로젝트 이름, 기본 환경 변수, 공통 응답 형식, 공통 DB 연결 기준을 먼저 맞출 필요가 있었다.
- `Database/docker`, `Database/postgresql` 자산을 기준으로 이후 Prisma schema, API, 테스트, CI 작업을 이어갈 수 있어야 했다.

## 📋 요구사항

- [x] `package.json` 프로젝트 이름을 `javascript-express-prisma` 기준으로 정리
- [x] 공통 Docker DB 실행 스크립트 추가
- [x] `.env.example`를 공통 PostgreSQL Docker 기준으로 정리
- [x] Express 기본 앱에 공통 응답 envelope 적용
- [x] `/health`, `/api/v1/health`, `/api/v1/categories`, `/api/v1/products`, `/api/v1/docs-status` 기본 라우트 정리
- [x] 404, 500 기본 에러 응답 정리
- [x] JavaScript 문서 폴더에 다음 단계 체크리스트 문서 추가
- [x] `Document/JavaScript/README.md`에 진행 순서 문서 링크 추가

## ✅ 산출물

- `BackEnd/JavaScript/expressshop_prismaorm/package.json`
- `BackEnd/JavaScript/expressshop_prismaorm/.env.example`
- `BackEnd/JavaScript/expressshop_prismaorm/src/app.js`
- `BackEnd/JavaScript/expressshop_prismaorm/README.md`
- `Document/JavaScript/README.md`
- `Document/JavaScript/00_next-steps-checklist.md`

## 검증 메모

- 프로젝트 이름이 `javascript-express-prisma` 기준으로 정리됨
- 공통 Docker PostgreSQL 접속 정보가 `.env.example`에 반영됨
- Express 기본 라우트가 공통 응답 envelope 형식으로 정리됨
- 공통 DB 자산을 기준으로 다음 단계 작업 순서 문서가 추가됨
- `npm install`, Prisma generate, Docker 실행, SQL import, Swagger 연결은 아직 진행하지 않음

## 메모

- 이번 변경은 기능 구현 단계가 아니라 JavaScript ORM 구현체의 시작 세팅을 공통 기준에 맞춰 정리하는 작업이다.
- 이후 구현은 `Database` 폴더의 공통 SQL과 Docker 자산을 기준으로 이어간다.
