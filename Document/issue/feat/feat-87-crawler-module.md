---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Crawler 모듈 구현"
labels: feature
issue: "[FEAT] Crawler 모듈 구현"
commit: "feat: (#87) 크롤러 작업 관리/수동 트리거/모니터링 API 구현"
branch: "feat/#87/crawler-module"
assignees: ""
---

## ✨ 기능 요약

> 판매처별 크롤러 작업 관리, 수동 수집 실행, 실행 이력 및 모니터링 통계 조회 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 크롤러 작업 엔티티 구현 (`crawler_jobs`)
- [x] 크롤러 실행 이력 엔티티 구현 (`crawler_runs`)
- [x] Crawler DTO 구현 (작업 생성/수정, 작업/실행이력 조회, 수동 트리거)
- [x] Crawler 모듈/서비스/컨트롤러 추가
- [x] 크롤러 작업 목록/생성 API 구현 (`GET/POST /crawler/admin/jobs`)
- [x] 크롤러 작업 수정/삭제 API 구현 (`PATCH/DELETE /crawler/admin/jobs/:id`)
- [x] 작업 단위 수동 수집 실행 API 구현 (`POST /crawler/admin/jobs/:id/run`)
- [x] 특정 판매처/상품 즉시 수집 API 구현 (`POST /crawler/admin/triggers`)
- [x] 크롤러 실행 이력 조회 API 구현 (`GET /crawler/admin/runs`)
- [x] 수집 상태 모니터링 통계 API 구현 (`GET /crawler/admin/monitoring`)
- [x] 수집 실행 로그 기록/성공률 집계 로직 구현
- [x] 이상치 탐지 카운트 계산 로직 추가
- [x] 앱 모듈 등록 (`CrawlerModule`)
- [x] API 라우트 상수 추가 (`CRAWLER`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
