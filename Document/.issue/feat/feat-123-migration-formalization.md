---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] TypeORM 마이그레이션 정식화 및 crawler_runs 스키마 반영"
labels: feature
issue: "[FEAT] TypeORM 마이그레이션 정식화 및 crawler_runs 스키마 반영"
commit: "feat: (#123) migration data-source 구성 및 crawler_runs 파이프라인 스키마 마이그레이션 추가"
branch: "feat/#123/migration-formalization"
assignees: ""
---

## ✨ 기능 요약

> TypeORM CLI용 데이터소스를 추가하고, 크롤러 워커 파이프라인 스키마 변경을 마이그레이션으로 정식 반영했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] TypeORM CLI 전용 `DataSource` 파일 추가 (`src/database/data-source.ts`)
- [x] migration 스크립트에 DataSource 경로 연결 (`package.json`)
- [x] `crawler_runs` 상태 enum 값 확장 마이그레이션 추가 (`QUEUED`, `PROCESSING`)
- [x] `crawler_runs` 수집 옵션 컬럼 추가 마이그레이션 반영 (`collect_price`, `collect_spec`, `detect_anomaly`)
- [x] `crawler_runs.status` 인덱스 추가 (`idx_crawler_runs_status`)
- [x] down 마이그레이션에서 컬럼/인덱스 롤백 처리
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
