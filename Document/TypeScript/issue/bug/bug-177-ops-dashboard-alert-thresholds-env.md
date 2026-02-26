---
name: "🐛 Bug Report"
about: 버그 신고
title: "[BUG] Ops Dashboard 경보 임계치 하드코딩 제거"
labels: bug
issue: "[BUG] Ops Dashboard 경보 임계치 하드코딩 제거"
commit: "bug: (#177) ops-dashboard 경보 임계치를 환경변수 기반으로 전환"
branch: "bug/#177/ops-dashboard-alert-thresholds-env"
assignees: ""
---

## 🐛 버그 요약

> Ops Dashboard 경보 임계치가 하드코딩되어 운영 환경별 튜닝이 어려운 문제를 수정했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Ops Dashboard 경보 임계치 하드코딩 제거
- [x] ConfigService 기반 임계치 로딩 로직 추가
- [x] 검색/크롤러/큐 경보 기준을 환경변수로 전환
- [x] `.env.example`에 Ops Dashboard 임계치 변수 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
