---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] JavaScript Express Prisma Setup Ready"
labels: chore
assignees: ""
issue: "[CHORE] JavaScript Express Prisma Setup Ready 문서 작성"
commit: "chore: (#435) JavaScript Express Prisma 기본 세팅 완료"
branch: "chore/#435/javascript-express-prisma-setup-ready"
---

## 🛠️ 작업 요약

> JavaScript ORM 구현체의 기본 실행 세팅을 완료하고 기능 구현이 가능한 상태로 정리한다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

- JavaScript 백엔드 구현을 본격적으로 시작하기 전에 공통 문서와 공통 DB 자산 기준의 실행 기반이 필요했다.
- `Express + Prisma ORM` 구현체가 공통 PostgreSQL Docker, 공통 SQL, 공통 응답 형식과 연결된 상태여야 이후 기능 구현을 안정적으로 진행할 수 있었다.
- 기능 구현 이전에 의존성 설치, Prisma client 생성, DB 초기화, 앱 기동까지 실제 검증이 필요했다.

## 📋 요구사항

- [x] `.env` 파일 추가 및 공통 PostgreSQL Docker 기준 연결 정보 정리
- [x] Prisma client 재사용 구조 정리
- [x] 의존성 설치 수행
- [x] `package-lock.json` 생성
- [x] Prisma schema를 공통 PostgreSQL 스키마 기준으로 1차 정렬
- [x] `prisma generate` 실행
- [x] `db:check`, `db:init`, `db:reset` 스크립트 추가
- [x] 공통 PostgreSQL 스키마와 샘플 데이터 재적용 확인
- [x] Express 앱 기동 확인
- [x] `/health`, `/api/v1/health` 응답 확인

## ✅ 산출물

- `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/.env`
- `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/package.json`
- `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/package-lock.json`
- `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/prisma/schema.prisma`
- `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/src/app.js`
- `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/src/prisma.js`
- `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/scripts/db-check.ps1`
- `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/scripts/db-init.ps1`
- `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/scripts/db-reset.ps1`
- `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql/README.md`

## 검증 메모

- `npm install` 완료
- `npm run prisma:generate` 완료
- `npm run db:check` 완료
- `npm run db:reset` 완료
- `http://127.0.0.1:8000/health` 정상 응답 확인
- `http://127.0.0.1:8000/api/v1/health` 정상 응답 확인

## 메모

- 이번 변경은 JavaScript 구현체를 기능 개발 가능한 상태로 만드는 초기 세팅 작업이다.
- Swagger, 본격 도메인 API, 테스트, CI, 문서 세트 본편은 다음 단계에서 이어서 작업한다.
