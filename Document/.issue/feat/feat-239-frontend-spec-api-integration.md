---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] FrontEnd 스펙(Spec) API 단계 연동"
labels: feature
issue: "[FEAT] FrontEnd 스펙(Spec) API 단계 연동"
commit: "feat: (#239) 스펙 API 연동 및 Spec API 테스트 페이지 추가"
branch: "feat/#239/frontend-spec-api-integration"
assignees: ""
---

## ✨ 기능 요약

> API 명세 순서(5. 스펙)에 맞춰 프론트 스펙 API 연동을 완료했습니다. 스펙 정의 조회/관리, 상품 스펙 조회/설정, 스펙 비교/점수화, 유사 상품 조회까지 테스트 가능한 전용 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 스펙 API 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `GET /specs/definitions`
  - [x] `GET /specs/definitions/resolved/:categoryId` (서버 구현 기준)
  - [x] `POST /specs/definitions` (Admin)
  - [x] `PATCH /specs/definitions/:id` (Admin)
  - [x] `DELETE /specs/definitions/:id` (Admin)
  - [x] `GET /products/:id/specs`
  - [x] `GET /products/:id/specs/grouped` (서버 구현 기준)
  - [x] `PUT /products/:id/specs` (Admin)
  - [x] `POST /specs/compare`
  - [x] `POST /specs/compare/numeric` (서버 구현 기준)
  - [x] `POST /specs/compare/scored`
  - [x] `POST /specs/score` (서버 구현 기준)
  - [x] `PUT /specs/scores/:specDefId` (Admin)
  - [x] `GET /products/:id/similar-spec-products` (서버 구현 기준)
- [x] 스펙 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages/SpecApiPage.tsx`)
  - [x] 스펙 정의 조회/해결된 정의 조회
  - [x] 스펙 정의 생성/수정/삭제(Admin)
  - [x] 상품 스펙 조회/그룹 조회/설정(Admin)
  - [x] 스펙 비교/숫자 비교/점수 비교
  - [x] 카테고리 기반 점수화/스코어 매핑(Admin)
  - [x] 유사 상품 조회 및 비교 결과 JSON 렌더링
- [x] 헤더/라우트에 Spec API 페이지 연결 (`FrontEnd/src/App.tsx`)
- [x] 비교 결과 렌더링 스타일 추가 (`FrontEnd/src/styles.css`)
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ✅ 완료 조건

> 이 기능이 완료됐다고 판단하는 기준을 작성해주세요.

- [x] `/spec-api`에서 명세 5단계 스펙 API 흐름을 end-to-end로 수동 검증 가능
- [x] 관리자 토큰으로 정의/상품스펙/스코어 관리 요청 검증 가능
- [x] 일반 사용자/미로그인 상태에서 Public API와 권한 오류 응답 구분 확인 가능
- [x] 타입체크 및 빌드 오류 없음

## ℹ️ 참고

> 명세 문서와 서버 구현 간 차이는 서버 구현 기준으로 우선 반영했습니다.

- 명세 외 확장 엔드포인트(`resolved`, `grouped`, `numeric`, `score`, `similar-spec-products`)를 함께 반영
- 테스트 페이지는 API 스모크 테스트 목적의 운영자 도구 성격으로 구성
