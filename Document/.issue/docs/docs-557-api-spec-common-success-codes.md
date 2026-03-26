---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] API Specification Common Success Codes"
labels: document
assignees: ""
issue: "[DOCS] API Specification Common Success Codes 문서 작성"
commit: "docs: (#557) API Specification Common Success Codes 문서 작성"
branch: "docs/#557/api-spec-common-success-codes"
---

## 🧾 문서 요약

> 어떤 문서인지 한 줄로 설명해주세요.

`02_api-specification.md`의 공통 에러 코드 섹션 바로 위에 공통 성공 응답 상태 표를 추가해, 성공/실패 공통 코드를 함께 참조할 수 있도록 정리한다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

기존 API 명세에는 공통 에러 코드만 정리되어 있었고, 성공 응답에서 반복적으로 쓰이는 HTTP 상태의 공통 기준은 한곳에 모여 있지 않았다.

`200/201/202/204` 성공 상태를 별도 표로 명시해 문서 해석 일관성과 다언어 구현체 정렬 기준을 보강한다.

공통 코드 표는 개별 엔드포인트 표를 대체하지 않고, API 명세 전체에서 반복되는 성공 상태 해석 기준을 보조하는 역할로 둔다.

## 📋 요구사항

> 수행해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `02_api-specification.md`의 공통 에러 코드 바로 위에 `## 공통 성공 코드` 섹션을 추가한다.
- [x] 공통 성공 코드 표는 기존 공통 에러 코드 표와 동일한 형식인 `HTTP Status | 코드 | 설명` 컬럼으로 작성한다.
- [x] 성공 코드 항목은 `200 OK`, `201 CREATED`, `202 ACCEPTED`, `204 NO_CONTENT`를 포함한다.
- [x] 각 설명은 조회/생성/비동기 접수/본문 없는 처리 완료 의미가 빠르게 이해되도록 간결하게 작성한다.
- [x] 기존 공통 에러 코드 섹션과 전체 API 명세 구조는 유지하고, 이번 변경은 성공 코드 공통 기준 보강에만 한정한다.
