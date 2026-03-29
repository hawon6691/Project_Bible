---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Kotlin Ktor Exposed DAO PcBuilder Friend Shortform API"
labels: feature
assignees: ""
issue: "[FEAT] Kotlin Ktor Exposed DAO PcBuilder Friend Shortform API 구현"
commit: "feat: (#567) Kotlin Ktor Exposed DAO PcBuilder Friend Shortform API 구현"
branch: "feat/#567/kotlin-ktor-exposeddao-pcbuilder-friend-shortform-api"
---

## ✨ 기능 요약

Kotlin 기준 구현체에 PcBuilder, Friend, Shortform API를 다음 실제 구현 도메인으로 추가하고, 현재 stub 라우트를 실제 controller service repository 흐름으로 전환한다.

## 📋 요구사항

- [x] Kotlin PcBuilder 도메인에 PC 빌드 목록/상세, 생성/수정/삭제, 부품 추가/제거, 호환성 조회, 공유 링크, 인기 견적, 관리자 호환성 규칙 CRUD 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Friend 도메인에 친구 신청/수락/거절, 친구 목록, 받은 요청/보낸 요청, 활동 피드, 차단/차단 해제, 친구 삭제 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] Kotlin Shortform 도메인에 multipart 업로드, 목록/상세, 좋아요 토글, 댓글 작성/조회, 랭킹, 트랜스코드 상태/재시도, 삭제, 사용자별 목록 엔드포인트를 실제 controller 기반으로 구현한다.
- [x] PcBuilder, Friend, Shortform service에 입력 검증, 권한 검증, 소유권 검사, 관계 상태 전이, 규칙 기반 호환성 판정, 응답 조립 흐름을 추가한다.
- [x] PcBuilder, Friend, Shortform repository를 인터페이스 기반으로 정리하고 런타임용 PostgreSQL Exposed DAO/DSL 구현과 테스트용 in-memory 구현을 분리한다.
- [x] 관련 Exposed 매핑과 Application, Routing, PbShopTestSupport wiring을 확장하고, 세 도메인 계약이 실제 라우팅 흐름으로 연결되도록 정리한다.
- [x] Kotlin 테스트에서 pcbuilder, friend, shortform 실제 계약 회귀 시나리오를 추가하고 `.\gradlew.bat compileKotlin`, `.\gradlew.bat compileTestKotlin`, `.\gradlew.bat test`를 통과한다.
