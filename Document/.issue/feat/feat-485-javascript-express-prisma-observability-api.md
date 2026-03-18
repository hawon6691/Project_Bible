---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Observability API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Observability API 구현"
commit: "feat: (#485) JavaScript Express Prisma Observability API 구현"
branch: "feat/#485/javascript-express-prisma-observability-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 관측 지표, 요청 추적, 운영 대시보드 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `admin/observability/metrics` 관측 지표 조회 API 추가
- [x] `admin/observability/traces` 요청 추적 목록 조회 API 추가
- [x] `admin/observability/dashboard` 관측 대시보드 조회 API 추가
- [x] 요청 단위 `x-request-id` 생성 및 HTTP trace 기록 미들웨어 추가
- [x] queue, resilience, crawler, search, ops-dashboard 연계 관측 요약 로직 추가
- [x] 라우트 인덱스에 `observability` 라우터 연결
- [x] README 노출 경로 요약 갱신
- [x] 앱 로딩 검증 완료
- [x] 대표 엔드포인트 수동 검증 완료
