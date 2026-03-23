# PBShop Kotlin Ktor Gradle Exposed DAO PostgreSQL

공통 문서와 공통 DB 자산을 기준으로 진행하는 Kotlin Ktor Exposed DAO 구현체입니다.
이 프로젝트는 Kotlin 전체의 1차 기준 구현체이며, 다른 Kotlin 트랙보다 먼저 완결 대상으로 유지합니다.

## Current Scope

- Gradle Wrapper 기반 Kotlin/Ktor 부트스트랩
- 공통 JSON 응답 envelope
- Request ID / 보안 헤더 / 인메모리 rate limit
- 공통 PostgreSQL bootstrap / seed / smoke
- 공통 명세 기준 다중 API slice 라우트 카탈로그
- OpenAPI JSON / Swagger HTML 문서 경로
- 운영/도메인 Kotlin 테스트 자산
- Kotlin 기준 트랙 메타데이터 노출

## Baseline Decisions

- 기준 slug: `kotlin-ktor-gradle-exposeddao-postgresql`
- 기준 스택: `Ktor + Gradle + Exposed DAO + PostgreSQL`
- 기본 포트: `8000`
- 기본 API prefix: `/api/v1`
- 문서 경로: `/docs`
- 공통 DB 기준: `Database/postgresql/*`, `Database/docker/docker-compose.yml`
- 후속 트랙 착수 조건: 이 프로젝트의 API, 테스트, CI, 문서 완료

## Run

```bash
./gradlew run
```

Windows에서는 아래를 사용합니다.

```powershell
.\gradlew.bat run
```

- 기본 주소: `http://127.0.0.1:8000`
- Health: `http://127.0.0.1:8000/health`
- Versioned Health: `http://127.0.0.1:8000/api/v1/health`
- Docs status: `http://127.0.0.1:8000/api/v1/docs-status`
- OpenAPI: `http://127.0.0.1:8000/docs/openapi`
- Swagger: `http://127.0.0.1:8000/docs/swagger`

## Test

```bash
./gradlew test
```

### Performance Assets

```bash
node test/performance/smoke.perf.js test-results/perf-smoke-summary.json
node test/performance/assert-summary.js test-results/perf-smoke-summary.json
```

### Script Assets

```bash
bash test/scripts/analyze-stability.sh
bash test/scripts/live-smoke.sh
```

## Docs

- OpenAPI JSON: `/docs/openapi`
- Swagger HTML: `/docs/swagger`
- Export artifacts: `./gradlew docsExport`

## Next

다음 순서를 고정 기준으로 사용합니다.

1. 기본 뼈대 정렬
2. DB 연결
3. 공통 요구사항과 API 명세 기준 전체 API 구현
4. 테스트 자산 확장
5. CI 구현
6. Kotlin 전용 문서 세트 작성
