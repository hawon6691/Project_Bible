---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] Ops Dashboard 경보 임계치 운영 문서 반영"
labels: document
issue: "[DOCS] Ops Dashboard 경보 임계치 운영 문서 반영"
commit: "docs: (#179) ops alert threshold 환경변수 운영 가이드 반영"
branch: "docs/#179/ops-alert-threshold-runbook"
assignees: ""
---

## 📌 관련 이슈

> 이 PR과 연관된 이슈 번호를 작성해주세요.

- #179

---

## 🧾 문서 요약

> 어떤 문서인지 한 줄로 설명해주세요.

Ops Dashboard 경보 임계치 환경변수(`OPS_ALERT_*`)를 운영 런북에 반영했습니다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

- `bug/#177`에서 경보 임계치가 환경변수 기반으로 변경됨
- 운영자가 코드 수정 없이 환경별 임계치 조정 방법을 바로 확인할 수 있어야 함

---

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 운영 런북에 Ops Dashboard 경보 임계치 환경변수 항목 추가
- [x] 검색/크롤러/큐 경보 임계치 변수별 목적 설명 추가
- [x] 운영자가 API 점검 시 함께 확인할 수 있도록 섹션 위치 정리
