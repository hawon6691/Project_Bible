---
title: "[FEAT] Java Spring Boot 백엔드 부트스트랩"
labels: ["backend", "java", "spring-boot"]
issue: "[FEAT] Java Spring Boot 백엔드 부트스트랩"
commit: "feat: (#373) Java Spring Boot 백엔드 부트스트랩"
branch: "feat/#373/java-spring-bootstrap"
---

## ✨ 기능 요약
- Java 트랙의 기준 구현 시작을 위해 `BackEnd/Java/springshop`에 Spring Boot 프로젝트를 부트스트랩한다.

## 📋 요구사항
- [x] Java 백엔드 경로 및 로컬 도구 상태 확인
  - [x] `BackEnd/Java/springshop` 확인
  - [x] Java 21 확인
  - [x] 전역 `Gradle`, `Maven` 미설치 확인
- [x] Spring Initializr 기반 Maven 프로젝트 생성
  - [x] Maven Wrapper 포함
  - [x] Java 21
  - [x] Spring Boot 3.5.x
  - [x] 기본 의존성 포함
    - [x] Web
    - [x] Validation
    - [x] Data JPA
    - [x] Security
    - [x] Actuator
    - [x] WebSocket
    - [x] OAuth2 Client
    - [x] Mail
    - [x] MySQL
    - [x] PostgreSQL
- [x] PBShop 기준 기본 설정 반영
  - [x] 앱 이름 `pbshop-springshop`
  - [x] 포트 `8000`
  - [x] 기본 DB 환경 변수 정리
  - [x] Health/Info/Prometheus actuator 노출
- [x] Java 전용 실행 README 작성
  - [x] `BackEnd/Java/README.md`
- [x] Maven Wrapper 테스트 실행 검증
  - [x] `mvnw.cmd test`
  - [x] Spring Boot 기본 테스트 통과

## 📂 영향 범위
- `BackEnd/Java/springshop/pom.xml`
- `BackEnd/Java/springshop/src/main/resources/application.properties`
- `BackEnd/Java/springshop/*`
- `BackEnd/Java/README.md`

## ✅ 완료 기준
- [x] Spring Boot 프로젝트 생성
- [x] PBShop 기준 포트/DB 기본값 반영
- [x] Java README 작성
- [x] Maven Wrapper 테스트 통과
