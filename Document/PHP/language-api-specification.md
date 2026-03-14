# PHP Language API Specification

## 1. 목적

본 문서는 [02_api-specification.md](C:/00_work/00_work/07_project-bible/Project_Bible/Document/02_api-specification.md)를 PHP/Laravel 구현 기준으로 해석한 언어별 API 명세다.
공통 API 계약은 유지하되, PHP 트랙에서 사용하는 구현 규칙과 응답 규약을 정의한다.

## 2. 적용 범위

- 프레임워크: Laravel
- 기본 구현 위치: `BackEnd/PHP`
- 검증 기준: Feature test, E2E test, CI workflow, route snapshot

## 3. 공통 계약 원칙

- URI, HTTP Method, 역할 분리는 공통 API 명세를 따른다.
- 요청/응답 필드 이름은 공통 API 명세와 호환되어야 한다.
- PHP 구현체는 Laravel 관례를 사용하더라도 외부 계약은 변경하지 않는다.
- 관리자 API와 공개 API는 라우트, 권한, 테스트에서 명확히 분리한다.

## 4. 라우팅 규칙

- 공개 API는 버전 prefix 기반으로 구성한다.
- 관리자/운영 API는 별도 prefix를 사용한다.
- Laravel `api.php` 라우트 정의를 기본으로 한다.
- route naming은 테스트와 snapshot 검증이 가능하도록 일관되게 유지한다.

## 5. 요청 처리 규칙

- 입력 검증은 Form Request 또는 Validator 계층에서 수행한다.
- 인증은 Laravel guard 또는 토큰 기반 인증 계층에서 처리한다.
- 권한 검증은 middleware, policy, gate 중 하나 이상의 방식으로 강제한다.
- 검색, 필터, 페이지네이션 파라미터는 공통 명세의 키 이름을 우선한다.

## 6. 응답 규칙

- JSON 응답을 기본으로 한다.
- 성공 응답은 `data` 루트를 기본으로 사용한다.
- 목록 응답은 필요 시 `data`, `meta` 구조를 사용한다.
- 오류 응답은 HTTP status와 함께 일관된 오류 코드 구조를 제공해야 한다.
- 프레임워크 기본 오류를 그대로 노출하지 않는다.

## 7. 데이터 계층 규칙

- Eloquent 모델, Repository, Service 조합은 구현 선택 사항이다.
- 다만 API 응답 계약은 ORM 구조에 종속되지 않아야 한다.
- 마이그레이션은 Laravel migration 기준으로 관리한다.
- 시드/팩토리는 테스트 재현성을 보장해야 한다.

## 8. 문서 및 계약 산출

- route snapshot 또는 이에 준하는 산출물을 유지한다.
- Swagger/OpenAPI를 직접 생성하지 않더라도 API 계약 검증 경로는 있어야 한다.
- 변경 시 테스트와 산출물이 함께 갱신되어야 한다.

## 9. 테스트 기준

- Feature test로 요청/응답 계약을 검증한다.
- E2E test로 핵심 사용자 흐름과 관리자 흐름을 검증한다.
- DB integration test로 마이그레이션과 영속성 흐름을 검증한다.
- 보안, 권한, 회귀 검증은 CI 수동 게이트와 연계할 수 있다.

## 10. CI 연계 기준

- `quality`, `api-regression`, `db-integration` 계층을 기본으로 둔다.
- 수동 게이트에서는 `release-gate`, `route-snapshot-manual`, 보안/마이그레이션/성능 확장 검증을 지원한다.
- API 계약 변경은 테스트와 CI 산출물로 추적 가능해야 한다.

## 11. 완료 기준

PHP 트랙이 API 구현 완료 상태라고 판단하려면 다음을 만족해야 한다.

1. 공통 API 명세의 엔드포인트가 구현되어 있다.
2. 공개 API와 관리자 API의 권한 경계가 동작한다.
3. Feature/E2E/DB integration 테스트가 준비되어 있다.
4. CI에서 핵심 회귀와 DB 검증이 가능하다.
5. 라우트 또는 계약 산출물로 변경 추적이 가능하다.
