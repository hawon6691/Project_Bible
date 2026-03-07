---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 판매처 신뢰도 모듈 구현"
labels: feature
issue: "[FEAT] 판매처 신뢰도 모듈 구현"
commit: "feat: (#51) 판매처 신뢰도 조회/이력/재산정 API 구현"
branch: "feat/#51/trust-module"
assignees: ""
---

## ✨ 기능 요약

> 판매처 신뢰도 현재 점수 조회, 신뢰도 이력 조회, 관리자 재산정 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 신뢰도 이력 엔티티 구현 (`trust_score_histories`)
- [x] 신뢰도 DTO 구현 (재산정/이력 조회)
- [x] Trust 모듈/서비스/컨트롤러 추가
- [x] 판매처 현재 신뢰도 조회 API 구현 (`GET /trust/sellers/:sellerId`)
- [x] 판매처 신뢰도 이력 조회 API 구현 (`GET /trust/sellers/:sellerId/history`)
- [x] 관리자 신뢰도 재산정 API 구현 (`POST /trust/admin/sellers/:sellerId/recalculate`)
- [x] 복합 지표 기반 점수 계산 로직 구현
- [x] 점수 기반 등급(S~D) 산정 로직 추가
- [x] 앱 모듈 등록 (`TrustModule`)
- [x] API 라우트 상수 추가 (`TRUST`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
