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

## 폴더명 규칙

실제 저장소 폴더명과 CLI 인자는 아래 규칙으로 통일한다.

`<language>-<framework>-<build>-<dataaccess>-<db>`

작성 규칙은 다음과 같다.

- 전부 소문자를 사용한다.
- 구분자는 `-`만 사용한다.
- 각 슬롯 내부에서는 추가 구분자를 넣지 않는다.
- `dataaccess`에는 `rawsql`, `orm` 같은 큰 분류가 아니라 실제 기술명을 넣는다.
- `db`는 `postgresql`, `mysql`처럼 실제 엔진명을 그대로 넣는다.

예시

- `java-spring-maven-jpa-postgresql`
- `javascript-express-npm-prisma-postgresql`
- `typescript-nest-npm-typeorm-postgresql`
- `php-laravel-composer-eloquent-postgresql`
- `kotlin-ktor-gradle-exposeddao-postgresql`

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
| Kotlin | `kotlin-spring-gradle-jdbc-postgresql`, `kotlin-spring-gradle-jpa-postgresql`, `kotlin-spring-gradle-jdbc-mysql`, `kotlin-spring-gradle-jpa-mysql`, `kotlin-ktor-gradle-exposedsql-postgresql`, `kotlin-ktor-gradle-exposeddao-postgresql`, `kotlin-ktor-gradle-exposedsql-mysql`, `kotlin-ktor-gradle-exposeddao-mysql` |
| C++ | `cpp-oatpp-cmake-libpqxx-postgresql`, `cpp-oatpp-cmake-odb-postgresql`, `cpp-oatpp-cmake-mysqlclient-mysql`, `cpp-oatpp-cmake-odb-mysql`, `cpp-drogon-cmake-libpqxx-postgresql`, `cpp-drogon-cmake-orm-postgresql`, `cpp-drogon-cmake-mysqlclient-mysql`, `cpp-drogon-cmake-orm-mysql`, `cpp-oatpp-conan-libpqxx-postgresql`, `cpp-oatpp-conan-odb-postgresql`, `cpp-oatpp-conan-mysqlclient-mysql`, `cpp-oatpp-conan-odb-mysql`, `cpp-drogon-conan-libpqxx-postgresql`, `cpp-drogon-conan-orm-postgresql`, `cpp-drogon-conan-mysqlclient-mysql`, `cpp-drogon-conan-orm-mysql` |
| C# | `csharp-aspnetcore-dotnetcli-dapper-postgresql`, `csharp-aspnetcore-dotnetcli-efcore-postgresql`, `csharp-aspnetcore-dotnetcli-dapper-mysql`, `csharp-aspnetcore-dotnetcli-efcore-mysql` |
| Go | `go-gin-gomodules-sqlx-postgresql`, `go-gin-gomodules-gorm-postgresql`, `go-gin-gomodules-sqlx-mysql`, `go-gin-gomodules-gorm-mysql`, `go-echo-gomodules-sqlx-postgresql`, `go-echo-gomodules-gorm-postgresql`, `go-echo-gomodules-sqlx-mysql`, `go-echo-gomodules-gorm-mysql` |
| Rust | `rust-actixweb-cargo-sqlx-postgresql`, `rust-actixweb-cargo-diesel-postgresql`, `rust-actixweb-cargo-sqlx-mysql`, `rust-actixweb-cargo-seaorm-mysql`, `rust-axum-cargo-sqlx-postgresql`, `rust-axum-cargo-diesel-postgresql`, `rust-axum-cargo-sqlx-mysql`, `rust-axum-cargo-seaorm-mysql` |
| JavaScript | `javascript-express-npm-knex-postgresql`, `javascript-express-npm-prisma-postgresql`, `javascript-express-npm-knex-mysql`, `javascript-express-npm-prisma-mysql` |
| TypeScript | `typescript-nest-npm-knex-postgresql`, `typescript-nest-npm-typeorm-postgresql`, `typescript-nest-npm-knex-mysql`, `typescript-nest-npm-typeorm-mysql` |
| Python | `python-django-pip-sqlalchemycore-postgresql`, `python-django-pip-djangoorm-postgresql`, `python-django-pip-sqlalchemycore-mysql`, `python-django-pip-djangoorm-mysql`, `python-fastapi-uv-sqlalchemycore-postgresql`, `python-fastapi-uv-sqlalchemyorm-postgresql`, `python-fastapi-uv-sqlalchemycore-mysql`, `python-fastapi-uv-sqlalchemyorm-mysql` |
| PHP | `php-laravel-composer-pdo-postgresql`, `php-laravel-composer-eloquent-postgresql`, `php-laravel-composer-pdo-mysql`, `php-laravel-composer-eloquent-mysql` |
| Ruby | `ruby-rails-bundler-sequel-postgresql`, `ruby-rails-bundler-activerecord-postgresql`, `ruby-rails-bundler-sequel-mysql`, `ruby-rails-bundler-activerecord-mysql` |
| Lua | `lua-lapis-luarocks-pgmoon-postgresql`, `lua-lapis-luarocks-model-postgresql`, `lua-lapis-luarocks-luasql-mysql`, `lua-lapis-luarocks-model-mysql` |

## 작성 원칙

| 항목 | 원칙 |
| --- | --- |
| 데이터 접근 | 반드시 `Raw SQL`과 `ORM`을 분리한다 |
| DB 엔진 | `PostgreSQL`, `MySQL`은 별도 프로젝트로 분리한다 |
| 프레임워크 | 2개면 각각 별도 프로젝트로 분리한다 |
| 빌드/패키지 도구 | 비교 축이면 별도 프로젝트로 분리한다 |
| 폴더명 | `<language>-<framework>-<build>-<dataaccess>-<db>` 규칙을 따른다 |
| 스택 선택 | 실무 평균 스택을 우선 기준으로 삼는다 |

## 결론

이 문서의 핵심은 언어 선택이 아니라, 언어별 구현체를 프레임워크, 빌드 도구, 데이터 접근 방식, DB 엔진 기준으로 몇 개의 비교 가능한 프로젝트로 나눌지 정하는 것이다.
