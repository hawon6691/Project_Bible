---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Resilience + Error Code Catalog API (Step 47-48) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Resilience + Error Code Catalog API (Step 47-48) 프론트엔드 연동"
commit: "feat: (#301) resilience+error-code API 연동 및 테스트 페이지 추가"
branch: "feat/#301/resilience-error-code-api-step"
assignees: ""
---

## ✨ 기능 요약

> 요청하신 2단계 묶음 방식으로 Resilience API(47번), Error Code Catalog API(48번)를 프론트엔드에 연동하고 수동 검증용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Resilience/Error Code 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `ResilienceCircuitSnapshot`, `ResiliencePolicyItem`
  - `ErrorCodeItem`
- [x] Resilience 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchResilienceSnapshotsAdmin` (`GET /resilience/circuit-breakers`)
  - `fetchResiliencePoliciesAdmin` (`GET /resilience/circuit-breakers/policies`)
  - `fetchResilienceSnapshotAdmin` (`GET /resilience/circuit-breakers/:name`)
  - `resetResilienceCircuitAdmin` (`POST /resilience/circuit-breakers/:name/reset`)
- [x] Error Code Catalog 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchErrorCodes` (`GET /errors/codes`)
  - `fetchErrorCode` (`GET /errors/codes/:key`)
- [x] Resilience/Error Code 테스트 페이지 추가
  - `FrontEnd/src/pages/ResilienceApiPage.tsx`
  - `FrontEnd/src/pages/ErrorCodeApiPage.tsx`
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/resilience-api`, `/error-code-api` 경로 추가
  - 상단 메뉴 `ResilienceAPI`, `ErrorCodeAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] Error Code Catalog API는 `src/common/controllers/error-code.controller.ts`에 구현되어 있어 해당 라우트(`/errors/codes`) 기준으로 반영
- [x] Resilience API는 Admin 권한 전용이므로 관리자 토큰 기준으로 검증 필요
