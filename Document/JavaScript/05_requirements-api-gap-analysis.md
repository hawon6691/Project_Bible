# JavaScript Requirements / API Gap Analysis

대상: `BackEnd/JavaScript/expressshop_prismaorm`

## 기능 기준

- 주요 REST API 갭은 대부분 정리되었다.
- 최근 닫힌 갭
  - `GET /errors/codes`
  - `GET /errors/codes/:key`
  - `GET /query/products`
  - `GET /query/products/:productId`
  - `POST /admin/query/products/:productId/sync`
  - `POST /admin/query/products/rebuild`
  - `POST /chat/rooms/:id/join`
  - `POST /chat/rooms/:id/messages`
  - Chat Socket.IO 이벤트

## 문서 기준 잔여 갭

- JavaScript 전용 구현 상태 문서 부재
- README에 최근 추가 API/실시간 이벤트 설명 부족
- OpenAPI에 Socket.IO 이벤트 설명 확장 필요

## 테스트 기준 잔여 갭

- 공통 `05_test-specification.md` 대비 테스트 축이 부족하다.
- 현재 부족 항목
  - `AuthSearchE2E`
  - `QueueAdminE2E`
  - `ObservabilityE2E`
  - `RateLimitRegressionE2E`
  - 도메인 통합 테스트 묶음
  - 성능 자산
  - 스크립트 자산

## CI 기준 잔여 갭

- 공통 `06_ci-specification.md` 대비 다음 게이트가 부족하다.
  - `quality`
  - `contract-doc`
  - `perf-smoke`
  - `migration-validation-manual`
  - `migration-roundtrip-manual`
  - `security-regression-manual`
  - `admin-boundary-manual`
  - `release-gate`

## 결론

JavaScript 트랙의 현재 남은 갭은 기능보다 검증/문서/CI 축에 집중되어 있다.
