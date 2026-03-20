---
name: "♻️ Refactor Request"
about: 기능 수정 제안
title: "[REFACT] TypeScript 내부 식별자 정리"
labels: refactor
assignees: ""
issue: "[REFACT] TypeScript 내부 식별자 정리"
commit: "refactor: (#523) typescript 내부 브랜딩 및 기본 식별자 정리"
branch: "refactor/#523/typescript-internal-identifier-alignment"
---

## ♻️ 수정 요약

> TypeScript 구현체의 Swagger 제목, 메일 브랜드, S3/VAPID 기본값, 테스트 DB 식별자를 `PBShop` 및 현재 프로젝트 명명 규칙 기준으로 정리했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Swagger 제목을 `PBShop API` 기준으로 정리한다.
- [x] 메일 발신자 기본값과 메일 제목의 `NestShop` 브랜딩을 `PBShop` 기준으로 정리한다.
- [x] 메일 템플릿의 서비스명을 `PBShop`으로 정리한다.
- [x] S3 버킷, VAPID subject, 기본 데이터베이스 이름을 `pbshop`/`pbdb` 기준으로 정리한다.
- [x] migration roundtrip 스크립트와 TypeScript CI의 테스트 데이터베이스 이름을 `pbdb_test` 기준으로 정리한다.
- [x] TypeScript 프로젝트에서 `NestShop`/`nestshop` 문자열이 남지 않도록 확인하고 `npm ci`, `npm run build`로 정합성을 검증한다.
