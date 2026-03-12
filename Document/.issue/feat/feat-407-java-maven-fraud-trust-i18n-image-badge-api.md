---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Java Maven Fraud Trust I18n Image Badge API 구현"
labels: feature
assignees: ""
issue: "[FEAT] Java Maven Fraud Trust I18n Image Badge API 구현"
commit: "feat: (#407) Java Maven Fraud Trust I18n Image Badge API 구현"
branch: "feat/#407/java-maven-fraud-trust-i18n-image-badge-api"
---

## ✨ 기능 요약

> Java Maven 트랙에서 사기 탐지, 판매처 신뢰도, 다국어/환율, 이미지, 배지 API를 구현한다.

## 📋 요구사항

- [x] `V11__fraud_trust_i18n_image_badge_support.sql` 마이그레이션 추가
- [x] Fraud / Trust / I18n / Image / Badge 테이블 추가
  - [x] `fraud_flags`
  - [x] `trust_score_histories`
  - [x] `translations`
  - [x] `exchange_rates`
  - [x] `image_assets`
  - [x] `image_variants`
  - [x] `badges`
  - [x] `user_badges`
- [x] Fraud 도메인 추가
  - [x] `FraudFlag`
  - [x] `FraudFlagRepository`
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
- [x] Trust 도메인 추가
  - [x] `TrustScoreHistory`
  - [x] `TrustScoreHistoryRepository`
  - [x] `TrustService`
  - [x] `TrustController`
- [x] Trust API 구현
  - [x] `GET /api/v1/trust/sellers/{sellerId}`
  - [x] `GET /api/v1/trust/sellers/{sellerId}/history`
  - [x] `POST /api/v1/trust/admin/sellers/{sellerId}/recalculate`
- [x] I18n 도메인/DTO 추가
  - [x] `Translation`
  - [x] `ExchangeRate`
  - [x] `TranslationRepository`
  - [x] `ExchangeRateRepository`
  - [x] `I18nService`
  - [x] `I18nController`
  - [x] `I18nDtos`
- [x] I18n API 구현
  - [x] `GET /api/v1/i18n/translations`
  - [x] `POST /api/v1/i18n/admin/translations`
  - [x] `DELETE /api/v1/i18n/admin/translations/{id}`
  - [x] `GET /api/v1/i18n/exchange-rates`
  - [x] `POST /api/v1/i18n/admin/exchange-rates`
  - [x] `GET /api/v1/i18n/convert`
- [x] Image 도메인 추가
  - [x] `ImageAsset`
  - [x] `ImageVariant`
  - [x] `ImageAssetRepository`
  - [x] `ImageVariantRepository`
  - [x] `ImageService`
  - [x] `ImageController`
- [x] Image API 구현
  - [x] `POST /api/v1/images/upload`
  - [x] `GET /api/v1/images/{id}/variants`
  - [x] `DELETE /api/v1/images/{id}`
  - [x] 원본 + 변환본 메타데이터 생성
- [x] Badge 도메인/DTO 추가
  - [x] `Badge`
  - [x] `UserBadge`
  - [x] `BadgeRepository`
  - [x] `UserBadgeRepository`
  - [x] `BadgeService`
  - [x] `BadgeController`
  - [x] `BadgeDtos`
- [x] Badge API 구현
  - [x] `GET /api/v1/badges`
  - [x] `GET /api/v1/badges/me`
  - [x] `GET /api/v1/users/{id}/badges`
  - [x] `POST /api/v1/admin/badges`
  - [x] `PATCH /api/v1/admin/badges/{id}`
  - [x] `DELETE /api/v1/admin/badges/{id}`
  - [x] `POST /api/v1/admin/badges/{id}/grant`
  - [x] `DELETE /api/v1/admin/badges/{id}/revoke/{userId}`
- [x] `SecurityConfig` 공개 경로 반영
- [x] `FlywayMigrationTest`를 v11 기준으로 갱신
- [x] 통합 테스트 추가
  - [x] `FraudTrustI18nImageBadgeApiTest`
  - [x] 사기 탐지/실제 가격/신뢰도 검증
  - [x] 번역/환율/이미지 업로드 검증
  - [x] 배지 생성/부여/회수 검증
- [x] 전체 회귀 검증 통과
  - [x] `cmd /c mvnw.cmd -Dtest=FraudTrustI18nImageBadgeApiTest test`
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- `FraudService`는 기존 `price_entries` 이력을 사용해 실구매가와 평균 대비 이상 가격을 계산한다.
- `TrustService`는 판매처 상태와 등록된 가격 이력을 바탕으로 점수/등급 히스토리를 생성한다.
- `I18nService`는 번역 upsert와 환율 환산을 단순 CRUD + 계산형으로 맞췄다.
- `ImageService`는 실제 스토리지 연동 대신 메타데이터와 파생 variant 레코드를 생성하는 방식으로 구현했다.
- 이미지 삭제 시 변환본 레코드를 먼저 정리하도록 수정해 JPA transient flush 문제를 해결했다.
