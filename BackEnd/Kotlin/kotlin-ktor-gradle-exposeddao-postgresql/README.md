# PBShop Kotlin Ktor Gradle Exposed DAO PostgreSQL

공통 문서와 공통 DB 자산을 기준으로 진행하는 Kotlin Ktor Exposed DAO 구현체입니다.

## Current Scope

- Gradle Wrapper 기반 Kotlin/Ktor 부트스트랩
- 공통 JSON 응답 envelope
- 기본 라우트
  - `GET /health`
  - `GET /api/v1/health`
  - `GET /api/v1/docs-status`
- 서버 테스트 기반 최소 검증

## Run

```bash
./gradlew run
```

Windows에서는 아래를 사용합니다.

```powershell
.\gradlew.bat run
```

## Test

```bash
./gradlew test
```

## Next

- 공통 요구사항과 API 명세 기준으로 도메인 API 확장
- DB 연결과 공통 SQL 초기화 전략 추가
- OpenAPI/Swagger 연결
- Kotlin 전용 상태 문서 세트 확장
