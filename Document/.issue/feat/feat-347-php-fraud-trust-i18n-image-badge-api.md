---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Fraud Trust I18n Image Badge API 구현"
labels: feature
assignees: ""
issue: "[FEAT] PHP Fraud Trust I18n Image Badge API 구현"
commit: "feat: (#347) PHP Fraud Trust I18n Image Badge API 구현"
branch: "feat/#347/php-fraud-trust-i18n-image-badge-api"
---

## ✨ 기능 요약

> PBShop PHP 트랙에서 사기 탐지, 판매처 신뢰도, 다국어/환율, 이미지, 배지 API를 구현한다.

## 📋 요구사항

- [x] Fraud / Trust / I18n / Image / Badge 테이블 추가
  - [x] `fraud_flags`
  - [x] `trust_score_histories`
  - [x] `translations`
  - [x] `exchange_rates`
  - [x] `image_assets`
  - [x] `image_variants`
  - [x] `badges`
  - [x] `user_badges`
- [x] 도메인 모델 추가
  - [x] `FraudFlag`
  - [x] `TrustScoreHistory`
  - [x] `Translation`
  - [x] `ExchangeRate`
  - [x] `ImageAsset`
  - [x] `ImageVariant`
  - [x] `Badge`
  - [x] `UserBadge`
- [x] Fraud 서비스/컨트롤러 추가
  - [x] `FraudService`
  - [x] `FraudController`
- [x] Fraud API 구현
  - [x] `GET /api/v1/fraud/alerts`
  - [x] `PATCH /api/v1/fraud/alerts/{id}/approve`
  - [x] `PATCH /api/v1/fraud/alerts/{id}/reject`
  - [x] `GET /api/v1/products/{id}/real-price`
  - [x] `GET /api/v1/fraud/products/{productId}/effective-prices`
  - [x] `GET /api/v1/fraud/products/{productId}/anomalies`
  - [x] `POST /api/v1/fraud/admin/products/{productId}/scan`
  - [x] `GET /api/v1/fraud/admin/products/{productId}/flags`
- [x] Trust 서비스/컨트롤러 추가
  - [x] `TrustService`
  - [x] `TrustController`
- [x] Trust API 구현
  - [x] `GET /api/v1/trust/sellers/{sellerId}`
  - [x] `GET /api/v1/trust/sellers/{sellerId}/history`
  - [x] `POST /api/v1/trust/admin/sellers/{sellerId}/recalculate`
- [x] I18n 요청 검증/서비스/컨트롤러 추가
  - [x] `UpsertTranslationRequest`
  - [x] `UpsertExchangeRateRequest`
  - [x] `I18nService`
  - [x] `I18nController`
- [x] I18n API 구현
  - [x] `GET /api/v1/i18n/translations`
  - [x] `POST /api/v1/i18n/admin/translations`
  - [x] `DELETE /api/v1/i18n/admin/translations/{id}`
  - [x] `GET /api/v1/i18n/exchange-rates`
  - [x] `POST /api/v1/i18n/admin/exchange-rates`
  - [x] `GET /api/v1/i18n/convert`
- [x] Image 요청 검증/서비스/컨트롤러 추가
  - [x] `UploadImageRequest`
  - [x] `ImageService`
  - [x] `ImageController`
- [x] Image API 구현
  - [x] `POST /api/v1/images/upload`
  - [x] `GET /api/v1/images/{id}/variants`
  - [x] `DELETE /api/v1/images/{id}`
  - [x] 원본 + 변환본 메타데이터 생성
- [x] Badge 요청 검증/서비스/컨트롤러 추가
  - [x] `CreateBadgeRequest`
  - [x] `UpdateBadgeRequest`
  - [x] `GrantBadgeRequest`
  - [x] `BadgeService`
  - [x] `BadgeController`
- [x] Badge API 구현
  - [x] `GET /api/v1/badges`
  - [x] `GET /api/v1/badges/me`
  - [x] `GET /api/v1/users/{id}/badges`
  - [x] `POST /api/v1/admin/badges`
  - [x] `PATCH /api/v1/admin/badges/{id}`
  - [x] `DELETE /api/v1/admin/badges/{id}`
  - [x] `POST /api/v1/admin/badges/{id}/grant`
  - [x] `DELETE /api/v1/admin/badges/{id}/revoke/{userId}`
- [x] 라우트 파일 분리 및 등록
  - [x] `routes/api_v1/fraud.php`
  - [x] `routes/api_v1/trust.php`
  - [x] `routes/api_v1/i18n.php`
  - [x] `routes/api_v1/images.php`
  - [x] `routes/api_v1/badges.php`
  - [x] `routes/api_v1.php`에 loader 연결
- [x] 통합 테스트 추가
  - [x] `tests/Feature/Api/FraudTrustI18nImageBadgeApiTest.php`
  - [x] 사기 탐지/실제 가격/신뢰도 검증
  - [x] 번역/환율/이미지 업로드 검증
  - [x] 배지 생성/부여/회수 검증
  - [x] `php artisan test tests/Feature/Api/FraudTrustI18nImageBadgeApiTest.php` 통과
- [x] 라우트 검증
  - [x] `php artisan route:list | Select-String 'fraud|trust|i18n|images|badges'` 검증
