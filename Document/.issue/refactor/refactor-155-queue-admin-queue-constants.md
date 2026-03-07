---
name: "♻️ Refactor Request"
about: 기능 수정 제안
title: "[REFACT] Queue Admin 큐 이름 상수화"
labels: refactor
issue: "[REFACT] Queue Admin 큐 이름 상수화"
commit: "refactor: (#155) queue-admin 큐 이름 상수/타입 통합으로 중복 제거"
branch: "refactor/#155/queue-admin-queue-constants"
assignees: ""
---

## ♻️ 수정 요약

> Queue Admin 모듈의 큐 이름 문자열 중복을 공통 상수/타입으로 통합했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 공통 큐 이름 상수 파일 추가 (`src/common/constants/queue-names.ts`)
- [x] 관리 대상 큐 목록 상수/타입 정의 (`MANAGED_QUEUE_NAMES`, `ManagedQueueName`)
- [x] Queue Admin 모듈의 `registerQueue` 문자열 상수로 치환
- [x] Queue Admin 서비스의 `@InjectQueue` 및 큐 매핑 상수로 치환
- [x] 지원 큐 검증 로직 상수 기반으로 정리
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] Queue Admin E2E 통과 (`npm run test:e2e -- queue-admin.e2e-spec.ts --runInBand`)
