# 05 Test Code List

## 목적

이 문서는 `TypeScript`와 `PHP` 백엔드의 실제 테스트 코드를 기준으로 만든 통합 테스트 완성본 목록이다.
테스트 이름은 확장자를 제외한 기준 이름으로 정리했고, 각 테스트가 검증하는 구체적인 내용과 현재 구현 언어별 존재 여부를 함께 기록한다.

기준 원칙:
- `TypeScript` 완성본의 `test/e2e` 축을 우선 기준으로 삼는다.
- `PHP`에만 있는 도메인별 대형 통합 테스트도 완성본 목록에 포함한다.
- 앞으로는 이 문서의 테스트 이름을 기준으로 양쪽 구현체를 맞춘다.

## 상태 표기

- `TS`: TypeScript에 구현됨
- `PHP`: PHP에 구현됨
- `공통`: 양쪽 모두 구현됨
- `추가 필요`: 한쪽에만 있어 반대편에도 맞춰야 함

## 공통 통합 테스트 기준 목록

### 1. AdminAuthorizationBoundaryE2E

- 상태: 공통
- TypeScript 기준 이름: `admin-authorization-boundary`
- PHP 기준 이름: `AdminAuthorizationBoundaryE2ETest`
- 검증 내용:
  - 관리자 헤더 또는 관리자 권한 없이 관리자 전용 엔드포인트 접근 시 차단되는지 확인
  - 일반 사용자와 관리자 사용자의 접근 결과가 다르게 나오는지 확인
  - 운영용 메트릭 또는 관리자 플랫폼 API가 권한 경계를 정확히 지키는지 검증

### 2. AdminPlatformE2E

- 상태: 공통
- TypeScript 기준 이름: `admin-platform`
- PHP 기준 이름: `AdminPlatformE2ETest`
- 검증 내용:
  - 검색 인덱스 상태 조회
  - 전체 리인덱스 요청
  - 단일 상품 리인덱스 요청
  - 아웃박스 요약 조회
  - 실패 작업 재큐잉 파라미터 검증
  - 크롤러 관리자 목록과 모니터링 응답 검증
  - 서킷 브레이커 목록, 상세, 리셋 API 검증

### 3. AuthSearchE2E

- 상태: 공통
- TypeScript 기준 이름: `auth-search`
- PHP 기준 이름: `AuthSearchE2ETest`
- 검증 내용:
  - 회원가입 요청의 payload 유효성 검증
  - 회원가입 성공 시 표준 응답 포맷 검증
  - 로그인 후 인증 흐름이 정상 동작하는지 확인
  - 자동완성 검색의 쿼리 검증
  - 검색 API의 래핑된 응답 구조와 기본 결과 검증
  - PHP에서는 회원가입, 로그인, 상품 조회를 한 흐름으로 연결해 검증

### 4. ContractPublicApiE2E

- 상태: 공통
- TypeScript 기준 이름: `contract-public-api`
- PHP 기준 이름: `ContractPublicApiE2ETest`
- 검증 내용:
  - `/health` 응답 계약이 안정적으로 유지되는지 확인
  - `/errors/codes` 응답 계약이 안정적으로 유지되는지 확인
  - 공개 API의 필드 구조, 성공 포맷, 코드 카탈로그 형태가 문서와 어긋나지 않는지 검증

### 5. PublicApiE2E

- 상태: TS만 구현, PHP 추가 필요
- TypeScript 기준 이름: `public-api`
- 검증 내용:
  - `/health`가 래핑된 성공 응답을 반환하는지 검증
  - `/errors/codes`가 코드 카탈로그를 반환하는지 검증
  - `ContractPublicApiE2E`가 계약 안정성 중심이라면, 이 테스트는 실제 공개 응답 사용성 중심으로 분리됨

### 6. ObservabilityE2E

- 상태: 공통
- TypeScript 기준 이름: `observability`
- PHP 기준 이름: `ObservabilityE2ETest`
- 검증 내용:
  - `/admin/observability/metrics`가 지연 시간과 에러 요약을 반환하는지 확인
  - `/admin/observability/traces`가 최근 트레이스 목록을 반환하는지 확인
  - `/admin/observability/dashboard`가 통합 관측성 payload를 반환하는지 확인

### 7. OpsDashboardE2E

- 상태: 공통
- TypeScript 기준 이름: `ops-dashboard`
- PHP 기준 이름: `OpsDashboardE2ETest`
- 검증 내용:
  - `/admin/ops-dashboard/summary`가 운영 요약 정보를 집계해서 반환하는지 확인
  - PHP에서는 resilience 관련 API 접근 가능 여부도 함께 묶어서 검증

### 8. OpsDashboardDependencyFailuresE2E

- 상태: 공통
- TypeScript 기준 이름: `ops-dashboard-dependency-failures`
- PHP 기준 이름: `OpsDashboardDependencyFailuresE2ETest`
- 검증 내용:
  - 의존성 장애가 발생했을 때 요약 상태가 `degraded`로 표기되는지 확인
  - 장애 원인이 응답에 누락되지 않고 수집되는지 검증

