---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[DOCS] API 명세-실제 라우트 동기화"
labels: documentation
issue: "[DOCS] API 명세-실제 라우트 동기화"
commit: "docs: (#141) API 명세를 최신 컨트롤러 라우트와 동기화"
branch: "docs/#141/api-spec-route-sync"
assignees: ""
---

## ✨ 기능 요약

> `02_api-specification.md`를 최신 Nest 컨트롤러/라우트 기준으로 정합성 보정했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Search 섹션 Admin/Recent/Outbox 라우트 최신화 (`/search/admin/index/*`, `/search/recent*`)
- [x] Crawler 섹션 경로 체계 보정 (`/crawler/admin/*`)
- [x] Prediction 엔드포인트 경로 보정 (`/predictions/products/:productId/price-trend`)
- [x] Shortform 섹션에 트랜스코딩 상태/재시도 API 반영
- [x] Shortform 랭킹 경로 보정 (`/shortforms/ranking/list`)
- [x] Resilience(Circuit Breaker) Admin API 섹션 추가
- [x] Error Code Catalog API 섹션 추가 (`/errors/codes`, `/errors/codes/:key`)

