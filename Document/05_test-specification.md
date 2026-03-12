# 05 Test Specification

## 목적

이 문서는 `04_language.md`의 언어별 구현 기준을 바탕으로, 프로젝트에서 공통으로 유지해야 하는 테스트 명세를 정의한다.
현재 작성된 실제 테스트 구현체는 `TypeScript`와 `PHP`를 기준으로 삼고, 이후 다른 언어도 이 명세를 따라 동일한 테스트 축을 맞추는 것을 목표로 한다.

## 기준 구현체

- TypeScript
  - 프레임워크: `NestJS`
  - 테스트 기준 경로: `BackEnd/TypeScript/nestshop/test`
- PHP
  - 프레임워크: `Laravel`
  - 테스트 기준 경로: `BackEnd/PHP/laravelshop/tests`

## 테스트 계층

### 1. 운영/플랫폼 E2E

운영 기능, 공개 계약, 보안 경계, 회복성, 관측성을 검증하는 횡단 테스트다.

공통 테스트 이름:
- `PublicApiE2E`
- `ContractPublicApiE2E`
- `AuthSearchE2E`
- `AdminAuthorizationBoundaryE2E`
- `AdminPlatformE2E`
- `ObservabilityE2E`
- `OpsDashboardE2E`
- `OpsDashboardDependencyFailuresE2E`
- `OpsDashboardResilienceE2E`
- `OpsDashboardThresholdsE2E`
- `QueueAdminE2E`
- `RateLimitRegressionE2E`
- `ResilienceAutoTuneE2E`
- `SecurityRegressionE2E`

검증 범위:
- 공개 API 응답 계약
- 인증/인가 경계
- 관리자 전용 엔드포인트 보호
- 운영 대시보드 요약 상태
- 큐 관리, 자동 재시도, 실패 작업 처리
- 관측성 메트릭/트레이스/대시보드
- 임계치 및 경고 규칙
- 회복성 정책 조회 및 자동 튜닝
- 보안 회귀와 요청 제한

## 2. 도메인 통합 테스트

실제 쇼핑몰 기능을 도메인 단위로 묶어 검증하는 통합 테스트다.

공통 테스트 이름:
- `AuthApi`
- `UserApi`
- `CategoryApi`
- `ProductApi`
- `SpecSellerPriceApi`
- `CartAddressApi`
- `OrderPaymentApi`
- `ReviewWishlistPointApi`
- `CommunityInquirySupportApi`
- `ActivityChatPushApi`
- `PredictionDealRecommendationRankingApi`
- `FraudTrustI18nImageBadgeApi`
- `PcFriendShortformMediaNewsMatchingApi`
- `AnalyticsUsedMarketAutoAuctionCompareApi`
- `PhpOpsApi`
- `HealthRoute`

검증 범위:
- 회원가입, 로그인, 인증, 토큰 처리
- 회원 정보, 프로필, 상태 관리
- 카테고리/상품 CRUD 및 권한
- 스펙, 판매처, 가격, 가격 알림
- 장바구니와 배송지
- 주문, 결제, 환불, 상태 전이
- 리뷰, 위시리스트, 포인트
- 커뮤니티, 문의, 고객센터
- 활동, 채팅, 푸시
- 예측, 특가, 추천, 랭킹
- 사기, 신뢰도, 다국어, 이미지, 배지
- PC 빌더, 친구, 숏폼, 미디어, 뉴스, 매칭
- 분석, 중고장터, 자동차, 역경매, 비교
- 운영 설정, 헬스, 에러 코드, Query, SearchSync, Crawler

## 3. 성능 테스트

성능과 부하 시나리오는 언어별로 동일한 이름과 목적을 유지한다.

공통 테스트 이름:
- `price-compare.perf`
- `search-ranking.perf`
- `smoke.perf`
- `soak.perf`
- `spike-search.perf`
- `assert-summary`
- `mock-perf-server`

검증 범위:
- 가격 비교 응답 시간
- 검색/랭킹 응답 시간
- 핵심 API smoke 성능
- 장시간 부하 안정성
- 급증 트래픽 대응
- 성능 요약 임계치 판정

## 4. 테스트 스크립트

릴리즈 게이트, 안정성 분석, 마이그레이션 검증을 위한 보조 스크립트다.

공통 스크립트 이름:
- `analyze-stability`
- `live-smoke`
- `migration-roundtrip`
- `validate-migrations`

검증 범위:
- flaky 분석
- 배포 전 smoke 확인
- 마이그레이션 왕복/정합성 검증

## TypeScript 구현 기준

### 파일 구조

- 운영/플랫폼 E2E: `test/e2e/*.e2e-spec.ts`
- 성능 테스트: `test/performance/*`
- 스크립트 테스트: `test/scripts/*`

### 작성 원칙

- NestJS 컨트롤러 단위 mock 기반 통합 테스트를 작성한다.
- `ResponseInterceptor`, `ValidationPipe`, Guard 동작을 포함한 E2E 형식을 유지한다.
- 보호 엔드포인트는 헤더 기반 테스트 가드로 `USER`, `SELLER`, `ADMIN` 권한을 재현한다.

## PHP 구현 기준

### 파일 구조

- 운영/플랫폼 E2E: `tests/E2E/*`
- 도메인 통합 테스트: `tests/Feature/Api/*`
- 성능 테스트: `tests/performance/*`
- 스크립트 테스트: `tests/scripts/*`

### 작성 원칙

- Laravel Feature Test 기반으로 실제 라우트와 응답 포맷을 검증한다.
- 인증이 필요한 경우 `ApiAuthTestHelpers`를 사용한다.
- 운영 E2E는 횡단 시나리오 위주로 `tests/E2E`에 유지한다.
- 도메인 회귀는 `tests/Feature/Api`에서 기능 묶음 단위로 유지한다.

## 현재 정렬 상태

### 운영/플랫폼 E2E

- `TypeScript`: 구현 완료
- `PHP`: 구현 완료

### 도메인 통합 테스트

- `TypeScript`: 구현 완료
- `PHP`: 구현 완료

### 성능 테스트

- `TypeScript`: 구현 완료
- `PHP`: 구현 완료

### 스크립트 테스트

- `TypeScript`: 구현 완료
- `PHP`: 구현 완료

## 적용 규칙

- 이후 다른 언어 구현체도 이 문서의 테스트 이름과 범위를 기준으로 맞춘다.
- 파일 위치는 언어/프레임워크 관례에 따라 달라질 수 있다.
- 다만 테스트 이름, 목적, 검증 범위는 이 문서 기준으로 동일해야 한다.
- 테스트 갭 추적과 우선순위 관리는 `06_php-typescript-test-gap-checklist.md`에서 이어간다.
