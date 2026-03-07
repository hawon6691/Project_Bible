---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Remaining Backend API 프론트엔드 연동 완료"
labels: feature
issue: "[FEAT] Remaining Backend API 프론트엔드 연동 완료"
commit: "feat: (#307) 남은 backend API 연동 및 개발자 테스트 페이지 완성"
branch: "feat/#307/frontend-remaining-api-integration-complete"
assignees: ""
---

## ✨ 기능 요약

> 프론트엔드 미연동 상태였던 나머지 백엔드 공개 API 모듈(Push, Crawler, Query, SearchSync)을 모두 연결하고, 개발자 테스트 페이지 및 라우트까지 추가해 현재 기준 백엔드 공개 HTTP API 전체를 프론트엔드에서 검증할 수 있도록 마무리했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 남은 API 모듈 식별 및 라우트/응답 구조 확인
  - [x] `Push`
  - [x] `Crawler`
  - [x] `Query`
  - [x] `SearchSync`
  - [x] 내부 운영성 모듈(`Redlock`)은 공개 테스트 API 대상이 아님을 확인
- [x] 남은 API 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - [x] `PushSubscriptionItem`
  - [x] `PushPreferenceItem`
  - [x] `PaginationMeta`
  - [x] `CrawlerJobItem`
  - [x] `CrawlerRunItem`
  - [x] `CrawlerMonitoringSummary`
  - [x] `CrawlerJobListResult`
  - [x] `CrawlerRunListResult`
  - [x] `ProductQueryViewItem`
  - [x] `ProductQueryViewListResult`
  - [x] `SearchSyncOutboxSummary`
- [x] Push 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `registerPushSubscription` (`POST /push/subscriptions`)
  - [x] `unregisterPushSubscription` (`POST /push/subscriptions/unsubscribe`)
  - [x] `fetchPushSubscriptions` (`GET /push/subscriptions`)
  - [x] `fetchPushPreference` (`GET /push/preferences`)
  - [x] `updatePushPreference` (`POST /push/preferences`)
- [x] Crawler 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `fetchCrawlerJobsAdmin` (`GET /crawler/admin/jobs`)
  - [x] `createCrawlerJobAdmin` (`POST /crawler/admin/jobs`)
  - [x] `updateCrawlerJobAdmin` (`PATCH /crawler/admin/jobs/:id`)
  - [x] `removeCrawlerJobAdmin` (`DELETE /crawler/admin/jobs/:id`)
  - [x] `triggerCrawlerJobAdmin` (`POST /crawler/admin/jobs/:id/run`)
  - [x] `triggerCrawlerManualAdmin` (`POST /crawler/admin/triggers`)
  - [x] `fetchCrawlerRunsAdmin` (`GET /crawler/admin/runs`)
  - [x] `fetchCrawlerMonitoringAdmin` (`GET /crawler/admin/monitoring`)
- [x] Query 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `fetchQueryProducts` (`GET /query/products`)
  - [x] `fetchQueryProductDetail` (`GET /query/products/:productId`)
  - [x] `syncQueryProductAdmin` (`POST /admin/query/products/:productId/sync`)
  - [x] `rebuildQueryProductsAdmin` (`POST /admin/query/products/rebuild`)
- [x] SearchSync 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - [x] `fetchSearchSyncOutboxSummaryAdmin` (`GET /search/admin/index/outbox/summary`)
  - [x] `requeueSearchSyncFailedAdmin` (`POST /search/admin/index/outbox/requeue-failed`)
- [x] 남은 API 전용 테스트 페이지 추가 (`FrontEnd/src/pages`)
  - [x] `PushApiPage.tsx`
  - [x] `CrawlerApiPage.tsx`
  - [x] `QueryApiPage.tsx`
  - [x] `SearchSyncApiPage.tsx`
- [x] 라우트/개발자 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - [x] `/developer/push-api`
  - [x] `/developer/crawler-api`
  - [x] `/developer/query-api`
  - [x] `/developer/search-sync-api`
  - [x] 구 경로 리다이렉트(`/push-api`, `/crawler-api`, `/query-api`, `/search-sync-api`) 추가
- [x] 현재 기준 백엔드 공개 HTTP API 전체 프론트 연동 완료 상태 정리
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] `Query` 모듈은 일반 상품 테이블 직접 조회가 아니라 읽기 모델(`query/products`) 기준으로 응답하므로 별도 타입(`ProductQueryViewItem`)으로 반영
- [x] `Crawler` 목록/실행 이력 응답은 `PaginationResponseDto` 구조(`items`, `meta`)를 사용하므로 페이지 렌더링도 해당 구조에 맞춰 처리
- [x] `Push` 구독 해제는 `DELETE`가 아니라 `POST /push/subscriptions/unsubscribe` 형태라 서버 구현 기준 그대로 반영
- [x] `SearchSync` 재큐잉은 body가 아닌 query `limit` 기반이라 요청 함수도 query 전달 방식으로 구현
- [x] `Crawler`, `SearchSync`는 관리자 권한 전용 운영 API이므로 관리자 토큰 기준 검증 필요
- [x] `Redlock`은 내부 락/인프라 모듈이며 별도 공개 테스트 컨트롤러가 없어 프론트 연동 완료 범위에서 제외
