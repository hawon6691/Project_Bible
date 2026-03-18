---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma I18n API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma I18n API 구현"
commit: "feat: (#473) JavaScript Express Prisma I18n API 구현"
branch: "feat/#473/javascript-express-prisma-i18n-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 다국어 번역 및 환율 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `i18n/translations` 번역 조회 API 추가
- [x] `admin/i18n/translations` 번역 등록/수정 API 추가
- [x] `admin/i18n/translations/:id` 번역 삭제 API 추가
- [x] `i18n/exchange-rates` 환율 목록 조회 API 추가
- [x] `admin/i18n/exchange-rates` 환율 등록/수정 API 추가
- [x] `i18n/convert` 금액 환산 API 추가
- [x] Prisma schema에 `translations`, `exchange_rates` 매핑 추가
- [x] 라우트 인덱스에 `i18n` 라우터 연결
- [x] README 노출 경로 요약 갱신
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
