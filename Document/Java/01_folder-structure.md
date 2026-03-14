# Java Folder Structure

## 대상

- 트랙: Java
- 기준 구현체: `BackEnd/Java/springshop_maven`

## 루트 구조

```text
BackEnd/Java/
├─ README.md
├─ springshop_maven/
│  ├─ pom.xml
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/com/pbshop/springshop/
│  │  │  │  ├─ config/
│  │  │  │  ├─ auth/
│  │  │  │  ├─ user/
│  │  │  │  ├─ category/
│  │  │  │  ├─ product/
│  │  │  │  ├─ order/
│  │  │  │  ├─ payment/
│  │  │  │  ├─ ops/
│  │  │  │  └─ ...
│  │  │  └─ resources/
│  │  │     ├─ application.properties
│  │  │     ├─ application-ci.properties
│  │  │     └─ db/migration/
│  │  └─ test/
│  │     ├─ java/com/pbshop/springshop/
│  │     │  ├─ e2e/
│  │     │  ├─ scripts/
│  │     │  ├─ support/
│  │     │  └─ ...
│  │     └─ resources/
│  │        └─ application-test.properties
│  └─ test/
│     ├─ performance/
│     └─ scripts/
└─ springshop_gradle/
```

## 디렉터리 역할

### `src/main/java/com/pbshop/springshop`

애플리케이션 본 코드 위치다.

- `config/`: 보안, Swagger, 공통 설정
- `auth/`: 인증 및 토큰 관련 기능
- `user/`, `category/`, `product/`, `order/` 등: 도메인별 API 및 서비스
- `ops/`: 운영, 대시보드, 관측성, 큐 관리 등 운영 계층

### `src/main/resources`

런타임 설정과 마이그레이션 자산 위치다.

- `application.properties`: 기본 설정
- `application-ci.properties`: CI 실행용 설정
- `db/migration/`: Flyway 마이그레이션 스크립트

### `src/test/java/com/pbshop/springshop`

JUnit 기반 테스트 코드 위치다.

- `e2e/`: 공개 API, 관리자 API, 운영 API E2E
- `scripts/`: 스크립트 성격 검증 테스트
- `support/`: 인증, 공통 요청, 테스트 데이터 보조 코드

### `src/test/resources`

테스트 전용 설정 파일 위치다.

- `application-test.properties`

### `test/performance`

성능 자산 위치다.

- smoke
- soak
- spike-search
- price-compare
- search-ranking

### `test/scripts`

운영/검증용 보조 스크립트 위치다.

- `analyze-stability.sh`
- `live-smoke.sh`
- `migration-roundtrip.sh`
- `validate-migrations.sh`

## 문서 구조

`Document/Java`는 다음 순서로 유지한다.

1. `01_folder-structure.md`
2. `02_runbook.md`
3. `03_implementation-status.md`
4. `04_operations-runbook.md`
5. `05_completion-report.md`
6. `06_requirements-api-gap-analysis.md`

## 운영 기준

- Maven 트랙을 Java 완료 기준으로 본다.
- Gradle 트랙은 참조용으로만 유지하며 완료 범위에 포함하지 않는다.
- 테스트/CI/문서 기준은 모두 `springshop_maven` 기준으로 관리한다.
