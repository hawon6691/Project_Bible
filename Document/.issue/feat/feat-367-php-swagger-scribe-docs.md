---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] PHP Laravel Swagger/Scribe API 문서 체계 추가"
labels: feature
issue: "[FEAT] PHP Laravel Swagger/Scribe API 문서 체계 추가"
commit: "feat: (#367) PHP Laravel에 Swagger UI와 Scribe API 문서 체계 추가"
branch: "feat/#367/php-swagger-scribe-docs"
assignees: ""
---

## ✨ 기능 요약

> PHP Laravel 백엔드에 `L5-Swagger`와 `Scribe`를 함께 도입하고, Swagger UI가 전체 API를 표시할 수 있도록 Scribe OpenAPI 스펙과 연결했습니다. 또한 `auth`, `users`, `products`, `categories`, `system` 핵심 컨트롤러에는 상세 OpenAPI 속성을 직접 추가하고, 나머지 API 컨트롤러 전체에는 공통 `OA\Tag`를 적용해 모듈 단위 Swagger 분류를 정리했습니다. 공통 응답 스키마(`success/data/meta/error`)도 컴포넌트로 분리했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Laravel 프로젝트에 `darkaonline/l5-swagger` 설치
- [x] Laravel 프로젝트에 `knuckleswtf/scribe` 설치
- [x] L5-Swagger 설정 publish 및 기본 문서 경로 구성
- [x] Scribe 설정 publish 및 문서 경로 `/docs/api` 구성
- [x] Swagger UI 경로 `/docs/swagger` 구성
- [x] Scribe OpenAPI 경로 `/docs/api.openapi` 생성 및 확인
- [x] Swagger UI가 Scribe OpenAPI 스펙을 읽도록 연결해 전체 API가 보이게 조정
- [x] OpenAPI 기본 메타데이터 추가 (`app/OpenApi/OpenApiSpec.php`)
- [x] `SystemController`에 기본 OpenAPI 속성 추가
- [x] `AuthController`에 핵심 엔드포인트 Swagger 속성 추가
- [x] `UserController`에 핵심 엔드포인트 Swagger 속성 추가
- [x] `ProductController`에 핵심 엔드포인트 Swagger 속성 추가
- [x] `CategoryController`에 핵심 엔드포인트 Swagger 속성 추가
- [x] 공통 응답 스키마 컴포넌트 분리
  - [x] `ApiSuccessEnvelope`
  - [x] `ApiErrorEnvelope`
  - [x] `ApiMeta`
  - [x] `ApiError`
- [x] 나머지 API 컨트롤러 전체에 `use OpenApi\Attributes as OA;` 적용
- [x] 나머지 API 컨트롤러 전체에 클래스 단위 `#[OA\Tag(...)]` 적용
- [x] PHP README에 문서 경로 추가
  - [x] `/docs/api`
  - [x] `/docs/swagger`
  - [x] `/docs/api.openapi`
- [x] 문서 생성 명령 검증
  - [x] `php artisan scribe:generate`
  - [x] `php artisan l5-swagger:generate`
- [x] 문서 페이지 응답 검증
  - [x] `http://127.0.0.1:8000/docs/api`
  - [x] `http://127.0.0.1:8000/docs/swagger`
  - [x] `http://127.0.0.1:8000/docs/api.openapi`

## 구현 메모

- [x] `Scribe`는 라우트 기준으로 전체 API 문서를 빠르게 구성
- [x] `L5-Swagger`는 Swagger UI 제공 및 코드 기반 OpenAPI 속성 확장 기반으로 사용
- [x] Swagger UI 기본 문서 URL이 잘못 조합되는 문제를 커스텀 뷰에서 수정
- [x] 전체 기능을 Swagger UI에서 바로 보이게 하기 위해 Swagger UI가 Scribe OpenAPI 스펙을 읽도록 연결
- [x] 주요 핵심 모듈은 별도로 컨트롤러 속성까지 추가해 코드 기반 문서 정의를 시작
- [x] 공통 응답 엔벨로프를 `app/OpenApi/Components/ApiResponseSchemas.php`로 분리
- [x] `ApiController`를 상속하는 API 컨트롤러 50개 전부에 클래스 단위 `OA\Tag` 적용 확인
- [x] 검증 결과
  - [x] `php artisan l5-swagger:generate`
  - [x] `php artisan scribe:generate`
  - [x] `GET /docs/api` -> `200`
  - [x] `GET /docs/swagger` -> `200`
  - [x] `GET /docs/api.openapi` -> `200`

## 범위 메모

- [x] Swagger UI에서 전체 기능 문서 노출은 `Scribe` 생성 OpenAPI를 기준으로 제공
- [x] 핵심 모듈은 메서드 단위 OpenAPI 속성 상세화 완료
- [x] 나머지 모듈은 현재 클래스 단위 태그 분류까지 우선 적용
- [ ] 모든 개별 엔드포인트에 수동 `OpenApi\Attributes` 상세 명세를 100% 작성하는 작업은 후속 확장 범위
