---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Trust API (다음 단계) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Trust API (다음 단계) 프론트엔드 연동"
commit: "feat: (#281) trust API 연동 및 Trust API 테스트 페이지 추가"
branch: "feat/#281/trust-api-step"
assignees: ""
---

## ✨ 기능 요약

> Fraud API 다음 단계로 Trust API를 프론트엔드에 연동하고, 수동 검증용 Trust API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Trust 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `TrustCurrentScore`
  - `TrustHistoryItem`
- [x] Trust 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchTrustCurrentScore` (`GET /trust/sellers/:sellerId`)
  - `fetchTrustHistory` (`GET /trust/sellers/:sellerId/history`)
  - `recalculateTrustScoreAdmin` (`POST /trust/admin/sellers/:sellerId/recalculate`)
- [x] Trust API 테스트 페이지 추가 (`FrontEnd/src/pages/TrustApiPage.tsx`)
  - 현재 신뢰도 조회
  - 신뢰도 이력 조회
  - 관리자 신뢰도 재산정
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/trust-api` 경로 추가
  - 상단 메뉴 `TrustAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세 문서에는 `/sellers/:id/trust` 형태가 기술되어 있음
- [x] 현재 서버 구현(`trust.controller.ts`)은 `/trust/sellers/:sellerId`, `/trust/sellers/:sellerId/history`, `/trust/admin/sellers/:sellerId/recalculate`를 제공
- [x] 이번 프론트 단계는 현재 서버 구현 기준으로 반영
