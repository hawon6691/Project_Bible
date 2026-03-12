---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] Requirements Language And Test Spec Alignment"
labels: document
assignees: ""
issue: "[DOCS] Requirements Language And Test Spec Alignment 문서 작성"
commit: "docs: (#413) Requirements Language And Test Spec Alignment 문서 작성"
branch: "docs/#413/requirements-language-and-test-spec-alignment"
---

## 🧾 문서 요약

> 요구사항, 언어 분기 기준, 테스트 공통 명세 문서를 재정렬하고 PHP/TypeScript 테스트 보강 결과까지 반영한다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

- `01_requirements.md`에는 순수 요구사항만 남기고, 언어별 기술 스택 정보는 `04_language.md`로 분리할 필요가 있다.
- `04_language.md`는 단순 스택 표가 아니라 실제 프로젝트를 몇 개 생성해야 하는지 판단하는 기준 문서가 되어야 한다.
- `05` 문서는 단순 테스트 목록보다 언어 공통 테스트 명세서 역할이 더 적합하다.
- `TypeScript`와 `PHP`의 실제 테스트 구현을 기준으로 공통 테스트 축을 맞춘 결과를 문서에 반영해야 한다.

## 📋 작업 항목

- [x] `Document/01_requirements.md` 재정렬
- [x] 요구사항 문서에서 언어별 기술 스택 제거
- [x] 요구사항 문서에서 구현체 기준 메모 제거
- [x] `Document/04_language.md` 재작성
- [x] `Raw SQL` / `ORM` 기준 분리 원칙 반영
- [x] 프레임워크 수와 빌드/패키지 도구 수에 따른 프로젝트 수 계산 규칙 반영
- [x] `Java`, `Kotlin`, `TypeScript`, `PHP` 프로젝트 분기 기준 정리
- [x] `C++`, `C#`, `Go`, `Rust`, `Ruby`, `Lua`까지 확장 반영
- [x] `Document/05_test-code-list.md`를 `Document/05_test-specification.md`로 변경
- [x] `05_test-specification.md`를 언어 공통 테스트 명세 문서로 재작성
- [x] `Document/06_php-typescript-test-gap-checklist.md` 참조 경로 갱신
- [x] `TypeScript` 통합 테스트 보강
- [x] `PHP` 운영 E2E 보강
- [x] 테스트 보강 결과를 `05`, `06` 문서에 반영

## ✅ 산출물

- `Document/01_requirements.md`
- `Document/04_language.md`
- `Document/05_test-specification.md`
- `Document/06_php-typescript-test-gap-checklist.md`

## 검증 메모

- TypeScript 추가 통합 테스트 배치 통과
- PHP 추가 E2E 테스트 배치 통과
- `05`, `06` 문서 기준으로 PHP와 TypeScript 테스트 축 정렬 완료

## 메모

- `01_requirements.md`는 순수 요구사항 문서 역할만 유지한다.
- `04_language.md`는 언어별 기술 선택 문서가 아니라 프로젝트 분기 규칙 문서로 재정의했다.
- `05_test-specification.md`는 향후 다른 언어 구현체에도 그대로 적용할 공통 테스트 명세서다.
