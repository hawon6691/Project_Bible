---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 큐 운영 복구(Admin Queue Recovery) 모듈 추가"
labels: enhancement
issue: "[FEAT] 큐 운영 복구(Admin Queue Recovery) 모듈 추가"
commit: "feat: (#149) queue-admin 모듈로 실패 잡 조회/재시도/삭제 운영 API 구현"
branch: "feat/#149/queue-recovery-admin-module"
assignees: ""
---

## ✨ 기능 요약

> 운영자가 Bull 큐 실패 잡을 조회하고 재시도/삭제할 수 있는 Queue Admin 모듈을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Queue Admin 모듈/서비스/컨트롤러 추가 (`src/queue-admin/*`)
- [x] 지원 큐 목록 조회 API 구현 (`GET /admin/queues/supported`)
- [x] 실패 잡 목록 조회 API 구현 (`GET /admin/queues/:queueName/failed`)
- [x] 실패 잡 일괄 재시도 API 구현 (`POST /admin/queues/:queueName/failed/retry`)
- [x] 실패 잡 개별 재시도 API 구현 (`POST /admin/queues/:queueName/jobs/:jobId/retry`)
- [x] 잡 개별 삭제 API 구현 (`DELETE /admin/queues/:queueName/jobs/:jobId`)
- [x] 지원 큐 검증/예외 처리 로직 구현 (`activity-log`, `video-transcode`, `crawler-collect`, `search-index-sync`)
- [x] API 라우트 상수 확장 (`QUEUE_ADMIN`)
- [x] 앱 모듈 등록 (`QueueAdminModule`)
- [x] Queue Admin E2E 테스트 추가 (`test/e2e/queue-admin.e2e-spec.ts`)
- [x] API 명세 문서 반영 (`03_api-specification.md`)
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
