---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] Language Matrix And Project Naming Alignment"
labels: document
assignees: ""
issue: "[DOCS] Language Matrix And Project Naming Alignment 문서 작성"
commit: "docs: (#431) Language Matrix And Project Naming Alignment 문서 작성"
branch: "docs/#431/language-matrix-and-project-naming-alignment"
---

## 🧾 문서 요약

> `Document/04_language.md`를 표 중심으로 재작성하고, 언어별 Raw SQL / ORM 평균 스택과 생성 프로젝트 이름 기준을 구체화한다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

- 기존 `04_language.md`는 설명이 길고, 실제로 몇 개의 구현 프로젝트를 만들어야 하는지 한눈에 보기 어려웠다.
- `Raw SQL`과 `ORM` 비교 축은 유지하되, 언어별 평균적인 기술 선택지를 더 구체적으로 적을 필요가 있었다.
- 생성 프로젝트 이름도 `rawsql`, `orm` 같은 추상 표현보다 실제 기술명이 드러나도록 정리할 필요가 있었다.
- 현재 Java, TypeScript, PHP 구현체 이름 정리와도 문서 기준을 맞출 필요가 있었다.

## 📋 작업 항목

- [x] `Document/04_language.md` 전체 재작성
- [x] 장문 설명을 표 중심 구조로 축소
- [x] 프로젝트 수 계산 규칙 표 정리
- [x] 언어별 프레임워크/빌드도구/Raw SQL/ORM 평균 스택 표 정리
- [x] `JavaScript`, `Python` 포함 언어별 비교 축 재정리
- [x] 생성 프로젝트 이름을 실제 기술명 기준으로 변경
- [x] `rawsql`, `orm` 같은 추상 이름 대신 `jpa`, `typeorm`, `eloquentorm`, `dapper`, `gorm` 등으로 구체화
- [x] `Raw SQL` / `ORM` 구분이 PostgreSQL, MySQL 모두를 포함하는 데이터 접근 방식 기준임을 정리

## ✅ 산출물

- `Document/04_language.md`

## 검증 메모

- 문서가 표 중심 구조로 압축됨
- 언어별 평균 스택이 실제 기술명 기준으로 정리됨
- 생성 프로젝트 이름이 스택 식별 가능 형태로 변경됨
- `Raw SQL` / `ORM`은 DB 엔진이 아니라 데이터 접근 방식 구분이라는 기준 확인

## 메모

- 현재 문서는 DB 엔진 비교 문서가 아니라 언어별 구현체 분기 문서다.
- 따라서 `PostgreSQL` / `MySQL`은 프로젝트 추가 분기 축으로 다루지 않고, `Raw SQL` / `ORM` 내부에서 모두 지원 가능한 대상으로 본다.
