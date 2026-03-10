---
title: "[FEAT] PHP 남은 Swagger Path 문서 보강"
labels: ["documentation", "php", "swagger", "openapi"]
issue: "[FEAT] PHP 남은 Swagger Path 문서 보강"
commit: "feat: (#369) PHP 남은 swagger path 문서 보강"
branch: "feat/#369/php-swagger-remaining-paths"
---

## ✨ 기능 요약
- PHP Laravel 백엔드에서 수동 `OpenApi\\Attributes`가 비어 있던 나머지 API 구간을 `app/OpenApi/Paths` 기준으로 보강한다.
- Swagger 생성 명령 실제 실행 결과를 기준으로 현재 누락/충돌 상태를 기록한다.

## 📋 요구사항
- [x] 남아 있던 Swagger 미문서 API 범위 식별
  - [x] Commerce/Order 계열
  - [x] Community/Chat/Push/Friend 계열
  - [x] Prediction/Deal/Recommendation/Ranking 계열
  - [x] Fraud/Trust/I18n/Image/Badge 계열
  - [x] PC Builder 계열
- [x] 중앙 OpenAPI Path 문서 클래스 추가
  - [x] `BackEnd/PHP/laravelshop/app/OpenApi/Paths/CommerceApiPaths.php`
  - [x] `BackEnd/PHP/laravelshop/app/OpenApi/Paths/CommunityMediaApiPaths.php`
  - [x] `BackEnd/PHP/laravelshop/app/OpenApi/Paths/OpsAndAnalyticsApiPaths.php`
- [x] 기존 공통 컴포넌트 재사용
  - [x] `#/components/schemas/ApiSuccessEnvelope`
  - [x] `#/components/schemas/ApiErrorEnvelope`
- [x] Swagger 생성 명령 실제 실행
  - [x] `php artisan l5-swagger:generate`
  - [x] 현재 실행 결과가 실패(`exit code 1`)함을 확인
- [x] 생성 실패 사실을 후속 조치 항목으로 기록

## 📌 확인 결과
- 이번 작업으로 수동 OpenAPI Path 문서가 비어 있던 주요 API 구간을 중앙 클래스 기준으로 보강했다.
- 실제 생성 명령 `php artisan l5-swagger:generate` 는 현재 환경에서 `exit code 1`로 실패했다.
- 현재 셸에서는 stderr 출력이 비어 있어 즉시 상세 stack trace를 확보하지 못했으므로, Swagger 생성 자체는 후속 충돌 해소가 필요하다.

## 📂 영향 범위
- `BackEnd/PHP/laravelshop/app/OpenApi/Paths/CommerceApiPaths.php`
- `BackEnd/PHP/laravelshop/app/OpenApi/Paths/CommunityMediaApiPaths.php`
- `BackEnd/PHP/laravelshop/app/OpenApi/Paths/OpsAndAnalyticsApiPaths.php`

## ✅ 완료 기준
- [x] 남은 수동 Swagger Path 문서 추가
- [x] 실제 Swagger 생성 명령 실행
- [ ] Swagger 생성 성공 및 산출물 확인
- [ ] 생성 실패 원인 추적 및 수정
