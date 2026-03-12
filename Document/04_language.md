# 04 Language

## 목적

이 문서는 언어별 기술 스택 자체를 나열하는 문서가 아니라, 실제로 몇 개의 구현 프로젝트를 만들어야 하는지 결정하는 기준 문서다.

이번 프로젝트의 핵심 비교 축은 아래 두 가지다.

- 데이터 접근 방식
  - `Raw SQL 방식`
  - `ORM 방식`
- 프레임워크 / 빌드 패키지 도구 조합

즉, 단순히 "Java는 Spring Boot를 쓴다" 수준이 아니라,
"한 언어에서 프레임워크가 몇 개냐", "빌드/패키지 도구가 몇 개냐", "그 위에 Raw SQL / ORM을 어떻게 나누느냐"를 기준으로 실제 프로젝트 수를 계산해야 한다.

## 기본 원칙

### 1. 데이터 접근 방식은 항상 2개로 나눈다

각 언어는 최소한 아래 2개의 구현 축을 가진다.

- `Raw SQL 방식`
- `ORM 방식`

이 원칙 때문에 프레임워크가 1개뿐인 언어라도 기본적으로 프로젝트는 2개가 된다.

예시:
- `NestJS + Raw SQL`
- `NestJS + ORM`

## 2. 프레임워크가 2개면 프로젝트 수는 4개가 된다

언어 하나에 프레임워크가 2개 있으면, 각 프레임워크마다 다시 `Raw SQL` / `ORM`으로 나뉘어야 한다.

계산식:
- `프레임워크 2개 x 데이터 접근 2개 = 4개 프로젝트`

예시:
- `Spring + Raw SQL`
- `Spring + ORM`
- `Ktor + Raw SQL`
- `Ktor + ORM`

## 3. 빌드/패키지 도구가 2개면 그것도 별도 프로젝트로 나눈다

특히 Java처럼 빌드 도구 자체가 비교 대상이면, 같은 프레임워크라도 빌드 도구별 프로젝트를 따로 둔다.

계산식:
- `빌드/패키지 도구 2개 x 데이터 접근 2개 = 4개 프로젝트`

예시:
- `Spring Boot + Maven + Raw SQL`
- `Spring Boot + Maven + ORM`
- `Spring Boot + Gradle + Raw SQL`
- `Spring Boot + Gradle + ORM`

즉, Java는 원래 `JPA 버전만` 두면 의미가 약하다.
왜냐하면 비교 축을 두겠다고 해놓고 실제로는 같은 ORM 계열만 남게 되기 때문이다.
그래서 Java도 반드시 `Raw SQL`과 `ORM`을 둘 다 나누는 것이 맞다.

## 프로젝트 수 계산 규칙

### 규칙 A. 프레임워크가 1개, 빌드/패키지 도구가 1개

- 결과: `2개 프로젝트`
- 계산:
  - `1 x 2 = 2`

예시:
- `Laravel + Raw SQL`
- `Laravel + ORM`

### 규칙 B. 프레임워크가 2개, 빌드/패키지 도구가 1개

- 결과: `4개 프로젝트`
- 계산:
  - `2 x 2 = 4`

예시:
- `Spring + Raw SQL`
- `Spring + ORM`
- `Ktor + Raw SQL`
- `Ktor + ORM`

### 규칙 C. 프레임워크가 1개, 빌드/패키지 도구가 2개

- 결과: `4개 프로젝트`
- 계산:
  - `2 x 2 = 4`

예시:
- `Spring Boot + Maven + Raw SQL`
- `Spring Boot + Maven + ORM`
- `Spring Boot + Gradle + Raw SQL`
- `Spring Boot + Gradle + ORM`

### 규칙 D. 프레임워크가 2개, 빌드/패키지 도구도 2개

- 결과: 경우에 따라 `8개 프로젝트`까지 늘어날 수 있다.
- 계산:
  - `프레임워크 2 x 빌드도구 2 x 데이터접근 2 = 8`

현재는 여기까지 확장하지 않고,
실제 비교 필요성이 있는 언어에서만 적용한다.

## 현재 정리 기준

| 언어 | 프레임워크 수 | 빌드/패키지 도구 수 | 데이터 접근 방식 수 | 기본 프로젝트 수 | 비고 |
| --- | --- | --- | --- | --- | --- |
| Java | 1 | 2 | 2 | 4 | `Maven`, `Gradle`을 분리 비교 |
| Kotlin | 2 | 1 | 2 | 4 | `Spring`, `Ktor` 분리 |
| C++ | 2 | 1 | 2 | 4 | `Oat++`, `Drogon` 분리 |
| C# | 1 | 1 | 2 | 2 | `ASP.NET Core` 기준 |
| Go | 2 | 1 | 2 | 4 | `Gin`, `Echo` 분리 |
| Rust | 2 | 1 | 2 | 4 | `Actix-web`, `Axum` 분리 |
| JavaScript | 1 | 1 | 2 | 2 | `Express` 기준 |
| TypeScript | 1 | 1 | 2 | 2 | `NestJS` 기준 |
| Python | 2 | 1 | 2 | 4 | `Django`, `FastAPI` 분리 |
| PHP | 1 | 1 | 2 | 2 | `Laravel` 기준 |
| Ruby | 1 | 1 | 2 | 2 | `Rails` 기준 |
| Lua | 1 | 1 | 2 | 2 | `Lapis (OpenResty)` 기준 |

