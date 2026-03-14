# 04 Language

## 목적

이 문서는 언어별 기술 스택을 장황하게 설명하는 문서가 아니라, 실제로 몇 개의 비교용 구현 프로젝트를 만들어야 하는지 정하는 기준 문서다.

핵심 비교 축은 다음 네 가지다.

- 프레임워크
- 빌드/패키지 도구
- 데이터 접근 방식
- DB 엔진

데이터 접근 방식은 모든 언어에서 아래 두 축으로 나눈다.

- `Raw SQL`
- `ORM`

DB 엔진은 공통으로 아래 두 축을 기준으로 둔다.

- `PostgreSQL`
- `MySQL`

## 프로젝트 수 계산 규칙

| 조건 | 계산식 | 결과 |
| --- | --- | --- |
| 프레임워크 1개, 빌드/패키지 도구 1개 | `1 x 2 x 2` | `4개 프로젝트` |
| 프레임워크 2개, 빌드/패키지 도구 1개 | `2 x 2 x 2` | `8개 프로젝트` |
| 프레임워크 1개, 빌드/패키지 도구 2개 | `2 x 2 x 2` | `8개 프로젝트` |
| 프레임워크 2개, 빌드/패키지 도구 2개 | `2 x 2 x 2 x 2` | 최대 `16개 프로젝트` |

## 언어별 정리 기준

| 언어 | 프레임워크 | 빌드/패키지 도구 | PostgreSQL Raw SQL 평균 스택 | MySQL Raw SQL 평균 스택 | PostgreSQL ORM 평균 스택 | MySQL ORM 평균 스택 | 기본 프로젝트 수 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Java | `Spring Boot` | `Maven`, `Gradle` | `JDBC`, `jOOQ` | `JDBC`, `MyBatis` | `JPA(Hibernate)`, `Spring Data JPA` | `JPA(Hibernate)`, `Spring Data JPA` | `8` |
| Kotlin | `Spring`, `Ktor` | `Gradle` | `JDBC`, `Exposed SQL DSL` | `JDBC`, `Exposed SQL DSL` | `JPA(Hibernate)`, `Exposed DAO` | `JPA(Hibernate)`, `Exposed DAO` | `8` |
| C++ | `Oat++`, `Drogon` | `CMake`, `Conan` | `libpqxx` | `mysqlclient` | `Drogon ORM`, `ODB` | `Drogon ORM`, `ODB` | `16` |
| C# | `ASP.NET Core` | `dotnet CLI` | `ADO.NET`, `Dapper` | `ADO.NET`, `Dapper` | `Entity Framework Core` | `Entity Framework Core` | `4` |
| Go | `Gin`, `Echo` | `Go Modules` | `database/sql`, `sqlx` | `database/sql`, `sqlx` | `GORM`, `Ent` | `GORM`, `Ent` | `8` |
| Rust | `Actix-web`, `Axum` | `Cargo` | `SQLx` | `SQLx` | `Diesel`, `SeaORM` | `Diesel`, `SeaORM` | `8` |
| JavaScript | `Express` | `npm` | `pg`, `knex` | `mysql2`, `knex` | `Prisma`, `Sequelize` | `Prisma`, `Sequelize` | `4` |
| TypeScript | `NestJS` | `npm` | `pg`, `knex` | `mysql2`, `knex` | `TypeORM`, `Prisma` | `TypeORM`, `Prisma` | `4` |
| Python | `Django`, `FastAPI` | `pip`, `uv` | `psycopg`, `SQLAlchemy Core` | `mysqlclient`, `SQLAlchemy Core` | `Django ORM`, `SQLAlchemy ORM` | `Django ORM`, `SQLAlchemy ORM` | `8` |
| PHP | `Laravel` | `Composer` | `PDO`, `Query Builder` | `PDO`, `Query Builder` | `Eloquent ORM` | `Eloquent ORM` | `4` |
| Ruby | `Rails` | `Bundler` | `pg`, `Sequel` | `mysql2`, `Sequel` | `ActiveRecord` | `ActiveRecord` | `4` |
| Lua | `Lapis(OpenResty)` | `LuaRocks` | `pgmoon` | `luasql` | `Lapis Model` | `Lapis Model` | `4` |

## 언어별 생성 프로젝트 기준

