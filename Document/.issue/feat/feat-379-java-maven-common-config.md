---
name: "Java Maven Common Config"
about: "Java Spring Boot Maven 공통 환경 설정 정리"
title: "[FEAT] Java Maven 공통 환경 설정 정리"
labels: ["documentation", "backend", "java", "spring-boot", "maven", "configuration"]
assignees: []
issue: "[FEAT] Java Maven 공통 환경 설정 정리"
commit: "feat: (#379) Java Maven 공통 환경 설정 정리"
branch: "feat/#379/java-maven-common-config"
---

## ✨ 기능 요약

Java Maven 기준 구현의 공통 환경 설정을 정리한다. 실행 프로필, 데이터베이스 기본값, 로케일/문서 경로, 파일 업로드 제한을 공통 속성으로 분리하고 테스트 프로필까지 함께 구성한다.

## 📋 요구사항

- [x] 공통 애플리케이션 설정 정리
  - [x] 기본 프로필 `local`
  - [x] 포트 `8000`
  - [x] graceful shutdown
  - [x] Jackson timezone 기본값 `Asia/Seoul`
- [x] 공통 런타임 환경 변수 정리
  - [x] MySQL 기본 DB 연결값
  - [x] 파일 업로드 제한
  - [x] API base path
  - [x] frontend URL
  - [x] 기본 로케일/지원 로케일
  - [x] Swagger/OpenAPI 문서 경로
- [x] 프로필 분리
  - [x] `application-local.properties`
  - [x] `application-test.properties`
- [x] 테스트 프로필 보강
  - [x] H2 in-memory DB 사용
  - [x] `create-drop`
  - [x] actuator 기본 비활성화
- [x] 설정 바인딩 클래스 추가
  - [x] `PbshopProperties`
  - [x] `PbshopConfiguration`
- [x] 공통 설정 테스트 추가
  - [x] `PbshopPropertiesTest`
  - [x] API base path, frontend URL, locale, docs path 검증
- [x] Maven 의존성 보강
  - [x] `spring-boot-configuration-processor`
  - [x] `h2` test dependency
- [x] Maven 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `2. 공통 환경 설정 정리`에 해당한다.
- 다음 단계는 `3. DB 연결 및 마이그레이션 전략 확정`이다.
