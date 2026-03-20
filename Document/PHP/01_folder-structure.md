# PHP Folder Structure

## 1. 개요

`BackEnd/PHP/php-laravel-composer-eloquent-postgresql`은 `PBShop`의 PHP 기준 구현체이며, Laravel을 기반으로 API 중심 구조로 정리한다.

## 2. 최상위 디렉터리

```text
BackEnd/PHP/php-laravel-composer-eloquent-postgresql
├─ app
├─ bootstrap
├─ config
├─ database
├─ public
├─ resources
├─ routes
├─ storage
├─ tests
└─ vendor
```

## 3. 애플리케이션 구조

```text
app
├─ Common
│  ├─ Constants
│  ├─ Exceptions
│  └─ Http
├─ Http
│  ├─ Controllers
│  │  └─ Api/V1
│  └─ Middleware
├─ Models
├─ Modules
│  ├─ Auth
│  ├─ User
│  ├─ Category
│  ├─ Product
│  ├─ Spec
│  ├─ Seller
│  ├─ Price
│  ├─ Cart
│  ├─ Address
│  ├─ Order
│  ├─ Payment
│  ├─ Review
│  ├─ Wishlist
│  ├─ Point
│  ├─ Community
│  ├─ Inquiry
│  ├─ Support
│  ├─ Activity
│  ├─ Chat
│  ├─ Push
│  ├─ Prediction
│  ├─ Deal
│  ├─ Recommendation
│  ├─ Ranking
│  ├─ Fraud
│  ├─ Trust
│  ├─ I18n
│  ├─ Image
│  ├─ Badge
│  ├─ PcBuilder
│  ├─ Friend
│  ├─ Shortform
│  ├─ Media
│  ├─ News
│  ├─ Matching
│  ├─ Analytics
│  ├─ UsedMarket
│  ├─ Auto
│  ├─ Auction
│  ├─ Compare
│  ├─ AdminSettings
│  ├─ ErrorCode
│  ├─ Resilience
│  ├─ QueueAdmin
│  ├─ OpsDashboard
│  ├─ Observability
│  ├─ Query
│  ├─ SearchSync
│  └─ Crawler
│  ├─ Requests
│  └─ Services
└─ Providers
```

## 4. 라우트 구조

```text
routes
├─ api_v1.php
└─ api_v1
   ├─ auth.php
   ├─ users.php
   ├─ categories.php
   ├─ products.php
   ├─ cart.php
   ├─ addresses.php
   ├─ orders.php
   ├─ payments.php
   ├─ reviews.php
   ├─ wishlist.php
   ├─ points.php
   ├─ community.php
   ├─ inquiries.php
   ├─ support.php
   ├─ activity.php
   ├─ chat.php
   ├─ push.php
   ├─ deals.php
   ├─ recommendations.php
   ├─ rankings.php
   ├─ fraud.php
   ├─ trust.php
   ├─ i18n.php
   ├─ images.php
   ├─ badges.php
   ├─ prediction.php
   ├─ specs.php
   ├─ sellers.php
   ├─ prices.php
   └─ ...
```

## 5. 데이터베이스 구조

```text
database
├─ factories
├─ migrations
├─ seeders
└─ sqlite
```

- 마이그레이션은 기능 단계별로 분리해 관리한다.
- 기준 DB는 MySQL(`pbdb`)이며, 공통 계약은 `Document/03_erd.md`를 따른다.

## 6. 테스트 구조

```text
tests
├─ Feature
│  └─ Api
└─ Unit
```

- API 테스트는 도메인 단위 Feature Test로 유지한다.
- 현재 구현 검증은 `tests/Feature/Api/*.php`에서 수행한다.

## 7. 설계 원칙

- Controller는 요청/응답과 권한 확인에 집중한다.
- 비즈니스 규칙은 `app/Modules/*/Services`에 둔다.
- 입력 검증은 `Requests`로 분리한다.
- 공통 응답 포맷은 `app/Common/Http/ApiResponse.php`를 사용한다.
- 예외는 공통 API 포맷으로 렌더링한다.
