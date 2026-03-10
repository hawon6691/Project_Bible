| 언어       | 프레임워크 (표준) | 빌드/패키지 도구 (Maven/Gradle 역할) | 직접 쿼리 방식 (Raw SQL / JDBC형) | 객체 매핑 방식 (ORM / JPA형) | 프로젝트 활용 팁                       |
| ---------- | ----------------- | ------------------------------------ | --------------------------------- | ---------------------------- | -------------------------------------- |
| Java       | Spring Boot       | Maven / Gradle                       | Spring JDBC (JdbcTemplate)        | Spring Data JPA (Hibernate)  | 표준 성능 비교의 기준점                |
| Kotlin     | Spring / Ktor     | Gradle (Kotlin DSL)                  | Exposed (DSL) / SQL시작           | Spring Data JPA / Korm       | Ktor + Exposed 조합은 초경량           |
| C++        | Oat++ / Drogon    | CMake + Conan                        | Oatpp-PostgreSQL (직접)           | Oatpp-ORM / sqlpp11          | 최소 메모리, 최대 속도 1위 후보        |
| C#         | ASP.NET Core      | dotnet CLI (NuGet)                   | Dapper (Micro-ORM)                | Entity Framework (EF) Core   | Dapper는 JDBC급 속도로 유명함          |
| Go         | Gin / Echo        | Go Modules (go mod)                  | database/sql / sqlx               | GORM / Ent                   | Go는 sqlx가 사실상 표준적 직접 쿼리    |
| Rust       | Actix-web / Axum  | Cargo                                | SQLx (Compile-time check)         | Diesel                       | SQLx는 직접 쿼리지만 안전함            |
| JavaScript | Express           | npm                                  | pg (node-postgres) / mysql2       | Sequelize / TypeORM          | 가장 대중적인 노드 환경 성능 체크      |
| TypeScript | NestJS            | npm                                  | Kysely / Knex.js                  | Prisma / TypeORM             | Prisma는 추상화 수준이 매우 높음       |
| Python     | Django / FastAPI  | pip                                  | psycopg2 / asyncpg                | Django ORM / SQLAlchemy      | 파이썬은 직접 쿼리 시 속도 향상폭이 큼 |
| PHP        | Laravel           | Composer                             | PDO (PHP Data Objects)            | Eloquent                     | Laravel 기본은 Eloquent나 PDO가 빠름   |
| Ruby       | Rails             | Bundler (Gemfile)                    | ActiveRecord::Base.connection     | ActiveRecord (ORM)           | 생산성 위주의 표준 모델                |
| Lua        | Lapis (OpenResty) | LuaRocks                             | pgmoon / resty.mysql              | Lapis DB Model               | Nginx 기반의 극강의 가벼움 증명        |
