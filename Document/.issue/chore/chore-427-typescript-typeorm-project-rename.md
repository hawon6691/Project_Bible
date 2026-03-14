---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] TypeScript TypeORM Project Rename"
labels: chore
assignees: ""
issue: "[CHORE] TypeScript TypeORM Project Rename 문서 작성"
commit: "chore: (#427) TypeScript TypeORM 프로젝트 이름 정리"
branch: "chore/#427/typescript-typeorm-project-rename"
---

## 🛠️ 작업 요약

> TypeScript 구현체와 CI 이름을 `nestshop_typeorm`, `typescript-typeorm-ci` 기준으로 정리한다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

- TypeScript 백엔드 구현은 Nest 기반이지만, 실제 데이터 접근 방식은 TypeORM 기준이다.
- 구현체 식별자는 프레임워크보다 저장소 구조와 ORM 구분에 더 직접적으로 연결되는 이름이 필요했다.
- 기존 `nestshop`, `typescript-nest-ci` 표기는 구현 성격을 충분히 설명하지 못했다.

## 📋 요구사항

- [x] TypeScript 프로젝트 폴더명을 `nestshop_typeorm`으로 변경
- [x] CI 파일명을 `typescript-typeorm-ci.yml`로 변경
- [x] workflow 표시명을 `PBShop TypeScript TypeORM CI`로 수정
- [x] concurrency group을 `typescript-typeorm-ci` 기준으로 수정
- [x] workflow 내부 `working-directory` 및 artifact 경로 수정
- [x] `BackEnd/TypeScript/README.md` 경로 및 이름 수정
- [x] 루트 README의 TypeScript 백엔드 경로 수정
- [x] TypeScript 문서 세트의 구현체 경로 수정
- [x] 공통 CI/테스트/ERD 문서의 TypeScript 구현체 경로 수정
- [x] `package.json`, `package-lock.json` 이름을 `nestshop_typeorm`으로 수정
- [x] 이전 `typescript-nest-ci`, `BackEnd/TypeScript/nestshop` 참조 제거 확인

## ✅ 산출물

- `.github/workflows/typescript-typeorm-ci.yml`
- `BackEnd/TypeScript/nestshop_typeorm`
- `BackEnd/TypeScript/README.md`
- `README.md`
- `Document/06_ci-specification.md`
- `Document/05_test-specification.md`
- `Document/03_erd.md`
- `Document/TypeScript/01_folder-structure.md`
- `Document/TypeScript/02_operations-runbook.md`
- `Document/TypeScript/03_release-checklist.md`
- `Document/TypeScript/04_completion-report.md`
- `Document/TypeScript/05_pre-release-final-gate.md`
- `BackEnd/TypeScript/nestshop_typeorm/package.json`
- `BackEnd/TypeScript/nestshop_typeorm/package-lock.json`

## 검증 메모

- 프로젝트 폴더명이 `nestshop_typeorm`으로 변경됨
- CI 파일명이 `typescript-typeorm-ci.yml`로 정리됨
- workflow 내부 경로가 새 폴더 기준으로 수정됨
- README와 문서의 구현체 경로가 새 이름 기준으로 수정됨
- 기존 `typescript-nest-ci`, `BackEnd/TypeScript/nestshop` 참조가 남지 않도록 검색 확인함

## 메모

- 이번 변경은 서비스 브랜드명 `nestshop`을 바꾸는 작업이 아니라, 구현체 식별자와 CI 이름을 TypeORM 기준으로 정리하는 작업이다.
- DB 이름, 샘플 데이터 이메일, 프론트엔드 키 이름 같은 서비스 레벨 `nestshop` 문자열은 rename 범위에 포함하지 않는다.
