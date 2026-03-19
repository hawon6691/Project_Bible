# 07 Code Convention

## 1. 목적

본 문서는 PBShop 멀티 스택 프로젝트에서 공통으로 사용하는 네이밍 규칙을 정의한다.

이 문서의 핵심 대상은 다음과 같다.

- 이슈 문서 이름
- 이슈 메타 정보
- 브랜치 이름
- 커밋 메시지
- 공통 문서 파일명
- 언어/프레임워크/구현체 식별 slug

앞으로 Java, JavaScript, Python, PHP, Kotlin, TypeScript, C++, C#, Go, Rust, Ruby, Lua 전부 같은 규칙으로 확장하는 것을 전제로 한다.

## 2. 기본 원칙

### 2.1 짧고 일관되게 작성한다

- 이름은 가능한 한 짧게 유지한다.
- 같은 의미를 여러 표현으로 섞지 않는다.
- 한 번 정한 slug는 같은 언어 트랙에서 계속 재사용한다.

예:

- `javascript-express-prisma`
- `typescript-nest-typeorm`
- `java-maven-jpa`

### 2.2 명사형을 우선한다

- 이슈 제목은 명사형을 우선한다.
- 브랜치 slug도 기능 설명형 문장보다 명사형 조합을 우선한다.
- “무엇을 했다”보다 “무엇에 대한 작업인지”가 드러나야 한다.

좋은 예:

- `Push API`
- `Crawler API`
- `Security Regression E2E`
- `Swagger OpenAPI`

지양 예:

- `Add push api endpoints`
- `Fix and improve crawler`

### 2.3 작업 성격에 맞는 타입을 사용한다

공통 타입은 아래 6개를 기준으로 한다.

- `feat`: 사용자 기능/API 추가
- `bug`: 버그 수정
- `test`: 테스트 코드/검증 자산 추가
- `chore`: CI, 빌드, 환경, 운영, 유지보수 작업
- `docs`: 문서 중심 작업
- `refactor`: 동작 변화 없이 구조 정리

## 3. 언어 식별 규칙

### 3.1 언어 표기

문서, 이슈, 브랜치, 커밋에서 사용하는 언어명은 아래 표기를 기준으로 한다.

| 표시명 | slug |
| --- | --- |
| Java | `java` |
| JavaScript | `javascript` |
| Python | `python` |
| PHP | `php` |
| Kotlin | `kotlin` |
| TypeScript | `typescript` |
| C++ | `cpp` |
| C# | `csharp` |
| Go | `go` |
| Rust | `rust` |
| Ruby | `ruby` |
| Lua | `lua` |

### 3.2 구현체 slug 규칙

언어명만으로 부족하면 아래 순서로 연결한다.

`language-framework-ormorstack`

예:

- `javascript-express-prisma`
- `typescript-nest-typeorm`
- `java-maven-jpa`
- `php-laravel-eloquentorm`

프레임워크/빌드/ORM을 모두 넣지 않아도 되지만, 저장소 안에서 어떤 구현체인지 구분 가능한 수준까지는 포함해야 한다.

## 4. 이슈 문서 규칙

### 4.1 문서 위치

이슈 문서는 작업 성격에 맞는 폴더에 둔다.

- `Document/.issue/feat`
- `Document/.issue/bug`
- `Document/.issue/test`
- `Document/.issue/chore`
- `Document/.issue/docs`
- `Document/.issue/refactor`

### 4.2 파일명 규칙

파일명 형식은 아래를 따른다.

`type-번호-language-implementation-topic.md`

예:

- `feat-489-javascript-express-prisma-image-core-api.md`
- `test-501-javascript-express-prisma-security-regression-e2e.md`
- `chore-497-javascript-express-prisma-swagger-openapi.md`

규칙:

- 모두 소문자
- 공백 대신 `-`
- 불필요한 조사/동사 제거
- topic은 가능한 한 명사형

### 4.3 제목 규칙

이슈 제목 형식:

`[TYPE] Language Implementation Topic`

예:

- `[FEAT] JavaScript Express Prisma Push API`
- `[TEST] JavaScript Express Prisma Ops E2E`
- `[CHORE] JavaScript Express Prisma CI`
- `[BUG] JavaScript Express Prisma Category Delete Guard`

### 4.4 issue / commit / branch 메타 규칙

이슈 문서 상단 메타는 아래 형식을 따른다.

`issue`

- `[TYPE] Language Implementation Topic 구현`

`commit`

- `type: (#번호) Language Implementation Topic 구현`

`branch`

- `type/#번호/language-implementation-topic`

예:

- `issue: "[TEST] JavaScript Express Prisma Security Regression E2E 구현"`
- `commit: "test: (#501) JavaScript Express Prisma Security Regression E2E 구현"`
- `branch: "test/#501/javascript-express-prisma-security-regression-e2e"`