### 9. OpsDashboardResilienceE2E

- 상태: TS만 구현, PHP 추가 필요
- TypeScript 기준 이름: `ops-dashboard-resilience`
- 검증 내용:
  - 부분 장애 상황에서 운영 대시보드가 degraded 요약을 정확히 반환하는지 검증
  - 회복성 관련 정보가 summary에 반영되는지 확인

### 10. OpsDashboardThresholdsE2E

- 상태: TS만 구현, PHP 추가 필요
- TypeScript 기준 이름: `ops-dashboard-thresholds`
- 검증 내용:
  - 현재 실패 수보다 임계치가 높으면 경고가 억제되는지 검증
  - 실패 수가 임계치에 도달하면 경고가 발생하는지 검증
  - 임계치 정책 문서와 실제 알림 판단이 일치하는지 확인

### 11. QueueAdminE2E

- 상태: 공통
- TypeScript 기준 이름: `queue-admin`
- PHP 기준 이름: `QueueAdminE2ETest`
- 검증 내용:
  - 지원 큐 목록 조회
  - 큐 상태 통계 조회
  - 실패 작업 목록의 페이지네이션 검증
  - 실패 작업 재시도
  - 자동 재시도 정책 실행
  - 단일 작업 재시도와 비정상 상태 작업 거절 처리
  - 작업 삭제 API 검증

### 12. RateLimitRegressionE2E

- 상태: 공통
- TypeScript 기준 이름: `rate-limit-regression`
- PHP 기준 이름: `RateLimitRegressionE2ETest`
- 검증 내용:
  - 기본 경로에서 반복 호출 시 429가 발생하는지 확인
  - `/auth` 경로에 더 엄격한 제한이 적용되는지 확인
  - PHP는 현재 health 반복 호출 안정성 위주라서, 추후 `/auth` 경계까지 TS 수준으로 맞출 필요가 있음

### 13. ResilienceAutoTuneE2E

- 상태: TS만 구현, PHP 추가 필요
- TypeScript 기준 이름: `resilience-auto-tune`
- 검증 내용:
  - 반복 실패 후 `/resilience/circuit-breakers/policies`가 자동 튜닝된 정책을 반환하는지 검증
  - 회복성 자동 조정 정책이 실제 실패 이력에 반응하는지 확인

### 14. SecurityRegressionE2E

- 상태: 공통
- TypeScript 기준 이름: `security-regression`
- PHP 기준 이름: `SecurityRegressionE2ETest`
- 검증 내용:
  - 인증과 인가가 필요한 민감 엔드포인트가 보호되는지 확인
  - TypeScript는 `RolesGuard`, `JwtAuthGuard`, `UploadSecurityService` 단위 성격까지 함께 검증
  - PHP는 보안 민감 API 접근 제어를 엔드투엔드 관점으로 검증

## PHP 도메인 통합 테스트 기준 목록

아래 테스트는 현재 PHP에 구현되어 있고, TypeScript에도 같은 축으로 맞춰가야 하는 완성본 목록이다.

### 15. AuthApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `AuthApiTest`
- 검증 내용:
  - 회원가입 시 사용자와 인증 코드 생성
  - 인증 확인, 로그인, 토큰 갱신, 로그아웃 흐름
  - 단순 E2E보다 더 세밀한 인증 API 회귀

### 16. UserApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `UserApiTest`
- 검증 내용:
  - 내 정보 조회/수정
  - 프로필 이미지 처리
  - 회원 탈퇴
  - 공개 프로필 조회
  - 관리자 사용자 관리 기능

### 17. CategoryApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `CategoryApiTest`
- 검증 내용:
  - 카테고리 트리 조회 및 단건 조회
  - 관리자 카테고리 생성/수정/삭제
  - 자식 카테고리가 있는 경우 삭제 차단
  - 일반 사용자 권한 차단

### 18. ProductApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `ProductApiTest`
- 검증 내용:
  - 공개 상품 목록/상세 조회
  - 관리자 상품 생성/수정/삭제
  - 일반 사용자 차단

### 19. SpecSellerPriceApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `SpecSellerPriceApiTest`
- 검증 내용:
  - 스펙 정의 및 상품 스펙 관리
  - 판매처 관리
  - 가격 데이터 관리
  - 사용자 가격 알림 관리

### 20. CartAddressApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `CartAddressApiTest`
- 검증 내용:
  - 배송지 CRUD
  - 장바구니 아이템 추가/수정/삭제
  - 사용자 장바구니 상태 반영

### 21. OrderPaymentApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `OrderPaymentApiTest`
- 검증 내용:
  - 주문 생성
  - 주문 목록/상세 조회
  - 주문 취소
  - 결제 처리
  - 주문 상태 전이와 사용자 소유권 검증

### 22. ReviewWishlistPointApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `ReviewWishlistPointApiTest`
- 검증 내용:
  - 리뷰 작성/수정/삭제
  - 리뷰에 따른 포인트 적립
  - 위시리스트 토글
  - 관리자 포인트 지급

