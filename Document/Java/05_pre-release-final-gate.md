# Java Pre-Release Final Gate

## 대상

- 트랙: Java
- 기준 구현체: `BackEnd/Java/java-spring-maven-jpa-postgresql`

## 목적

본 문서는 릴리스 직전 Java Maven JPA 트랙의 최종 점검 기준을 정의한다.

## 필수 확인 항목

다음 항목은 모두 통과해야 한다.

1. `quality`
2. `platform-e2e`
3. `swagger-export`
4. `migration-validation`
5. `security-regression`
6. `release-gate`

## 권장 추가 확인 항목

운영 상황에 따라 다음도 수행한다.

1. `dependency-failure`
2. `stability-check`
3. `perf-smoke`
4. `perf-extended`
5. `live-smoke`

## 문서 확인 항목

릴리스 전 다음 문서 상태를 확인한다.

- `01_folder-structure.md`
- `02_runbook.md`
- `03_implementation-status.md`
- `04_report.md`
- `06_requirements-api-gap-analysis.md`

## 운영 확인 항목

- Swagger UI 경로 접근 가능
- OpenAPI JSON 산출 가능
- Flyway migration validate 가능
- 관리자/운영 경계 테스트 성공
- 보안 회귀 테스트 성공

## 실패 시 처리

- CI artifact 로그 확인
- surefire reports 확인
- Swagger export 로그 확인
- migration 관련 설정과 datasource classpath 확인

## 최종 판단

다음 조건을 모두 만족하면 릴리스 가능 상태로 본다.

1. 필수 CI 항목 통과
2. 치명적 갭 없음
3. 문서 갱신 완료
4. 운영 검증 경로 확인 완료