## 언어별 권장 비교 축

### Java

- 프레임워크
  - `Spring Boot`
- 빌드/패키지 도구
  - `Maven`
  - `Gradle`
- 데이터 접근
  - `Raw SQL / JDBC`
  - `ORM / JPA`

생성 프로젝트:
- `java-spring-maven-rawsql`
- `java-spring-maven-orm`
- `java-spring-gradle-rawsql`
- `java-spring-gradle-orm`

### Kotlin

- 프레임워크
  - `Spring`
  - `Ktor`
- 빌드/패키지 도구
  - `Gradle`
- 데이터 접근
  - `Raw SQL`
  - `ORM`

생성 프로젝트:
- `kotlin-spring-rawsql`
- `kotlin-spring-orm`
- `kotlin-ktor-rawsql`
- `kotlin-ktor-orm`

### C++

- 프레임워크
  - `Oat++`
  - `Drogon`
- 빌드/패키지 도구
  - `CMake + Conan`
- 데이터 접근
  - `Raw SQL`
  - `ORM`

생성 프로젝트:
- `cpp-oatpp-rawsql`
- `cpp-oatpp-orm`
- `cpp-drogon-rawsql`
- `cpp-drogon-orm`

### C#

- 프레임워크
  - `ASP.NET Core`
- 빌드/패키지 도구
  - `dotnet CLI`
- 데이터 접근
  - `Raw SQL / Dapper`
  - `ORM / Entity Framework Core`

생성 프로젝트:
- `csharp-aspnet-rawsql`
- `csharp-aspnet-orm`

### Go

- 프레임워크
  - `Gin`
  - `Echo`
- 빌드/패키지 도구
  - `Go Modules`
- 데이터 접근
  - `Raw SQL / sqlx`
  - `ORM / GORM`

생성 프로젝트:
- `go-gin-rawsql`
- `go-gin-orm`
- `go-echo-rawsql`
- `go-echo-orm`

### Rust

- 프레임워크
  - `Actix-web`
  - `Axum`
- 빌드/패키지 도구
  - `Cargo`
- 데이터 접근
  - `Raw SQL / SQLx`
  - `ORM / Diesel`

생성 프로젝트:
- `rust-actix-rawsql`
- `rust-actix-orm`
- `rust-axum-rawsql`
- `rust-axum-orm`

### TypeScript

- 프레임워크
  - `NestJS`
- 빌드/패키지 도구
  - `npm`
- 데이터 접근
  - `Raw SQL`
  - `ORM`

생성 프로젝트:
- `typescript-nest-rawsql`
- `typescript-nest-orm`

### PHP

- 프레임워크
  - `Laravel`
- 빌드/패키지 도구
  - `Composer`
- 데이터 접근
  - `Raw SQL / PDO`
  - `ORM / Eloquent`

생성 프로젝트:
- `php-laravel-rawsql`
- `php-laravel-orm`

### Ruby

- 프레임워크
  - `Rails`
- 빌드/패키지 도구
  - `Bundler`
- 데이터 접근
  - `Raw SQL`
  - `ORM / ActiveRecord`

생성 프로젝트:
- `ruby-rails-rawsql`
- `ruby-rails-orm`

### Lua

- 프레임워크
  - `Lapis (OpenResty)`
- 빌드/패키지 도구
  - `LuaRocks`
- 데이터 접근
  - `Raw SQL`
  - `ORM / Lapis Model`

생성 프로젝트:
- `lua-lapis-rawsql`
- `lua-lapis-orm`

## 작성 원칙

- 언어별 구현 문서와 이슈는 반드시 `Raw SQL`과 `ORM`을 구분해서 작성한다.
- 프레임워크가 2개면 프레임워크별로 문서와 프로젝트를 분리한다.
- 빌드/패키지 도구가 2개면 도구별로 문서와 프로젝트를 분리한다.
- 비교 의미가 없는 중복 조합은 만들지 않되, 비교 축으로 선언한 항목은 반드시 프로젝트로 분리한다.

## 결론

이 문서의 핵심은 "어떤 언어를 쓸까"가 아니라 "그 언어를 몇 개의 비교 가능한 프로젝트로 쪼갤까"다.

정리하면:
- 프레임워크 1개면 기본 `2개 프로젝트`
- 프레임워크 2개면 기본 `4개 프로젝트`
- 빌드/패키지 도구가 2개면 그것도 `4개 프로젝트`
- 필요 시 `프레임워크 x 빌드도구 x Raw/ORM` 조합으로 더 확장할 수 있다.
