# TypeScript Language API Specification

## 1. 목적

본 문서는 [02_api-specification.md](C:/00_work/00_work/07_project-bible/Project_Bible/Document/02_api-specification.md)를 TypeScript/NestJS 구현 기준으로 해석한 언어별 API 명세다.
공통 API 계약은 유지하되, TypeScript 트랙에서 사용하는 애플리케이션 구조와 검증 규칙을 정의한다.

## 2. 적용 범위

- 프레임워크: NestJS
- 기본 구현 위치: `BackEnd/TypeScript`
- 검증 기준: e2e test, contract test, perf smoke, CI workflow

## 3. 공통 계약 원칙

- URI, HTTP Method, 리소스 역할은 공통 API 명세를 따른다.
- DTO와 내부 타입 정의는 구현 편의를 위한 것이며 외부 계약을 바꾸지 않는다.
- 공개 API와 관리자 API는 controller, guard, test에서 분리한다.
- NestJS 모듈 구조는 자유롭지만 계약은 문서 기준과 일치해야 한다.

## 4. 라우팅 규칙

- 버전 prefix와 리소스 경로는 공통 API 명세를 따른다.
- 공개/관리자/운영 경로는 controller 레벨에서 명확히 분리한다.
- 필요 시 global prefix를 사용하되, 외부 노출 경로는 문서 기준을 유지한다.

## 5. 요청 처리 규칙

- 입력 검증은 DTO + validation pipe 기준으로 처리한다.
- 인증은 guard 또는 interceptor와 연동된 토큰 방식으로 처리한다.
- 권한은 role guard, custom guard, header auth 등으로 강제할 수 있다.
- 검색/필터/페이지네이션 쿼리는 공통 명세 키를 우선한다.

## 6. 응답 규칙

- JSON 응답을 기본으로 한다.
- 성공 응답은 `data` 루트를 기본으로 사용한다.
- 컬렉션 응답은 구현에 따라 `data` 또는 `data.items`를 사용할 수 있으나, 공통 계약과 테스트에서 일치해야 한다.
- 예외 응답은 Nest 기본 예외를 그대로 노출하지 않고 프로젝트 규약에 맞춰 정리한다.

## 7. 애플리케이션 구조 규칙

- controller, service, dto, guard, test factory를 기본 단위로 본다.
- mock 기반 테스트 환경과 실제 앱 부트스트랩 환경을 구분할 수 있어야 한다.
- 외부 연동이 없는 경우에도 계약 회귀를 검증할 수 있는 test harness를 유지한다.

## 8. 문서 및 계약 산출

- OpenAPI/Swagger 또는 계약 검증 테스트를 유지해야 한다.
- controller/DTO 변경이 있으면 관련 e2e 및 contract 테스트도 같이 갱신한다.
- 필요 시 mock server 기반 성능 smoke 자산을 함께 유지한다.

## 9. 테스트 기준

- e2e test로 핵심 API 흐름을 검증한다.
- contract test로 공개 API 계약을 고정한다.
- admin boundary, security regression, rate limit 계열 검증 경로를 유지한다.
- perf smoke는 자동 또는 수동 CI 단계에서 재사용 가능해야 한다.

## 10. CI 연계 기준

- `quality`, `e2e-critical`, `perf-smoke`를 자동 계층으로 둔다.
- 수동 게이트에서는 `release-gate`, 계약/보안/운영/확장 성능 검증을 수행할 수 있어야 한다.
- mock server와 테스트 앱 팩토리를 활용해 재현 가능한 실행 구조를 유지한다.

## 11. 완료 기준

TypeScript 트랙이 API 구현 완료 상태라고 판단하려면 다음을 만족해야 한다.

1. 공통 API 명세의 핵심 엔드포인트가 NestJS controller에 구현되어 있다.
2. DTO, guard, 예외 처리 규칙이 외부 계약과 일치한다.
3. e2e/contract/security/perf smoke 검증 경로가 있다.
4. CI에서 핵심 회귀와 성능 smoke를 자동 실행할 수 있다.
5. 계약 변경 시 테스트와 문서 산출이 함께 갱신된다.