### 23. CommunityInquirySupportApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `CommunityInquirySupportApiTest`
- 검증 내용:
  - 커뮤니티 게시글, 좋아요, 댓글 흐름
  - 상품 문의 등록 및 관리자 답변
  - 고객센터 티켓 등록, 처리, 응답 흐름

### 24. ActivityChatPushApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `ActivityChatPushApiTest`
- 검증 내용:
  - 활동 이력 관리
  - 채팅방 생성, 참여, 메시지 발송
  - 푸시 구독과 알림 설정 관리

### 25. PredictionDealRecommendationRankingApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `PredictionDealRecommendationRankingApiTest`
- 검증 내용:
  - 예측 결과 조회
  - 특가 흐름 검증
  - 추천 결과 검증
  - 랭킹 응답과 조건별 조회 검증

### 26. FraudTrustI18nImageBadgeApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `FraudTrustI18nImageBadgeApiTest`
- 검증 내용:
  - 사기 탐지/신고와 신뢰 점수 흐름
  - 다국어 리소스와 번역 응답
  - 이미지 업로드/삭제/메타데이터
  - 배지 지급 및 조회

### 27. PcFriendShortformMediaNewsMatchingApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `PcFriendShortformMediaNewsMatchingApiTest`
- 검증 내용:
  - PC 견적 저장/공유/호환성 조회
  - 친구 요청/수락/차단
  - 숏폼 목록/좋아요/댓글
  - 미디어 메타데이터와 스트리밍 관련 응답
  - 뉴스 카테고리/뉴스/관리자 수정 흐름
  - 상품 매칭 등록/조회

### 28. AnalyticsUsedMarketAutoAuctionCompareApi

- 상태: PHP만 구현, TS 확장 필요
- PHP 기준 이름: `AnalyticsUsedMarketAutoAuctionCompareApiTest`
- 검증 내용:
  - 분석 리포트와 통계 응답
  - 중고 시세 흐름
  - 자동차 관련 데이터 조회
  - 역경매 생성/참여/낙찰 흐름
  - 비교 API 결과 검증

### 29. PhpOpsApi

- 상태: PHP만 구현, TS 일부 대응, TS 확장 필요
- PHP 기준 이름: `PhpOpsApiTest`
- 검증 내용:
  - 관리자 설정
  - 헬스체크
  - 에러 코드 카탈로그
  - resilience 관련 API
  - Queue / Query / SearchSync / Crawler
  - Ops Dashboard / Observability
  - TypeScript의 여러 운영 E2E를 PHP 쪽에서는 하나의 대형 통합 테스트로 일부 묶고 있음

### 30. HealthRoute

- 상태: PHP만 구현, TS는 PublicApi/ContractPublicApi에 분산
- PHP 기준 이름: `HealthRouteTest`
- 검증 내용:
  - `/health`가 표준 API 계약에 맞는지 검증
  - TS에서는 `public-api`, `contract-public-api`가 이 역할을 분담하고 있음

## 성능 테스트 완성본 목록

아래 성능 테스트 이름은 양쪽이 동일하게 유지한다.

- `price-compare.perf`
  - 가격 비교 API의 응답 시간과 처리량 검증
- `search-ranking.perf`
  - 검색과 랭킹 조합 API의 성능 검증
- `smoke.perf`
  - 핵심 엔드포인트의 기본 성능 스모크 검증
- `soak.perf`
  - 장시간 부하 지속 시 안정성 검증
- `spike-search.perf`
  - 검색 트래픽 급증 상황 검증
- `assert-summary`
  - 성능 요약 결과의 임계치 판정
- `mock-perf-server`
  - 성능 테스트용 모의 서버

## 테스트 스크립트 완성본 목록

아래 스크립트 이름도 양쪽이 동일하게 유지한다.

- `analyze-stability`
  - 반복 실행 결과에서 flaky 패턴 분석
- `live-smoke`
  - 배포 환경 또는 유사 환경에서 최소 생존 검증
- `migration-roundtrip`
  - 마이그레이션 적용/롤백 또는 roundtrip 검증
- `validate-migrations`
  - 마이그레이션 파일의 정합성 검증

## 최종 정렬 기준

### PHP에 추가로 맞춰야 하는 TypeScript 통합 테스트

- `PublicApiE2E`
- `OpsDashboardResilienceE2E`
- `OpsDashboardThresholdsE2E`
- `ResilienceAutoTuneE2E`

### TypeScript에 추가로 맞춰야 하는 PHP 도메인 통합 테스트

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

## 앞으로의 작성 원칙

- 새 테스트 문서는 확장자를 제외한 위 기준 이름을 우선 사용한다.
- `TypeScript`는 운영/보안/플랫폼 축의 세분화된 E2E를 유지한다.
- `PHP`는 도메인 묶음 통합 테스트를 유지하되, 운영 E2E는 TypeScript와 같은 단위로 점차 분리한다.
- 양쪽 구현체가 모두 갖춰지면 이 문서의 상태 표기를 `공통`으로 갱신한다.
