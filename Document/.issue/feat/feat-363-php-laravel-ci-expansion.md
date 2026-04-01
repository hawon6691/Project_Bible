---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Laravel CI 확장 및 수동 워크플로 보강"
labels: feature
assignees: ""
issue: "[FEAT] PHP Laravel CI 확장 및 수동 워크플로 보강"
commit: "feat: (#363) PHP Laravel CI 확장 및 수동 워크플로 보강"
branch: "feat/#363/php-laravel-ci-expansion"
---

## ✨ 기능 요약

TypeScript CI처럼 PHP도 자동 검증과 수동 실행 워크플로를 분리해, 품질 검사 외에 API 회귀, MySQL 통합 검증, route snapshot, release gate, live smoke를 실행할 수 있도록 GitHub Actions를 확장한다.

## 📋 요구사항

- [x] `php-laravel-ci.yml`을 TypeScript CI 구조에 맞게 확장
- [x] PHP 전용 테스트 자산 추가 (`tests/scripts`, `tests/performance`)
- [x] PHP 전용 E2E 계층 추가 (`tests/E2E`)
- [x] `phpunit.xml`에 E2E testsuite 추가
- [x] `composer.json`에 `test:e2e:*` 스크립트 추가
- [x] 자동 실행용 `quality` 잡 유지 및 정리
- [x] 자동 실행용 `api-regression` 잡 추가
- [x] 자동 실행용 `db-integration` 잡 추가
- [x] `workflow_dispatch` 입력값 검증 job 추가
- [x] 수동 실행용 `release-gate` 잡 추가
- [x] 수동 실행용 `api-regression-manual` 잡 추가
- [x] 수동 실행용 `db-integration-manual` 잡 추가
- [x] 수동 실행용 `route-snapshot-manual` 잡 추가
- [x] 수동 실행용 `live-smoke-manual` 잡 추가
- [x] 수동 실행용 `perf-smoke-manual` 잡 추가
- [x] 수동 실행용 `stability-check-manual` 잡 추가
- [x] 수동 실행용 `security-regression-manual` 잡 추가
- [x] 수동 실행용 `migration-roundtrip-manual` 잡 추가
- [x] 수동 실행용 `admin-boundary-manual` 잡 추가
- [x] 수동 실행용 `rate-limit-regression-manual` 잡 추가
- [x] 수동 실행용 `dependency-failure-manual` 잡 추가
- [x] 수동 실행용 `contract-e2e-manual` 잡 추가
- [x] 수동 실행용 `perf-extended-manual` 잡 추가
- [x] 수동 실행용 `migration-validation-manual` 잡 추가
- [x] 테스트 결과/route snapshot artifact 업로드 구성
- [x] 수동 dispatch 입력값 검증 항목에 추가 manual job 옵션 반영
- [x] 잘못된 MySQL 포트로 의존성 실패를 의도적으로 검출하는 단계 추가
- [x] TypeScript 대비 남아 있던 manual CI 항목(contract/perf-extended/migration-validation) 추가
- [x] 인라인 shell 위주 검증을 전용 PHP/k6 자산 호출 방식으로 정리
- [x] GitHub Actions가 `tests/E2E`, `tests/scripts`, `tests/performance` 자산을 직접 호출하도록 정리





