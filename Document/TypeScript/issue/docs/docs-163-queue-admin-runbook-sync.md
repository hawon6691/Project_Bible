---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] Queue Admin 운영 규칙/런북 동기화"
labels: document
issue: "[DOCS] Queue Admin 운영 규칙/런북 동기화"
commit: "docs: (#163) queue-admin retry 상태 규칙과 stats 운영 절차 문서 반영"
branch: "docs/#163/queue-admin-runbook-sync"
assignees: ""
---

## 📌 관련 이슈

> 이 PR과 연관된 이슈 번호를 작성해주세요.

- #163

---

## 🧾 문서 요약

> 어떤 문서인지 한 줄로 설명해주세요.

Queue Admin의 재시도 상태 제약(`failed` 전용)과 큐 통계 조회(`stats`) 운영 절차를 API 명세/운영 런북에 반영했습니다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

- `bug/#161`로 재시도 상태 검증 로직이 변경되어 문서 불일치 가능성이 생김
- 운영자가 큐 적체를 빠르게 판단할 수 있도록 `stats` 활용 절차를 명확히 할 필요가 있음

---

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] API 명세서 Queue Admin 섹션에 재시도 상태 제약 규칙 추가
- [x] `failed` 외 상태 재시도 요청 시 `400 VALIDATION_FAILED` 반환 정책 명시
- [x] 운영 런북에 `GET /admin/queues/stats` 점검 절차 추가
- [x] 운영 런북에 재시도 상태 제약 주의사항 추가
