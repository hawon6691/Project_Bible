---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 자동차 모듈 구현"
labels: feature
issue: "[FEAT] 자동차 모듈 구현"
commit: "feat: (#77) 자동차 모델/트림/견적/리스오퍼 API 구현"
branch: "feat/#77/auto-module"
assignees: ""
---

## ✨ 기능 요약

> 자동차 모델 목록, 트림/옵션 조회, 신차 견적 계산, 모델별 렌트/리스 오퍼 조회 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Auto 모듈/서비스/컨트롤러 추가
- [x] 모델 목록 조회 쿼리 DTO 구현 (`CarModelQueryDto`)
- [x] 신차 견적 요청 DTO 구현 (`AutoEstimateDto`)
- [x] 자동차 모델 목록 API 구현 (`GET /auto/models`)
- [x] 모델별 트림/옵션 조회 API 구현 (`GET /auto/models/:id/trims`)
- [x] 신차 견적 계산 API 구현 (`POST /auto/estimate`)
- [x] 모델별 렌트/리스 오퍼 조회 API 구현 (`GET /auto/models/:id/lease-offers`)
- [x] 기본 견적 계산 로직 구현 (기본가 + 옵션가 + 세금 + 월납입금)
- [x] 브랜드/타입 필터링 로직 구현
- [x] 앱 모듈 등록 (`AutoModule`)
- [x] API 라우트 상수 추가 (`AUTO`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
