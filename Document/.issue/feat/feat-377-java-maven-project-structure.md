---
name: "Java Maven Project Structure"
about: "Java Spring Boot Maven 프로젝트 구조 정리"
title: "[FEAT] Java Maven 프로젝트 구조 정리"
labels: ["documentation", "backend", "java", "spring-boot", "maven"]
assignees: []
issue: "[FEAT] Java Maven 프로젝트 구조 정리"
commit: "feat: (#377) Java Maven 프로젝트 구조 정리"
branch: "feat/#377/java-maven-project-structure"
---

## ✨ 기능 요약

Java `springshop_maven` 기준 구현을 시작하기 위한 기본 프로젝트 구조를 정리한다. 공통 응답 포맷, 보안 기본 설정, 시스템 상태 확인 엔드포인트를 먼저 배치해 이후 모듈 구현의 기준점을 만든다.

## 📋 요구사항

- [x] Maven 기준 Spring Boot 프로젝트 식별자 정리
  - [x] `artifactId`: `springshop-maven`
  - [x] `name`: `pbshop-springshop-maven`
  - [x] `description`: `PBShop Java Spring Boot Maven reference backend`
- [x] Maven 프로젝트 기본 리소스 설정 정리
  - [x] 애플리케이션 이름 `pbshop-springshop-maven`
  - [x] 기본 포트 `8000`
  - [x] MySQL 기본 연결값 환경 변수 기반 구성
  - [x] actuator `health`, `info`, `prometheus` 노출 유지
- [x] 공통 패키지 구조 선행 정리
  - [x] `common`
  - [x] `config`
  - [x] `system`
  - [x] `auth`
  - [x] `user`
  - [x] `category`
  - [x] `product`
- [x] 공통 응답 포맷 클래스 추가
  - [x] `ApiResponse`
  - [x] `ApiMeta`
  - [x] `ApiError`
- [x] 보안 기본 설정 추가
  - [x] `/api/v1/health` 공개
  - [x] actuator 공개 경로 허용
  - [x] 나머지 경로 인증 기본값 적용
- [x] 시스템 상태 확인 컨트롤러 추가
  - [x] `GET /api/v1/health`
  - [x] 응답에 `status`, `application` 포함
- [x] Maven 테스트 보강
  - [x] `SystemControllerTest` 추가
  - [x] `/api/v1/health` 응답 검증
- [x] Maven 기본 테스트 검증 통과
  - [x] `cmd /c mvnw.cmd test`

## 📌 참고

- 대상 프로젝트: `BackEnd/Java/springshop_maven`
- 이번 단계는 Java Maven 트랙의 `1. 프로젝트 구조 정리`에 해당한다.
- 다음 단계는 `2. 공통 환경 설정 정리`이다.
