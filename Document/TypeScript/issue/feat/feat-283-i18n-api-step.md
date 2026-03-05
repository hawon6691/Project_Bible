---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] I18n API (다음 단계) 프론트엔드 연동"
labels: feature
issue: "[FEAT] I18n API (다음 단계) 프론트엔드 연동"
commit: "feat: (#283) i18n API 연동 및 I18n API 테스트 페이지 추가"
branch: "feat/#283/i18n-api-step"
assignees: ""
---

## ✨ 기능 요약

> Trust API 다음 단계로 I18n API를 프론트엔드에 연동하고, 수동 검증용 I18n API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] I18n 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `TranslationItem`
  - `ExchangeRateItem`
  - `ConvertedAmountResult`
- [x] I18n 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchTranslations` (`GET /i18n/translations`)
  - `upsertTranslationAdmin` (`POST /i18n/admin/translations`)
  - `removeTranslationAdmin` (`DELETE /i18n/admin/translations/:id`)
  - `fetchExchangeRates` (`GET /i18n/exchange-rates`)
  - `upsertExchangeRateAdmin` (`POST /i18n/admin/exchange-rates`)
  - `convertAmount` (`GET /i18n/convert`)
- [x] I18n API 테스트 페이지 추가 (`FrontEnd/src/pages/I18nApiPage.tsx`)
  - 번역 조회
  - 번역 등록/수정 및 삭제(Admin)
  - 환율 목록 조회 및 등록/수정(Admin)
  - 금액 환산
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/i18n-api` 경로 추가
  - 상단 메뉴 `I18nAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 명세 문서는 Admin 경로를 `/admin/i18n/...` 형태로 설명
- [x] 현재 서버 구현(`i18n.controller.ts`)은 `/i18n/admin/...` 형태를 사용
- [x] 이번 프론트 단계는 현재 서버 구현 기준으로 반영
