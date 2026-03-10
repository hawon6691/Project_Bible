---
name: "Java Maven DB Migration Strategy"
about: "Java Spring Boot Maven DB 연결 및 마이그레이션 전략 확정"
title: "[FEAT] Java Maven DB 연결 및 마이그레이션 전략 확정"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "database", "flyway"]
assignees: []
issue: "[FEAT] Java Maven DB 연결 및 마이그레이션 전략 확정"
commit: "feat: (#381) Java Maven DB 연결 및 마이그레이션 전략 확정"
branch: "feat/#381/java-maven-db-migration-strategy"
---

## ✨ 기능 요약

Java Maven 기준 구현의 데이터베이스 연결 방식을 Flyway 마이그레이션 중심으로 고정한다. 기본 실행은 MySQL, 테스트는 H2 in-memory를 사용하고 공통 초기 스키마를 migration으로 관리한다.

## 📋 요구사항

- [x] DB 마이그레이션 도구 확정
  - [x] `Flyway` 의존성 추가
- [x] Hibernate 자동 스키마 생성 비활성화
  - [x] `spring.jpa.hibernate.ddl-auto=validate`
  - [x] `spring.sql.init.mode=never`
- [x] Flyway 공통 설정 추가
  - [x] `spring.flyway.enabled=true`
  - [x] `spring.flyway.locations=classpath:db/migration`
  - [x] `spring.flyway.baseline-on-migrate=true`
  - [x] migration naming validation 활성화
- [x] 테스트 환경 DB 전략 정리
  - [x] H2 in-memory + MySQL mode 유지
  - [x] test 프로필에서도 Flyway 기반 schema 생성
  - [x] `spring.flyway.clean-disabled=false`
- [x] 초기 스키마 migration 추가
  - [x] `V1__init_pbshop_core.sql`
  - [x] `users`
  - [x] `categories`
  - [x] `sellers`
  - [x] `products`
  - [x] `product_specs`
  - [x] `price_entries`
  - [x] `system_settings`
- [x] DB/Flyway 검증 테스트 추가
  - [x] `FlywayMigrationTest`
  - [x] version `1` 적용 확인
  - [x] 핵심 테이블 생성 확인
- [x] 기존 테스트를 test profile로 고정
  - [x] `SpringshopApplicationTests`
  - [x] `SystemControllerTest`
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 테스트 과정에서 기본 프로필이 MySQL을 바라보던 문제를 `@ActiveProfiles("test")`로 정리했다.
- 다음 단계는 `4. 공통 응답 포맷/예외 처리/보안 기본 구성`이다.
