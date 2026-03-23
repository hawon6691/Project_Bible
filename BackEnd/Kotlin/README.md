# PBShop Kotlin Backend

## 1. 기준 구현체

Kotlin 1차 개발 기준은 아래 단일 트랙으로 고정한다.

- 기준 트랙: `BackEnd/Kotlin/kotlin-ktor-gradle-exposeddao-postgresql`
- 기준 스택: `Ktor + Gradle + Exposed DAO + PostgreSQL`
- 기준 slug: `kotlin-ktor-gradle-exposeddao-postgresql`

이 트랙이 API, 테스트, CI, 문서의 단일 기준 소스다.
Kotlin의 다른 예정 트랙(`Spring/JPA/JDBC`, `MySQL`, `Exposed SQL`)은 이 기준 구현체 완료 전까지 착수하지 않는다.

## 2. 공통 기준

Kotlin 트랙은 아래 공통 자산을 기준으로 맞춘다.

- 요구사항: `Document/01_requirements.md`
- API 명세: `Document/02_api-specification.md`
- ERD: `Document/03_erd.md`
- PostgreSQL 스키마: `Database/postgresql/postgres_table.sql`
- PostgreSQL 샘플 데이터: `Database/postgresql/sample_data.sql`
- PostgreSQL 수동 설정 참고: `Database/postgresql/setting.sql`
- 공용 Docker 인프라: `Database/docker/docker-compose.yml`

## 3. 운영 기본값

- 기본 포트: `8000`
- 기본 API prefix: `/api/v1`
- Health: `/health`, `/api/v1/health`
- 문서 경로: `/docs` 예정

## 4. 개발 순서

Kotlin 1차 구현은 아래 순서로 진행한다.

1. 기본 뼈대 정렬
2. DB 연결
3. 전체 API 단계 구현
4. 테스트 자산
5. CI
6. Kotlin 문서 세트
