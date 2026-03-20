---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] Java Maven JPA Project Rename"
labels: chore
assignees: ""
issue: "[CHORE] Java Maven JPA Project Rename 문서 작성"
commit: "chore: (#425) Java Maven JPA 프로젝트 이름 정리"
branch: "chore/#425/java-maven-jpa-project-rename"
---

## 🛠️ 작업 요약

> Java Maven ORM 구현체의 이름과 경로를 `maven-jpa` 기준으로 정리한다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

- Java Maven 구현은 ORM, 정확히는 JPA 기반이므로 단순 `maven` 표기보다 `maven-jpa`가 더 정확하다.
- 워크플로, 문서, README, 프로젝트 경로까지 같은 기준으로 정렬해야 관리가 쉬워진다.
- 기존 `springshop_maven` 경로와 `java-maven-ci` 표기는 구현 성격을 충분히 설명하지 못했다.

## 📋 요구사항

- [x] Java CI 워크플로 이름을 `java-maven-jpa-ci` 기준으로 정리
- [x] 워크플로 표시명과 concurrency group 수정
- [x] Java Maven JPA 프로젝트 폴더명을 `java-spring-maven-jpa-postgresql`로 변경
- [x] `BackEnd/Java/README.md` 경로 및 명칭 수정
- [x] Java 문서 세트의 구현체 경로 표기 수정
- [x] Java 완료 이슈 문서의 명칭 및 경로 표기 수정
- [x] 이전 `springshop_maven` 참조 제거 확인

## ✅ 산출물

- `.github/workflows/java-spring-maven-jpa-postgresql-ci.yml`
- `BackEnd/Java/java-spring-maven-jpa-postgresql`
- `BackEnd/Java/README.md`
- `Document/Java/01_folder-structure.md`
- `Document/Java/02_runbook.md`
- `Document/Java/03_implementation-status.md`
- `Document/Java/04_completion-report.md`
- `Document/Java/05_pre-release-final-gate.md`
- `Document/Java/06_requirements-api-gap-analysis.md`
- `Document/Java/language-api-specification.md`
- `Document/.issue/docs/docs-423-java-maven-jpa-completion-alignment.md`

## 검증 메모

- 프로젝트 폴더명이 `java-spring-maven-jpa-postgresql`로 변경됨
- 워크플로 파일명이 `java-spring-maven-jpa-postgresql-ci.yml`로 정리됨
- 문서와 README의 경로 참조가 새 이름 기준으로 수정됨
- 기존 `springshop_maven` 참조가 남지 않도록 검색 확인함

## 메모

- 이번 변경은 구현 방식을 바꾸는 작업이 아니라 명칭과 경로를 실제 구현 성격에 맞게 정렬하는 작업이다.
- Gradle 트랙은 이번 rename 범위에 포함하지 않는다.
