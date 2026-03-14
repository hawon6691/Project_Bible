# Java Language API Specification

## 1. 목적

본 문서는 [02_api-specification.md](C:/00_work/00_work/07_project-bible/Project_Bible/Document/02_api-specification.md)를 Java/Spring Boot 구현 기준으로 해석한 언어별 API 명세다.
공통 API 계약은 유지하되, Java 트랙에서 사용하는 Spring 계층 구조와 문서/테스트 기준을 정의한다.

## 2. 적용 범위

- 프레임워크: Spring Boot
- 빌드 기준: Maven
- 기본 구현 위치: `BackEnd/Java/springshop_maven`
- 검증 기준: integration test, e2e test, Flyway validation, Swagger/OpenAPI, CI workflow

## 3. 공통 계약 원칙

- URI, HTTP Method, 리소스 역할은 공통 API 명세를 따른다.
- Spring MVC 구현 세부 사항은 외부 계약에 영향을 주지 않아야 한다.
- 공개 API와 관리자/운영 API는 controller, security rule, test에서 분리한다.
- DTO와 엔티티는 분리하는 것을 기본 원칙으로 한다.

## 4. 라우팅 규칙

- 공통 API 명세의 버전 prefix와 리소스 경로를 유지한다.
- 공개, 관리자, 운영 엔드포인트는 별도 controller 또는 path prefix로 분리한다.
- actuator 성격의 운영 경로와 비즈니스 API를 혼합하지 않는다.

## 5. 요청 처리 규칙

- 입력 검증은 Bean Validation과 request DTO 기준으로 처리한다.
- 인증은 Spring Security 필터 체인 기준으로 처리한다.
- 권한은 role/authority 또는 별도 security matcher로 강제한다.
- 검색, 필터, 페이지네이션 쿼리는 공통 API 명세 키를 우선한다.

## 6. 응답 규칙

- JSON 응답을 기본으로 한다.
- 성공 응답은 `data` 루트를 기본으로 사용한다.
- 컬렉션 응답 구조는 테스트와 Swagger 산출물에서 일치해야 한다.
- 오류 응답은 공통 오류 코드와 status 구조를 유지해야 한다.
- Spring 기본 예외 메시지를 그대로 외부에 노출하지 않는다.

## 7. 애플리케이션 구조 규칙

- controller, service, repository, dto 계층을 기본 구조로 본다.
- 영속성 계층은 JPA 구현 세부에 묶이지 않도록 API DTO와 분리한다.
- 마이그레이션은 Flyway 기준으로 관리한다.
- 테스트 프로필과 CI 프로필은 로컬 실행과 분리해 유지한다.

## 8. 문서 및 계약 산출

- Swagger/OpenAPI 산출 경로를 유지한다.
- 보안 설정은 문서 산출 경로 접근을 허용해야 한다.
- API 구조 변경 시 integration/e2e test와 Swagger 산출이 함께 갱신되어야 한다.

## 9. 테스트 기준

- integration test로 요청/응답 계약과 데이터 흐름을 검증한다.
- e2e test로 공개 API, 관리자 API, 운영 API를 검증한다.
- Flyway migration 검증을 유지한다.
- 보안, 레이트 리밋, 회복성, 운영 대시보드 계열 검증을 지원한다.

## 10. CI 연계 기준

- `quality`, `platform-e2e`, `swagger-export`를 기본 자동 계층으로 둔다.
- 수동 게이트에서는 `release-gate`, `contract-e2e-manual`, `migration-validation-manual`, `live-smoke-manual` 및 확장 운영 검증을 지원한다.
- OpenAPI 산출과 테스트 결과는 아티팩트로 재사용 가능해야 한다.

## 11. 완료 기준

Java Maven 트랙이 API 구현 완료 상태라고 판단하려면 다음을 만족해야 한다.

1. 공통 API 명세의 핵심 엔드포인트가 Spring controller에 구현되어 있다.
2. Spring Security 규칙이 공개/관리자/운영 API 경계를 강제한다.
3. integration/e2e/Flyway/Swagger 검증 경로가 있다.
4. CI에서 핵심 회귀와 문서 산출을 자동 검증할 수 있다.
5. 계약 변경 시 테스트, Swagger, 운영 검증이 함께 갱신된다.
