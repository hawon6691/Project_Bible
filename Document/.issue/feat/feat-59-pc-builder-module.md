---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PC 견적 짜기 모듈 구현"
labels: feature
issue: "[FEAT] PC 견적 짜기 모듈 구현"
commit: "feat: (#59) PC 견적/부품/호환성/공유/규칙 API 구현"
branch: "feat/#59/pc-builder-module"
assignees: ""
---

## ✨ 기능 요약

> PC 견적 생성/관리, 부품 추가/삭제, 호환성 체크, 공유 링크, 인기 견적, 관리자 호환성 규칙 CRUD 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] PC 견적 엔티티 구현 (`pc_builds`)
- [x] PC 견적 부품 엔티티 구현 (`pc_build_parts`)
- [x] 호환성 규칙 엔티티 구현 (`pc_compatibility_rules`)
- [x] PC Builder DTO 구현 (견적 생성/수정, 부품 추가, 규칙 생성/수정, 목록 쿼리)
- [x] PC Builder 모듈/서비스/컨트롤러 추가
- [x] 내 견적 목록 API 구현 (`GET /pc-builds`)
- [x] 견적 생성 API 구현 (`POST /pc-builds`)
- [x] 견적 상세 조회 API 구현 (`GET /pc-builds/:id`)
- [x] 견적 수정/삭제 API 구현 (`PATCH/DELETE /pc-builds/:id`)
- [x] 부품 추가/삭제 API 구현 (`POST /pc-builds/:id/parts`, `DELETE /pc-builds/:id/parts/:partId`)
- [x] 호환성 체크 API 구현 (`GET /pc-builds/:id/compatibility`)
- [x] 공유 링크 생성/공유 조회 API 구현 (`GET /pc-builds/:id/share`, `GET /pc-builds/shared/:shareCode`)
- [x] 인기 견적 목록 API 구현 (`GET /pc-builds/popular`)
- [x] 관리자 호환성 규칙 CRUD API 구현 (`/admin/compatibility-rules`)
- [x] 판매처 가격 기반 부품 가격 선택 및 총액 갱신 로직 구현
- [x] 필수 부품 누락/규칙 기반 경고/전력 추정 호환성 평가 로직 구현
- [x] 앱 모듈 등록 (`PcBuilderModule`)
- [x] API 라우트 상수 추가 (`PC_BUILDER`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
