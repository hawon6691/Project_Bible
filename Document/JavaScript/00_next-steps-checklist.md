# JavaScript Next Steps Checklist

`BackEnd/JavaScript/expressshop_prismaorm` 구현은 공통 문서 기준 핵심 완료선을 정리했고, 현재는 선택적 고도화 단계에 들어갔다.

기준 문서
- `Document/01_requirements.md`
- `Document/02_api-specification.md`
- `Document/03_erd.md`
- `Document/04_language.md`
- `Document/05_test-specification.md`
- `Document/06_ci-specification.md`

공통 DB 자산
- `Database/docker/docker-compose.yml`
- `Database/postgresql/postgres_table.sql`
- `Database/postgresql/sample_data.sql`
- `Database/postgresql/setting.sql`
- `Database/mysql/mysql_table.sql`
- `Database/mysql/mysql_sample_data.sql`
- `Database/marie/marie_table.sql`
- `Database/marie/marie_sample_data.sql`

DB 원칙
- JavaScript 구현은 자체 임의 스키마를 기준으로 만들지 않는다.
- 공통 `Database` 폴더의 SQL과 Docker 자산을 기준으로 맞춘다.
- ORM 모델은 공통 SQL 스키마에 맞게 역으로 정렬한다.
- 로컬 실행과 테스트 환경은 공통 `docker-compose.yml`을 우선 사용한다.

## 현재 상태 요약

- 완료된 항목
  - 프로젝트 식별자/구조 정리
  - 공통 API 구조 및 OpenAPI/Swagger
  - 핵심 도메인/운영 API
  - Error Code Catalog
  - Query API
  - Chat REST gap
  - Chat Socket.IO
  - 플랫폼 E2E / 도메인 API 테스트
  - 성능/스크립트 자산 기본선
  - 자동/수동 CI 게이트
  - JavaScript 전용 상태 문서 세트

- 현재 남은 선택 작업
  - JavaScript 보조 문서 확장
  - 확장 성능 자산(`soak`, `spike-search`, `price-compare`) 추가 검토
  - 수동 GitHub Actions 게이트 실실행 이력 축적

## 추천 다음 단계

1. 수동 GitHub Actions 게이트를 실제로 한 번씩 실행해 artifact를 축적한다.
2. 필요하면 확장 성능 자산 `soak`, `spike-search`, `price-compare`를 추가한다.
3. 운영 인수인계가 필요하면 JavaScript 보조 문서를 추가 작성한다.
