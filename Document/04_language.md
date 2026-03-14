# 04 Language

## 목적

이 문서는 언어별 기술 스택을 장황하게 설명하는 문서가 아니라, 실제로 몇 개의 비교용 구현 프로젝트를 만들어야 하는지 정하는 기준 문서다.

핵심 비교 축은 다음 세 가지다.

- 프레임워크
- 빌드/패키지 도구
- 데이터 접근 방식

데이터 접근 방식은 모든 언어에서 아래 두 축으로 나눈다.

- `Raw SQL`
- `ORM`

## 프로젝트 수 계산 규칙

| 조건 | 계산식 | 결과 |
| --- | --- | --- |
| 프레임워크 1개, 빌드/패키지 도구 1개 | `1 x 2` | `2개 프로젝트` |
| 프레임워크 2개, 빌드/패키지 도구 1개 | `2 x 2` | `4개 프로젝트` |
| 프레임워크 1개, 빌드/패키지 도구 2개 | `2 x 2` | `4개 프로젝트` |
| 프레임워크 2개, 빌드/패키지 도구 2개 | `2 x 2 x 2` | 최대 `8개 프로젝트` |

## 언어별 정리 기준

| 언어 | 프레임워크 | 빌드/패키지 도구 | Raw SQL 평균 스택 | ORM 평균 스택 | 기본 프로젝트 수 |
| --- | --- | --- | --- | --- | --- |
| Java | `Spring Boot` | `Maven`, `Gradle` | `JDBC`, `jOOQ`, `MyBatis` | `JPA(Hibernate)`, `Spring Data JPA` | `4` |
| Kotlin | `Spring`, `Ktor` | `Gradle` | `JDBC`, `Exposed SQL DSL` | `JPA(Hibernate)`, `Exposed DAO` | `4` |
| C++ | `Oat++`, `Drogon` | `CMake`, `Conan` | `libpqxx`, `mysqlclient`, `sqlite3` | `Drogon ORM`, `ODB` | `4` |
| C# | `ASP.NET Core` | `dotnet CLI` | `ADO.NET`, `Dapper` | `Entity Framework Core` | `2` |
| Go | `Gin`, `Echo` | `Go Modules` | `database/sql`, `sqlx` | `GORM`, `Ent` | `4` |
| Rust | `Actix-web`, `Axum` | `Cargo` | `SQLx` | `Diesel`, `SeaORM` | `4` |
| JavaScript | `Express` | `npm` | `pg`, `mysql2`, `knex` | `Sequelize`, `Prisma` | `2` |
| TypeScript | `NestJS` | `npm` | `pg`, `mysql2`, `knex` | `TypeORM`, `Prisma` | `2` |
| Python | `Django`, `FastAPI` | `pip`, `uv` | `psycopg`, `mysqlclient`, `SQLAlchemy Core` | `Django ORM`, `SQLAlchemy ORM` | `4` |
| PHP | `Laravel` | `Composer` | `PDO`, `Query Builder` | `Eloquent ORM` | `2` |
| Ruby | `Rails` | `Bundler` | `pg`, `mysql2`, `Sequel` | `ActiveRecord` | `2` |
| Lua | `Lapis(OpenResty)` | `LuaRocks` | `luasql`, `pgmoon` | `Lapis Model` | `2` |

## 언어별 생성 프로젝트 기준

| 언어 | 생성 프로젝트 |
| --- | --- |
| Java | `java-spring-maven-jdbc`, `java-spring-maven-jpa`, `java-spring-gradle-jdbc`, `java-spring-gradle-jpa` |
| Kotlin | `kotlin-spring-jdbc`, `kotlin-spring-jpa`, `kotlin-ktor-exposedsql`, `kotlin-ktor-exposeddao` |
| C++ | `cpp-oatpp-libpqxx`, `cpp-oatpp-odb`, `cpp-drogon-sqlmapper`, `cpp-drogon-orm` |
| C# | `csharp-aspnet-dapper`, `csharp-aspnet-efcore` |
| Go | `go-gin-sqlx`, `go-gin-gorm`, `go-echo-sqlx`, `go-echo-gorm` |
| Rust | `rust-actix-sqlx`, `rust-actix-diesel`, `rust-axum-sqlx`, `rust-axum-seaorm` |
| JavaScript | `javascript-express-knex`, `javascript-express-prisma` |
| TypeScript | `typescript-nest-knex`, `typescript-nest-typeorm` |
| Python | `python-django-sqlalchemycore`, `python-django-djangoorm`, `python-fastapi-sqlalchemycore`, `python-fastapi-sqlalchemyorm` |
| PHP | `php-laravel-pdo`, `php-laravel-eloquentorm` |
| Ruby | `ruby-rails-sequel`, `ruby-rails-activerecord` |
| Lua | `lua-lapis-pgmoon`, `lua-lapis-model` |

## 작성 원칙

| 항목 | 원칙 |
| --- | --- |
| 데이터 접근 | 반드시 `Raw SQL`과 `ORM`을 분리한다 |
| 프레임워크 | 2개면 각각 별도 프로젝트로 분리한다 |
| 빌드/패키지 도구 | 비교 축이면 별도 프로젝트로 분리한다 |
| 스택 선택 | 실무 평균 스택을 우선 기준으로 삼는다 |

## 결론

이 문서의 핵심은 언어 선택이 아니라, 언어별 구현체를 몇 개의 비교 가능한 프로젝트로 나눌지 정하는 것이다.