## 5. 브랜치 규칙

### 5.1 기본 형식

브랜치 이름은 아래 형식을 따른다.

`type/#번호/slug`

예:

- `feat/#489/javascript-express-prisma-image-core-api`
- `test/#503/javascript-express-prisma-ops-e2e`
- `chore/#495/javascript-express-prisma-ci`

### 5.2 slug 규칙

slug는 아래 순서로 작성한다.

`language-implementation-topic`

예:

- `java-maven-jpa-swagger-openapi`
- `typescript-nest-typeorm-contract-admin-e2e`
- `python-fastapi-sqlalchemyorm-security-regression-e2e`

### 5.3 topic 규칙

topic은 아래 기준으로 만든다.

- 기능/API: `push-api`, `crawler-api`, `pc-builder-api`
- 테스트: `critical-e2e-test-base`, `security-regression-e2e`, `ops-e2e`
- 운영/CI: `ci`, `swagger-openapi`, `migration-validation`
- 버그: `category-delete-guard`, `fk-alignment`

## 6. 커밋 규칙

### 6.1 기본 형식

커밋 메시지는 아래 형식을 따른다.

`type: (#번호) Language Implementation Topic 구현`

예:

- `feat: (#463) JavaScript Express Prisma Push API 구현`
- `test: (#499) JavaScript Express Prisma Contract Admin E2E 구현`
- `chore: (#495) JavaScript Express Prisma CI 구현`
- `bug: (#453) JavaScript Express Prisma Category Delete Guard 수정`

### 6.2 동사 규칙

동사는 아래 기준을 따른다.

- `feat`, `test`, `chore`, `docs`, `refactor`: `구현`
- `bug`: `수정`

예:

- `test: (#501) JavaScript Express Prisma Security Regression E2E 구현`
- `bug: (#453) JavaScript Express Prisma Category Delete Guard 수정`

## 7. 공통 문서 규칙

### 7.1 루트 문서

루트 `Document` 문서는 아래 형식을 따른다.

`NN_topic.md`

예:

- `01_requirements.md`
- `02_api-specification.md`
- `03_erd.md`
- `04_language.md`
- `05_test-specification.md`
- `06_ci-specification.md`
- `07_code-convention.md`

규칙:

- 번호 2자리 고정
- topic은 소문자 + kebab-case
- 공통 설계 문서는 언어명을 넣지 않는다

### 7.2 언어별 문서

언어별 하위 폴더 문서는 언어 폴더 아래에서 별도 관리한다.

예:

- `Document/JavaScript/00_next-steps-checklist.md`
- `Document/TypeScript/01_folder-structure.md`

언어 폴더명은 표시용 대문자를 허용하지만, 파일명은 숫자 prefix + kebab-case를 유지한다.

## 8. 추천 slug 사전

반복적으로 쓰는 topic은 아래 표현을 우선 사용한다.

### 8.1 기능/API

- `auth-api`
- `account-catalog-api`
- `price-analytics-api`
- `prediction-api`
- `trust-api`
- `i18n-api`
- `crawler-api`
- `admin-settings-api`
- `image-core-api`
- `image-attachments-api`
- `pc-builder-api`

### 8.2 테스트

- `critical-e2e-test-base`
- `contract-admin-e2e`
- `security-regression-e2e`
- `ops-e2e`
- `migration-validation`
- `migration-roundtrip`
- `critical-stability`
- `dependency-failure-e2e`

### 8.3 운영/문서/정리

- `ci`
- `swagger-openapi`
- `structure-hardening`
- `project-rename`

## 9. 지양 사항

- 같은 의미를 다른 slug로 혼용하는 것
  - 예: `open-api`, `openapi`, `swagger-docs`를 섞는 방식
- 언어 slug를 제멋대로 바꾸는 것
  - 예: `js`, `node`, `javascript` 혼용
- 타입과 실제 작업 성격이 맞지 않는 것
  - 예: CI 작업을 `feat`로 기록
- 제목에 장황한 설명문을 넣는 것
  - 예: `Add all remaining tests for javascript backend`
- 브랜치에 불필요한 동사나 조사 넣는 것
  - 예: `feat/#501/add-javascript-security-test`

## 10. 결론

이 문서의 핵심은 “모든 언어 트랙에서 같은 방식으로 이름을 짓는 것”이다.

앞으로 모든 구현체는 아래 규칙을 우선 따른다.

1. 언어 slug는 고정한다.
2. 이슈/브랜치/커밋은 같은 topic을 공유한다.
3. 작업 성격에 맞는 타입을 쓴다.
4. 제목과 slug는 짧은 명사형으로 유지한다.
5. 공통 문서는 번호형 파일명 규칙을 유지한다.
