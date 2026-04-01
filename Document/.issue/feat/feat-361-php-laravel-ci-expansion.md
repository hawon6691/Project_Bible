---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Laravel CI 확장 및 수동 워크플로 보강"
labels: feature
assignees: ""
issue: "[FEAT] PHP Laravel CI 확장 및 수동 워크플로 보강"
commit: "feat: (#361) PHP Laravel CI 확장 및 수동 워크플로 보강"
branch: "feat/#361/php-laravel-ci-expansion"
---

## ✨ 기능 요약

TypeScript CI처럼 PHP도 자동 검증과 수동 실행 워크플로를 분리해, 품질 검사 외에 API 회귀, MySQL 통합 검증, route snapshot, release gate, live smoke를 실행할 수 있도록 GitHub Actions를 확장한다.

## 📋 요구사항

- [x] `php-laravel-ci.yml`을 TypeScript CI 구조에 맞게 확장
- [x] 자동 실행용 `quality` 잡 유지 및 정리
- [x] 자동 실행용 `api-regression` 잡 추가
- [x] 자동 실행용 `db-integration` 잡 추가
- [x] `workflow_dispatch` 입력값 검증 job 추가
- [x] 수동 실행용 `release-gate` 잡 추가
- [x] 수동 실행용 `api-regression-manual` 잡 추가
- [x] 수동 실행용 `db-integration-manual` 잡 추가
- [x] 수동 실행용 `route-snapshot-manual` 잡 추가
- [x] 수동 실행용 `live-smoke-manual` 잡 추가
- [x] 테스트 결과/route snapshot artifact 업로드 구성






