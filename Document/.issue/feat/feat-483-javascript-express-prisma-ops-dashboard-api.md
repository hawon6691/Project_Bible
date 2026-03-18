---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Ops Dashboard API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Ops Dashboard API 구현"
commit: "feat: (#483) JavaScript Express Prisma Ops Dashboard API 구현"
branch: "feat/#483/javascript-express-prisma-ops-dashboard-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 운영 통합 대시보드 요약 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `admin/ops-dashboard/summary` 운영 통합 요약 조회 API 추가
- [x] `health`, `searchSync`, `crawler`, `queue` 지표 집계 로직 추가
- [x] 부분 실패 시 `degraded` 상태 및 `errors` 수집 로직 추가
- [x] 경보 임계치 기반 `alerts`, `alertCount` 계산 로직 추가
- [x] 라우트 인덱스에 `ops-dashboard` 라우터 연결
- [x] README 노출 경로 요약 갱신
- [x] 대표 엔드포인트 수동 검증 완료
