---
name: "🧪 Test Request"
about: TypeScript Nest CI 워크플로 이름 정리
title: ""
labels: test
assignees: ""
issue: "[TEST] TypeScript Nest CI 워크플로 이름 정리"
commit: "test: (#419) TypeScript Nest CI 워크플로 이름 정리"
branch: "test/#419/typescript-nest-ci-workflow-rename"
---

## 🧪 테스트 요약

TypeScript Nest 백엔드 GitHub Actions 워크플로 파일명을 `nestshop-ci`에서 `typescript-nest-ci`로 정리해, 저장소 내 워크플로 명명 규칙을 더 명확하게 맞춘다.

## 📋 요구사항

- [x] `.github/workflows/nestshop-ci.yml` 파일명을 `.github/workflows/typescript-nest-ci.yml`로 변경
- [x] 워크플로 `push` / `pull_request` 트리거의 자기 참조 경로를 새 파일명으로 변경
- [x] `concurrency.group` 값을 `typescript-nest-ci-${{ github.ref }}`로 변경
- [x] 기존 TypeScript Nest 백엔드 대상 경로(`BackEnd/TypeScript/nestshop/**`)는 유지
- [x] 변경 후 Git 기준에서 삭제/추가 형태로 rename 상태가 확인되도록 정리

## 📌 결과

- TypeScript Nest CI 워크플로 파일명이 저장소 내 다른 언어 트랙 CI 이름과 비교해 더 명확한 형태로 정리되었다.
- 워크플로 트리거와 concurrency key가 새 파일명 기준으로 동작하도록 맞춰졌다.
