---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Search API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Search API 구현"
commit: "feat: (#471) JavaScript Express Prisma Search API 구현"
branch: "feat/#471/javascript-express-prisma-search-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 통합 검색, 자동완성, 최근 검색어, 인기 검색어, 검색 인덱스 관리 API를 추가한다.


## 📋 요구사항

- [x] `GET /api/v1/search` 엔드포인트를 추가한다.
- [x] `GET /api/v1/search/autocomplete` 엔드포인트를 추가한다.
- [x] `GET /api/v1/search/popular` 엔드포인트를 추가한다.
- [x] `POST /api/v1/search/recent` 엔드포인트를 추가한다.
- [x] `GET /api/v1/search/recent` 엔드포인트를 추가한다.
- [x] `DELETE /api/v1/search/recent/:keywordId` 엔드포인트를 추가한다.
- [x] `PATCH /api/v1/search/preferences` 엔드포인트를 추가한다.
- [x] `GET /api/v1/search/admin/weights` 엔드포인트를 추가한다.
- [x] `PATCH /api/v1/search/admin/weights` 엔드포인트를 추가한다.
- [x] `GET /api/v1/search/admin/index/status` 엔드포인트를 추가한다.
- [x] `POST /api/v1/search/admin/index/reindex` 엔드포인트를 추가한다.
- [x] `POST /api/v1/search/admin/index/products/:id/reindex` 엔드포인트를 추가한다.
- [x] `GET /api/v1/search/admin/index/outbox/summary` 엔드포인트를 추가한다.
- [x] `POST /api/v1/search/admin/index/outbox/requeue-failed` 엔드포인트를 추가한다.
- [x] 검색 로그 및 집계 기반 모델을 Prisma 스키마에 반영한다.
- [x] 관리자 전용 재색인 기능에 권한 검증을 적용한다.
- [x] README에 검색 API 경로를 반영한다.
