---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] JavaScript Express Prisma Structure Hardening"
labels: chore
assignees: ""
issue: "[CHORE] JavaScript Express Prisma Structure Hardening 문서 작성"
commit: "chore: (#443) JavaScript Express Prisma 파일 구조 정리"
branch: "chore/#443/javascript-express-prisma-structure-hardening"
---

## 🛠️ 작업 요약

> JavaScript Express Prisma 구현체의 파일 구조를 실무형 기준에 맞게 정리하고, validator/config/DTO/mapper/repository 계층을 명확히 분리한다.

## 🎯 목적 및 배경

> 왜 이 작업이 필요한가요?

- 초기 구현 단계에서 빠르게 붙인 라우트와 로직을 그대로 두면 기능이 늘어날수록 유지보수가 어려워진다.
- Express + Prisma 구현체도 다른 언어와 마찬가지로 역할 분리와 파일 네이밍 일관성이 필요했다.
- 공통 문서 기준을 벗어나지 않으면서 이후 Swagger, 테스트, CI를 붙이기 쉬운 구조로 먼저 정리할 필요가 있었다.

## 📋 요구사항

- [x] 기능별 폴더 기준으로 `controller / service / repository / mapper` 구조 정리
- [x] `src/modules` 레거시 폴더 제거
- [x] 전역 `validators`를 각 feature 폴더 안으로 이동
- [x] `validate` 미들웨어를 통해 라우트에 validator 연결
- [x] 컨트롤러 응답을 mapper 기반 DTO로 통일
- [x] `prisma` 진입점을 `src/prisma.js` 하나로 통일
- [x] `routes` 파일명을 복수형 기준으로 정리
- [x] 중앙 라우트 등록 파일을 새 파일 구조에 맞게 정리
- [x] 구조 변경 후 대표 엔드포인트 재기동 및 응답 확인

## ✅ 산출물

- `BackEnd/JavaScript/expressshop_prismaorm/src/routes/index.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/prisma.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/middleware/validate.js`
- `BackEnd/JavaScript/expressshop_prismaorm/src/auth/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/users/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/categories/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/products/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/cart/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/addresses/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/orders/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/payments/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/reviews/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/wishlist/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/points/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/boards/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/inquiries/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/faqs/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/notices/*`
- `BackEnd/JavaScript/expressshop_prismaorm/src/tickets/*`

## 검증 메모

- `/health` 정상 응답 확인
- `/api/v1/categories` 정상 응답 확인
- 로그인 후 `/api/v1/orders` 정상 응답 확인
- 로그인 후 `/api/v1/support/tickets` 정상 응답 확인
- 빈 로그인 요청 시 validator가 `400` 응답으로 차단되는 것 확인
- `src/modules` 제거 후에도 앱 재기동이 정상 동작하는 것 확인

## 메모

- 이번 변경은 기능 추가보다 구조 정리와 계층 분리에 초점을 둔 작업이다.
- 현재 JavaScript 구현체는 `feature folder + routes + controller + service + repository + mapper + validator + config` 기준으로 정리돼 있다.
- 다음 단계는 구조 정리보다 `Swagger`, 남은 기능 구현, 테스트, CI 작성이 우선이다.
