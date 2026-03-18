---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Crawler API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Crawler API 구현"
commit: "feat: (#475) JavaScript Express Prisma Crawler API 구현"
branch: "feat/#475/javascript-express-prisma-crawler-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 크롤러 작업 관리 및 실행 모니터링 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `crawler/admin/jobs` 크롤러 작업 목록 조회 API 추가
- [x] `crawler/admin/jobs` 크롤러 작업 생성 API 추가
- [x] `crawler/admin/jobs/:id` 크롤러 작업 수정 API 추가
- [x] `crawler/admin/jobs/:id` 크롤러 작업 삭제 API 추가
- [x] `crawler/admin/jobs/:id/run` 크롤러 작업 수동 실행 API 추가
- [x] `crawler/admin/triggers` 크롤러 수동 트리거 API 추가
- [x] `crawler/admin/runs` 크롤러 실행 이력 조회 API 추가
- [x] `crawler/admin/monitoring` 크롤러 모니터링 요약 조회 API 추가
- [x] Prisma schema에 `crawler_jobs`, `crawler_runs` 매핑 추가
- [x] 라우트 인덱스에 `crawler` 라우터 연결
- [x] README 노출 경로 요약 갱신
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
