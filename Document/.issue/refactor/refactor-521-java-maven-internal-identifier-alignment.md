---
name: "♻️ Refactor Request"
about: 기능 수정 제안
title: "[REFACT] Java Maven 내부 식별자 정리"
labels: refactor
assignees: ""
issue: "[REFACT] Java Maven 내부 식별자 정리"
commit: "refactor: (#521) java-maven 내부 패키지명 및 애플리케이션 식별자 정리"
branch: "refactor/#521/java-maven-internal-identifier-alignment"
---

## ♻️ 수정 요약

> Java Maven 구현체의 내부 패키지명, 애플리케이션 클래스명, Maven/Spring 식별자를 새 프로젝트 명명 규칙에 맞춰 정리했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Maven `artifactId`와 프로젝트 `name`을 `java-spring-maven-jpa-postgresql`, `pbshop-java-spring-maven-jpa-postgresql` 기준으로 정리한다.
- [x] Spring `application.name`과 로컬 로깅 패키지 기준을 새 내부 식별자에 맞춰 수정한다.
- [x] 루트 패키지를 `com.pbshop.springshop`에서 `com.pbshop.java.spring.maven.jpa.postgresql`로 일괄 정리한다.
- [x] 애플리케이션 진입 클래스와 테스트 루트 클래스를 `JavaSpringMavenJpaPostgresqlApplication` 기준으로 정리한다.
- [x] 패키지 이동 이후 `mvnw -Dspring.profiles.active=ci clean verify`가 통과하도록 정합성을 맞춘다.
