---
title: "[FEAT] Java Maven/Gradle 이중 부트스트랩"
labels: ["backend", "java", "spring-boot", "maven", "gradle"]
issue: "[FEAT] Java Maven/Gradle 이중 부트스트랩"
commit: "feat: (#375) Java Maven Gradle 이중 부트스트랩"
branch: "feat/#375/java-maven-gradle-dual-bootstrap"
---

## ✨ 기능 요약
- Java Spring Boot 기준 구현을 `Maven`과 `Gradle` 두 형태로 동시에 유지할 수 있도록 프로젝트를 이중 부트스트랩한다.

## 📋 요구사항
- [x] 기존 `springshop` Maven 프로젝트를 `springshop_maven`으로 재정렬
- [x] 동일 기준의 `springshop_gradle` 프로젝트 추가 생성
- [x] 두 프로젝트 모두 PBShop 기본값 반영
  - [x] 포트 `8000`
  - [x] MySQL 기본 DB 환경 변수
  - [x] PostgreSQL 전환 가능 구조
  - [x] actuator health/info/prometheus 노출
- [x] Maven/Gradle 이름 분리
  - [x] Maven 앱 이름 `pbshop-springshop-maven`
  - [x] Gradle 앱 이름 `pbshop-springshop-gradle`
- [x] Java README를 이중 실행 기준으로 갱신
- [x] 두 프로젝트 실행 검증
  - [x] `mvnw.cmd test`
  - [x] `gradlew.bat test`

## 📂 영향 범위
- `BackEnd/Java/springshop_maven/*`
- `BackEnd/Java/springshop_gradle/*`
- `BackEnd/Java/README.md`

## ✅ 완료 기준
- [x] Maven/Gradle 두 부트스트랩 프로젝트 공존
- [x] 두 프로젝트 기본 설정 정렬
- [x] 두 wrapper 테스트 성공