| 언어 | 생성 프로젝트 |
| --- | --- |
| Java | `java-spring-maven-jdbc-postgresql`, `java-spring-maven-jpa-postgresql`, `java-spring-maven-jdbc-mysql`, `java-spring-maven-jpa-mysql`, `java-spring-gradle-jdbc-postgresql`, `java-spring-gradle-jpa-postgresql`, `java-spring-gradle-jdbc-mysql`, `java-spring-gradle-jpa-mysql` |
| Kotlin | `kotlin-spring-jdbc-postgresql`, `kotlin-spring-jpa-postgresql`, `kotlin-spring-jdbc-mysql`, `kotlin-spring-jpa-mysql`, `kotlin-ktor-exposedsql-postgresql`, `kotlin-ktor-exposeddao-postgresql`, `kotlin-ktor-exposedsql-mysql`, `kotlin-ktor-exposeddao-mysql` |
| C++ | `cpp-oatpp-libpqxx-postgresql`, `cpp-oatpp-odb-postgresql`, `cpp-oatpp-mysqlclient-mysql`, `cpp-oatpp-odb-mysql`, `cpp-drogon-libpqxx-postgresql`, `cpp-drogon-orm-postgresql`, `cpp-drogon-mysqlclient-mysql`, `cpp-drogon-orm-mysql`, `cpp-oatpp-libpqxx-postgresql-conan`, `cpp-oatpp-odb-postgresql-conan`, `cpp-oatpp-mysqlclient-mysql-conan`, `cpp-oatpp-odb-mysql-conan`, `cpp-drogon-libpqxx-postgresql-conan`, `cpp-drogon-orm-postgresql-conan`, `cpp-drogon-mysqlclient-mysql-conan`, `cpp-drogon-orm-mysql-conan` |
| C# | `csharp-aspnet-dapper-postgresql`, `csharp-aspnet-efcore-postgresql`, `csharp-aspnet-dapper-mysql`, `csharp-aspnet-efcore-mysql` |
| Go | `go-gin-sqlx-postgresql`, `go-gin-gorm-postgresql`, `go-gin-sqlx-mysql`, `go-gin-gorm-mysql`, `go-echo-sqlx-postgresql`, `go-echo-gorm-postgresql`, `go-echo-sqlx-mysql`, `go-echo-gorm-mysql` |
| Rust | `rust-actix-sqlx-postgresql`, `rust-actix-diesel-postgresql`, `rust-actix-sqlx-mysql`, `rust-actix-seaorm-mysql`, `rust-axum-sqlx-postgresql`, `rust-axum-diesel-postgresql`, `rust-axum-sqlx-mysql`, `rust-axum-seaorm-mysql` |
| JavaScript | `javascript-express-knex-postgresql`, `javascript-express-prisma-postgresql`, `javascript-express-knex-mysql`, `javascript-express-prisma-mysql` |
| TypeScript | `typescript-nest-knex-postgresql`, `typescript-nest-typeorm-postgresql`, `typescript-nest-knex-mysql`, `typescript-nest-typeorm-mysql` |
| Python | `python-django-sqlalchemycore-postgresql`, `python-django-djangoorm-postgresql`, `python-django-sqlalchemycore-mysql`, `python-django-djangoorm-mysql`, `python-fastapi-sqlalchemycore-postgresql`, `python-fastapi-sqlalchemyorm-postgresql`, `python-fastapi-sqlalchemycore-mysql`, `python-fastapi-sqlalchemyorm-mysql` |
| PHP | `php-laravel-pdo-postgresql`, `php-laravel-eloquentorm-postgresql`, `php-laravel-pdo-mysql`, `php-laravel-eloquentorm-mysql` |
| Ruby | `ruby-rails-sequel-postgresql`, `ruby-rails-activerecord-postgresql`, `ruby-rails-sequel-mysql`, `ruby-rails-activerecord-mysql` |
| Lua | `lua-lapis-pgmoon-postgresql`, `lua-lapis-model-postgresql`, `lua-lapis-luasql-mysql`, `lua-lapis-model-mysql` |

## 작성 원칙

| 항목 | 원칙 |
| --- | --- |
| 데이터 접근 | 반드시 `Raw SQL`과 `ORM`을 분리한다 |
| DB 엔진 | `PostgreSQL`, `MySQL`은 별도 프로젝트로 분리한다 |
| 프레임워크 | 2개면 각각 별도 프로젝트로 분리한다 |
| 빌드/패키지 도구 | 비교 축이면 별도 프로젝트로 분리한다 |
| 스택 선택 | 실무 평균 스택을 우선 기준으로 삼는다 |

## 결론

이 문서의 핵심은 언어 선택이 아니라, 언어별 구현체를 프레임워크, 빌드 도구, 데이터 접근 방식, DB 엔진 기준으로 몇 개의 비교 가능한 프로젝트로 나눌지 정하는 것이다.
