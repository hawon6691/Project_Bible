---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Health Check 모듈 구현"
labels: feature
issue: "[FEAT] Health Check 모듈 구현"
commit: "feat: (#85) DB/Redis/Elasticsearch 헬스체크 API 구현"
branch: "feat/#85/health-module"
assignees: ""
---

## ✨ 기능 요약

> `/health` 엔드포인트에서 DB, Redis, Elasticsearch 상태를 통합 조회하는 Health Check 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Health 모듈/서비스/컨트롤러 추가
- [x] 헬스 체크 공개 API 구현 (`GET /health`)
- [x] DB 상태 확인 로직 구현 (`SELECT 1`)
- [x] Redis 상태 확인 로직 구현 (TCP 연결 기반)
- [x] Elasticsearch 상태 확인 로직 구현 (`/_cluster/health`)
- [x] 서비스 전체 상태 집계 로직 구현 (`up`/`degraded`/`down`)
- [x] 응답에 세부 체크 결과(`database`, `redis`, `elasticsearch`) 및 `checkedAt` 포함
- [x] 앱 모듈 등록 (`HealthModule`)
- [x] API 라우트 상수 재사용 (`HEALTH.BASE`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
