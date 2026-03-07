---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 통합 에러코드 카탈로그 조회 API 구현"
labels: feature
issue: "[FEAT] 통합 에러코드 카탈로그 조회 API 구현"
commit: "feat: (#125) common error code catalog API 및 라우트 상수 추가"
branch: "feat/#125/error-code-catalog-api"
assignees: ""
---

## ✨ 기능 요약

> 공통 에러코드 상수를 카탈로그 형태로 확장하고, 운영/문서 확인용 조회 API를 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 에러코드 카탈로그 타입/배열 export 추가 (`ERROR_CODE_CATALOG`)
- [x] 에러코드 전체 목록 조회 API 구현 (`GET /errors/codes`)
- [x] 에러코드 단건 조회 API 구현 (`GET /errors/codes/:key`)
- [x] CommonModule에 ErrorCodeController 등록
- [x] API 라우트 상수 확장 (`ERRORS`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
