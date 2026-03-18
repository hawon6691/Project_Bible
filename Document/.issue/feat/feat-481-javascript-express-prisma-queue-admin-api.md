---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Queue Admin API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Queue Admin API 구현"
commit: "feat: (#481) JavaScript Express Prisma Queue Admin API 구현"
branch: "feat/#481/javascript-express-prisma-queue-admin-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 큐 운영 복구 및 실패 Job 관리 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `admin/queues/supported` 운영 대상 큐 목록 조회 API 추가
- [x] `admin/queues/stats` 큐 상태 통계 조회 API 추가
- [x] `admin/queues/auto-retry` 전체 큐 실패 Job 자동 재시도 API 추가
- [x] `admin/queues/:queueName/failed` 실패 Job 목록 조회 API 추가
- [x] `admin/queues/:queueName/failed/retry` 실패 Job 일괄 재시도 API 추가
- [x] `admin/queues/:queueName/jobs/:jobId/retry` 실패 Job 개별 재시도 API 추가
- [x] `admin/queues/:queueName/jobs/:jobId` Job 개별 삭제 API 추가
- [x] 라우트 인덱스에 `queue-admin` 라우터 연결
- [x] README 노출 경로 요약 갱신
- [x] 대표 엔드포인트 수동 검증 완료
