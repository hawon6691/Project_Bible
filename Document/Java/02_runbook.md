# Java Runbook

## 대상

- 트랙: Java
- 기준 구현체: `BackEnd/Java/springshop_maven_jpa`

## 기본 실행

로컬 기본 실행:

```bash
./mvnw spring-boot:run
```

CI 프로필 실행:

```bash
./mvnw -Dspring-boot.run.profiles=ci spring-boot:run
```

## 테스트 실행

전체 테스트:

```bash
./mvnw test
```

검증 포함 전체 빌드:

```bash
./mvnw clean verify
```

특정 테스트 클래스 실행:

```bash
./mvnw "-Dtest=PublicApiE2ETest,SwaggerDocsE2ETest" test
```

## Swagger/OpenAPI 확인

- Swagger UI: `/docs/swagger`
- OpenAPI JSON: `/docs/openapi`

확인 절차:

1. 애플리케이션 실행
2. Swagger UI 접근 확인
3. OpenAPI JSON 응답 확인
4. 보안 설정이 문서 경로를 차단하지 않는지 확인

## 마이그레이션 확인

기준 도구:

- Flyway

운영 절차:

1. migration validate 실행
2. roundtrip 필요 시 별도 검증
3. schema history 충돌 여부 확인

## CI 기준

자동 실행:

- `quality`
- `platform-e2e`
- `swagger-export`
- `perf-smoke`

수동 실행:

- `release-gate`
- `contract-e2e`
- `migration-validation`
- `migration-roundtrip`
- `stability-check`
- `security-regression`
- `admin-boundary`
- `rate-limit-regression`
- `dependency-failure`
- `perf-smoke`
- `perf-extended`
- `live-smoke`

## 장애 대응

### Swagger export 실패

- 실행 프로필이 `ci`인지 확인
- H2 드라이버가 runtime classpath에 포함되는지 확인
- 포트와 문서 경로가 설정과 일치하는지 확인
- CI artifact 로그 확인

### 마이그레이션 실패

- Flyway 스크립트 순서 확인
- DB 연결 정보 확인
- validate/roundtrip 결과 같이 확인

### 보안 경계 실패

- SecurityConfig 확인
- Swagger 예외 경로 확인
- 관리자 경계 E2E와 보안 회귀 E2E 결과 같이 확인

## 릴리스 전 체크

최소 확인 항목:

1. `quality`
2. `platform-e2e`
3. `swagger-export`
4. `migration-validation`
5. `security-regression`
6. `release-gate`

선택 확장 항목:

1. `perf-extended`
2. `dependency-failure`
3. `live-smoke`
