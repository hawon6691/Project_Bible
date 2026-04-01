---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Ops Dashboard 임계치 경보 회귀 테스트 추가"
labels: feature
issue: "[FEAT] Ops Dashboard 임계치 경보 회귀 테스트 추가"
commit: "feat: (#181) ops dashboard threshold 경보 동작 회귀 e2e 추가"
branch: "feat/#181/ops-dashboard-threshold-regression"
assignees: ""
---

## ✨ 기능 요약

> Ops Dashboard 경보 임계치 환경변수(`OPS_ALERT_*`)가 실제 경보 발생/억제에 반영되는지 E2E로 검증했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Ops Dashboard 임계치 회귀 E2E 스펙 추가 (`ops-dashboard-thresholds.e2e-spec.ts`)
- [x] 임계치가 높은 경우 경보 미발생(`alertCount=0`) 시나리오 추가
- [x] 임계치 충족 시 `searchSync/crawler/queue` 경보 발생 시나리오 추가
- [x] ConfigService mock 기반 임계치 주입 테스트 구성
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
- [x] E2E 테스트 통과 (`npm run test:e2e -- ops-dashboard-thresholds.e2e-spec.ts --runInBand`)


