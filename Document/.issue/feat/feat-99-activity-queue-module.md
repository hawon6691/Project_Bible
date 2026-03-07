---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Activity Queue 비동기 저장 구현"
labels: feature
issue: "[FEAT] Activity Queue 비동기 저장 구현"
commit: "feat: (#99) 활동 이력 비동기 저장(Bull Queue) 처리 추가"
branch: "feat/#99/activity-queue-module"
assignees: ""
---

## ✨ 기능 요약

> 활동 이력(최근 본 상품, 검색 기록)을 동기 DB 저장 대신 Bull Queue 기반 비동기 처리로 전환했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Activity 전용 큐 프로세서 추가 (`ActivityProcessor`)
- [x] Activity 모듈에 Bull Queue 등록 (`activity-log`)
- [x] 최근 본 상품 이벤트 큐 등록 로직 구현 (`enqueueRecentProduct`)
- [x] 검색 기록 이벤트 큐 등록 로직 구현 (`enqueueSearchHistory`)
- [x] 워커 저장 로직 분리 구현 (`persistRecentProduct`, `persistSearchHistory`)
- [x] 활동 기록 API를 비동기 큐 등록 방식으로 전환
- [x] 큐 재시도/백오프 옵션 적용 (attempts, fixed backoff)
- [x] API 라우트 상수 확장 (`ACTIVITY.RECENT_PRODUCT_TRACK`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
