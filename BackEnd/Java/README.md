# PBShop Java Backend

## 1. 경로

- Maven JPA 프로젝트: `BackEnd/Java/java-spring-maven-jpa-postgresql`
- Gradle 프로젝트: `BackEnd/Java/java-spring-gradle-jpa-postgresql`

## 2. 전제

- Java 21
- Maven/Gradle 전역 설치는 없어도 됨
- Maven Wrapper / Gradle Wrapper 사용
- 기본 포트: `8000`

## 3. 실행

### Maven JPA

```bash
cd BackEnd/Java/java-spring-maven-jpa-postgresql
mvnw.cmd spring-boot:run
```

### Gradle

```bash
cd BackEnd/Java/java-spring-gradle-jpa-postgresql
gradlew.bat bootRun
```

## 4. 테스트

### Maven JPA

```bash
cd BackEnd/Java/java-spring-maven-jpa-postgresql
mvnw.cmd test
```

### Gradle

```bash
cd BackEnd/Java/java-spring-gradle-jpa-postgresql
gradlew.bat test
```

## 5. 기본 데이터베이스 설정

기본값은 MySQL 기준이다.

```text
DB_URL=jdbc:mysql://127.0.0.1:3306/pbdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
DB_USERNAME=project_bible
DB_PASSWORD=project_bible
DB_DRIVER=com.mysql.cj.jdbc.Driver
```

PostgreSQL로 바꾸려면 실행 시 환경 변수만 교체하면 된다.

```text
DB_URL=jdbc:postgresql://127.0.0.1:5432/pbdb
DB_USERNAME=project_bible
DB_PASSWORD=project_bible
DB_DRIVER=org.postgresql.Driver
```

## 6. 확인 포인트

- 애플리케이션 포트: `8000`
- Health: `http://127.0.0.1:8000/actuator/health`
- 다음 구현 기준 문서:
  - `Document/01_requirements.md`
  - `Document/02_api-specification.md`
  - `Document/03_erd.md`
