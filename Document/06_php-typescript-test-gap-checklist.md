# 06 PHP TypeScript Test Gap Checklist

## 목적

이 문서는 [05_test-code-list.md](c:/00_work/00_work/03_Project_Bible/Project_Bible/Document/05_test-code-list.md)를 기준으로, `PHP`와 `TypeScript` 사이에 아직 맞춰지지 않은 통합 테스트를 어떤 순서로 작성해야 하는지 정리한 실행 체크리스트다.

기준 원칙:
- `TypeScript`의 세분화된 운영 E2E를 `PHP`에 먼저 맞춘다.
- `PHP`의 도메인 통합 테스트를 `TypeScript`에 확장한다.
- 이름은 확장자를 제외한 통합 기준 이름을 사용한다.

## 1. PHP에 먼저 추가해야 할 테스트

### P0

- [ ] `PublicApiE2E`
  - `/health` 래핑 응답 검증
  - `/errors/codes` 코드 카탈로그 응답 검증
  - 현재 `ContractPublicApiE2E`와 `HealthRoute`에 분산된 검증을 사용자 관점 공개 API 테스트로 분리

- [ ] `OpsDashboardResilienceE2E`
  - 부분 장애 상황에서 `/admin/ops-dashboard/summary`가 `degraded` 상태를 정확히 반환하는지 검증
  - resilience 관련 요약 지표가 summary에 반영되는지 확인

- [ ] `OpsDashboardThresholdsE2E`
  - 실패 수가 임계치보다 낮을 때 경고가 억제되는지 검증
  - 실패 수가 임계치에 도달했을 때 경고가 발생하는지 검증

- [ ] `ResilienceAutoTuneE2E`
  - 반복 실패 후 정책 튜닝 결과가 `/resilience/circuit-breakers/policies`에 반영되는지 검증
  - 자동 튜닝 정책이 실제 실패 상태를 반영하는지 확인

## 2. TypeScript에 추가해야 할 테스트

### P0

- [ ] `AuthApi`
  - 회원가입, 인증 확인, 로그인, 토큰 갱신, 로그아웃

- [ ] `UserApi`
  - 내 정보 조회/수정
  - 프로필 이미지
  - 회원 탈퇴
  - 공개 프로필
  - 관리자 사용자 관리

- [ ] `ProductApi`
  - 상품 목록/상세
  - 관리자 상품 관리
  - 권한 차단

- [ ] `OrderPaymentApi`
  - 주문 생성
  - 주문 조회
  - 주문 취소
  - 결제 처리

### P1

- [ ] `CategoryApi`
  - 카테고리 트리/단건 조회
  - 관리자 카테고리 관리
  - 자식 카테고리 삭제 차단

- [ ] `SpecSellerPriceApi`
  - 스펙 정의/상품 스펙
  - 판매처 관리
  - 가격 관리
  - 가격 알림

- [ ] `CartAddressApi`
  - 배송지 CRUD
  - 장바구니 아이템 관리

- [ ] `ReviewWishlistPointApi`
  - 리뷰 CRUD
  - 포인트 적립/지급
  - 위시리스트 토글

- [ ] `HealthRoute`
  - `/health` 단독 계약 테스트
  - 현재 `PublicApiE2E`, `ContractPublicApiE2E`에 분산된 health 확인을 단일 회귀 축으로 보강

### P2

- [ ] `CommunityInquirySupportApi`
  - 커뮤니티 게시글/좋아요/댓글
  - 상품 문의
  - 고객센터 티켓

- [ ] `ActivityChatPushApi`
  - 활동 이력
  - 채팅방/메시지
  - 푸시 구독/설정

- [ ] `PredictionDealRecommendationRankingApi`
  - 예측
  - 특가
  - 추천
  - 랭킹

- [ ] `FraudTrustI18nImageBadgeApi`
  - 사기 탐지
  - 신뢰 점수
  - 다국어
  - 이미지
  - 배지

- [ ] `PcFriendShortformMediaNewsMatchingApi`
  - PC 견적
  - 친구
  - 숏폼
  - 미디어
  - 뉴스
  - 매칭

- [ ] `AnalyticsUsedMarketAutoAuctionCompareApi`
  - 분석
  - 중고 시세
  - 자동차
  - 역경매
  - 비교

- [ ] `PhpOpsApi`
  - 관리자 설정
  - 헬스
  - 에러 코드
  - resilience
  - queue/query/searchsync/crawler
  - ops dashboard/observability

## 3. 권장 작성 순서

### PHP

1. `PublicApiE2E`
2. `OpsDashboardResilienceE2E`
3. `OpsDashboardThresholdsE2E`
4. `ResilienceAutoTuneE2E`

### TypeScript

1. `AuthApi`
2. `UserApi`
3. `ProductApi`
4. `OrderPaymentApi`
5. `CategoryApi`
6. `SpecSellerPriceApi`
7. `CartAddressApi`
8. `ReviewWishlistPointApi`
9. 나머지 확장 도메인 통합 테스트

## 4. 완료 기준

- PHP는 TypeScript 운영 E2E 축을 모두 갖춰야 한다.
- TypeScript는 PHP 도메인 통합 테스트 축을 모두 갖춰야 한다.
- 테스트 이름은 이 문서 기준 이름으로 관리한다.
- 문서와 실제 테스트 파일 목록이 다르면 이 문서를 먼저 갱신한다.
