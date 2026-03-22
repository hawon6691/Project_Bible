---
name: "♻️ Refactor Request"
about: 기능 수정 제안
title: "[REFACT] 남은 백엔드 내부 식별자 정리"
labels: refactor
assignees: ""
issue: "[REFACT] 남은 백엔드 내부 식별자 정리"
commit: "refactor: (#525) 남은 백엔드 내부 식별자 정리"
branch: "refactor/#525/remaining-backend-internal-identifier-alignment"
---

## ♻️ 수정 요약

> 어떤 기능을 수정한 것인지 한 줄로 설명해주세요.

Java Gradle, Kotlin, PHP, JavaScript 구현체의 남아 있던 내부 패키지명, 앱 식별자, 기본 브랜딩 문자열을 현재 프로젝트 명명 규칙에 맞춰 정리한 작업입니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Java Gradle 구현체의 `rootProject.name`, `spring.application.name`, 루트 패키지, 애플리케이션 클래스 및 테스트 클래스 식별자를 새 명명 규칙에 맞춰 정리한다.
- [x] Kotlin 구현체의 루트 패키지, `mainClass`, Ktor module 경로, 런타임 `appName` 식별자를 새 명명 규칙에 맞춰 정리한다.
- [x] PHP 구현체의 Composer 프로젝트 식별자, 기본 앱명, 메일 발신자, 로그 파일명, 캐시·세션·Redis prefix fallback, 기본 DB 이름을 PBShop 기준으로 정리한다.
- [x] JavaScript 구현체의 기본 `APP_NAME`과 health 응답 앱 식별자를 `pbshop-javascript-express-npm-prisma-postgresql` 기준으로 정리한다.
- [x] Java Gradle과 Kotlin은 각각 `.\gradlew.bat ... test`, JavaScript는 `npm run prisma:generate`, `npm run test:e2e:platform`으로 검증하고, PHP는 `composer` 부재 환경에서 변경 파일 `php -l` 문법 검증까지 확인한다.
