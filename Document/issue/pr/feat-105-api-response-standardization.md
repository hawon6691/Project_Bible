---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] API 응답 표준화"
labels: feature
issue: "[FEAT] API 응답 표준화"
commit: "feat: (#105) 성공/실패 응답 스키마 정규화"
branch: "feat/#105/api-response-standardization"
assignees: ""
---

## ✨ 기능 요약

> 전역 인터셉터/예외 필터를 보강하여 성공/실패 응답을 요구사항 스펙 형태로 표준화했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 성공 응답 공통 래퍼 유지 (`success`, `data`, `timestamp`)
- [x] 페이징 응답 `meta` 상위 노출 정규화 (`page`, `limit`, `totalCount`, `totalPages`)
- [x] `PaginationResponseDto(items + meta)` 형태 자동 변환 로직 구현
- [x] 실패 응답에 스펙 필드 추가 (`error.code`, `error.message`)
- [x] 실패 응답 하위 호환 필드 유지 (`errorCode`, `message`)
- [x] 예외 로깅/응답 포맷 통합 유지
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
