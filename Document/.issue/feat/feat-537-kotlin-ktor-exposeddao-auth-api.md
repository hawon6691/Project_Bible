---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO Auth API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO Auth API 구현"
commit: "feat: (#537) Kotlin Ktor Exposed DAO Auth API 구현"
branch: "feat/#537/kotlin-ktor-exposeddao-auth-api"
---

## ✨ 기능 요약

> 어떤 기능인지 한 줄로 설명해주세요.

Kotlin 기준 구현체에 Auth API를 첫 번째 실제 구현 도메인으로 추가하고, stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Kotlin Auth 도메인에 회원가입, 이메일 인증, 인증 재발송, 로그인, 로그아웃, 토큰 갱신, 비밀번호 재설정, 소셜 로그인 관련 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Auth service에 입력 검증, 비밀번호 해시 처리, 인증코드 검증, 토큰 발급 및 비밀번호 재설정 흐름을 추가한다.
- [x] Auth repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL JDBC 구현과 테스트용 in-memory 구현을 분리한다.
- [x] Application 및 Routing wiring에서 Auth repository 주입 구조를 반영하고, 테스트 환경에서는 in-memory auth repository를 기본 사용하도록 정리한다.
- [x] Auth OpenAPI 메타데이터를 현재 엔드포인트 계약에 맞게 갱신하고, Kotlin 테스트에서 auth 요청 body와 회귀 시나리오를 검증한다.
- [x] `.\gradlew.bat test`를 통과한다.
