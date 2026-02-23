---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] 운영 런북 문서 추가"
labels: document
issue: "[DOCS] 운영 런북 문서 추가"
commit: "docs: (#151) 배포/장애 대응 운영 런북 문서 작성"
branch: "docs/#151/operations-runbook"
assignees: ""
---
## 📌 관련 이슈

> 이 PR과 연관된 이슈 번호를 작성해주세요.

- #151

---

## 🧾 문서 요약

> 어떤 문서인지 한 줄로 설명해주세요.

운영/배포/장애 대응에 사용할 `nestshop` 백엔드 운영 런북 문서를 추가했습니다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

- 기능 구현 범위가 확장되면서 장애 지점(DB/Redis/ES/Queue/FFmpeg)이 늘어남
- 운영자가 동일한 절차로 1차 대응할 수 있는 기준 문서가 필요함
- 복구 API(`queue-admin`, `resilience`, `search outbox`) 사용 방법을 한 곳에 정리할 필요가 있음

---

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 운영 런북 문서 신규 작성 (`Document/05_operations-runbook.md`)
- [x] 운영 환경 기본 체크 항목 정리 (`/health`, 검색/큐/서킷브레이커 상태)
- [x] 서비스 기동 순서 및 배포 전 체크리스트 정리
- [x] 장애 분류별 1차 대응 절차 정리 (API/검색/큐/서킷브레이커)
- [x] 큐 운영 복구 API 사용 절차 정리 (`/admin/queues/*`)
- [x] 검색 Outbox 재동기화 절차 정리 (`/search/admin/index/outbox/*`)
- [x] 크롤러/트랜스코딩 장애 대응 시나리오 정리
- [x] 롤백 기준 및 후속 조치 체크리스트 정리
- [x] 운영자 실행 명령 모음 추가 (typecheck/e2e/perf smoke)
