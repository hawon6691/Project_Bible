---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] PBShop 다중 언어 기준 문서 정렬"
labels: document
assignees: ""
issue: "[DOCS] PBShop 다중 언어 기준 문서 정렬"
commit: "docs: (#313) PBShop 다중 언어 기준 문서 정렬"
branch: "docs/#313/multi-language-reference-document-alignment"
---

## 🧾 문서 요약

> `01_requirements.md`, `02_api-specification.md`, `03_erd.md`에 PBShop 기준 구현과 다중 언어 확장 원칙을 명확히 반영한다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

- TypeScript 구현을 기준 구현(Reference Implementation)으로 고정하고, 이후 언어별 백엔드 구현이 어떤 문서를 공통 기준으로 따라야 하는지 명확히 할 필요가 있다.
- 기존 문서는 `NestShop` 표기와 단일 기술 스택 중심 표현이 남아 있어, 현재 프로젝트 방향인 `PBShop` 다중 언어 확장 구조와 완전히 맞지 않았다.
- 요구사항 자체는 유지하면서도, 언어별 구현 계획과 기준 문서를 분리해 읽을 수 있게 정리할 필요가 있었다.

## 📋 요구사항

- [x] `Document/01_requirements.md` 프로젝트 개요 정리
  - [x] 프로젝트명을 `NestShop`에서 `PBShop`으로 변경
  - [x] 공통 개요(`프로젝트명`, `아키텍처`, `인증 방식`) 유지
  - [x] `1.1 언어별 기술 스택` 표 추가
  - [x] 언어별 스택을 `프레임워크/런타임`, `ORM/데이터 접근`, `데이터베이스`, `실시간 통신`, `큐/비동기`, `인증/보안`, `검색/스토리지/기타`로 분리
  - [x] `TypeScript`를 기준 구현으로, `PHP`, `JavaScript`, `Java`, `Python`, `Kotlin`을 예정 스택으로 구분
  - [x] 기능 요구사항 본문은 변경하지 않음
- [x] `Document/02_api-specification.md` 기준 구현 문구 보강
  - [x] `TypeScript + NestJS + PostgreSQL`를 기준 구현으로 명시
  - [x] `PHP`, `JavaScript`, `Java`, `Python`, `Kotlin` 백엔드가 동일 API 명세를 공통 계약으로 사용한다는 설명 추가
  - [x] 기존 API 엔드포인트/응답 명세 본문은 유지
- [x] `Document/03_erd.md` 기준 스키마 문구 보강
  - [x] `TypeScript + PostgreSQL`를 기준 스키마로 명시
  - [x] 다른 언어 백엔드도 동일 ERD 구조를 기준으로 구현한다는 설명 추가
  - [x] 기존 ERD 본문과 테이블 목록은 유지
- [x] 다중 언어 확장 방향이 문서 전반에서 일관되게 읽히도록 정리

## 📌 참고

- 기준 구현(Reference Implementation): `TypeScript + NestJS + PostgreSQL`
- 후속 구현 예정 언어: `PHP`, `JavaScript`, `Java`, `Python`, `Kotlin`
