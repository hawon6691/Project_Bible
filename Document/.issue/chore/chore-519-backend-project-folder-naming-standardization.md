---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] Backend Project Folder Naming Standardization"
labels: chore
assignees: ""
issue: "[CHORE] Backend 프로젝트 폴더명 규칙 통일"
commit: "chore: (#519) backend 프로젝트 폴더명 규칙 통일"
branch: "chore/#519/backend-project-folder-naming-standardization"
---

## 🛠️ 작업 요약

> 백엔드 구현체 폴더명을 `<language>-<framework>-<build>-<dataaccess>-<db>` 규칙으로 통일하는 유지보수 작업입니다.

문서, 워크플로, 구현체 경로를 같은 규칙으로 정리한다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

언어별 폴더명이 제각각이라 CLI 자동화와 폴더 탐색 시 예측성이 떨어졌다.

`rawsql`, `orm` 같은 큰 분류보다 `jpa`, `typeorm`, `prisma`, `eloquent`, `exposeddao`처럼 실제 접근 기술명을 쓰는 편이 관리에 더 적합하다.

## 📋 요구사항

> 수행해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `Document/04_language.md`에 `<language>-<framework>-<build>-<dataaccess>-<db>` 규칙을 추가하고 생성 프로젝트 예시를 정리한다.
- [x] Java, JavaScript, TypeScript, PHP, Kotlin 구현체 폴더명과 관련 README, 문서, GitHub Actions 경로 참조를 새 규칙으로 수정한다.
- [x] JavaScript, TypeScript 패키지명과 Kotlin Gradle 프로젝트명 등 일부 식별자를 새 규칙에 맞춰 정리한다.
- [x] 기존 폴더명은 Git 기준 삭제, 새 폴더명은 추가 상태로 남아 있어 후속 `git add -A` 정리가 필요함을 기록한다.
- [ ] Java, Java Gradle, Kotlin 내부 패키지명과 앱 식별자 같은 서비스 레벨 문자열 cleanup은 후속 이슈로 분리한다.
