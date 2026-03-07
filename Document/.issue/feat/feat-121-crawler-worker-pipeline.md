---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Crawler 워커 기반 실제 수집 파이프라인 구현"
labels: feature
issue: "[FEAT] Crawler 워커 기반 실제 수집 파이프라인 구현"
commit: "feat: (#121) crawler queue worker와 가격/스펙 upsert 파이프라인 적용"
branch: "feat/#121/crawler-worker-pipeline"
assignees: ""
---

## ✨ 기능 요약

> 크롤러 실행을 Bull Queue 워커로 전환하고, 실행 시 가격/스펙 데이터를 실제 DB에 수집(upsert)하도록 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Crawler 실행 상태 enum 확장 (`QUEUED`, `PROCESSING`, `SUCCESS`, `FAILED`)
- [x] `crawler_runs`에 수집 옵션 컬럼 추가 (`collectPrice`, `collectSpec`, `detectAnomaly`)
- [x] `crawler-collect` Bull Queue 등록
- [x] 크롤러 워커 프로세서 추가 (`CrawlerProcessor`)
- [x] 작업/수동 트리거 시 실행 이력 생성 후 큐 등록 로직 적용
- [x] 워커에서 실행 상태 전이 처리 (`QUEUED -> PROCESSING -> SUCCESS/FAILED`)
- [x] 가격 수집 로직 구현 (`price_entries` upsert, `crawledAt` 갱신)
- [x] 스펙 수집 로직 구현 (`product_specs` upsert)
- [x] 이상치 카운트 계산 로직 반영 (기준가 대비 20% 이상 변동)
- [x] 모니터링 통계에 대기/처리중 건수 반영
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
