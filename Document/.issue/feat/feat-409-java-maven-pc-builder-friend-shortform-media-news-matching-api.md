---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Java Maven PC Builder / Friend / Shortform / Media / News / Matching API"
labels: feature
assignees: ""
issue: "[FEAT] Java Maven PC Builder Friend Shortform Media News Matching API 구현"
commit: "feat: (#409) Java Maven PC Builder Friend Shortform Media News Matching API 구현"
branch: "feat/#409/java-maven-pc-builder-friend-shortform-media-news-matching-api"
---

## ✨ 기능 요약

> Spring Boot Maven 백엔드에 PC 빌더, 친구, 숏폼, 미디어, 뉴스, 매칭 API를 추가하고, 관련 마이그레이션과 테스트를 함께 정리한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `V12__pc_friend_shortform_media_news_matching_support.sql` 마이그레이션 추가
- [x] `pcbuilder` 도메인 엔티티, 리포지토리, 서비스, 컨트롤러 추가
- [x] `friend` 도메인 엔티티, 리포지토리, 서비스, 컨트롤러 추가
- [x] `shortform` 도메인 엔티티, 리포지토리, 서비스, 컨트롤러 추가
- [x] `media` 도메인 엔티티, 리포지토리, 서비스, 컨트롤러 추가
- [x] `news` 도메인 엔티티, 리포지토리, 서비스, 컨트롤러 추가
- [x] `matching` 도메인 엔티티, 리포지토리, 서비스, 컨트롤러 추가
- [x] `SecurityConfig`에 공개 조회용 GET 엔드포인트 반영
- [x] `FlywayMigrationTest`에 버전 `12` 및 신규 테이블 검증 반영
- [x] `PcFriendShortformMediaNewsMatchingApiTest` 통합 테스트 초안 추가
- [x] Java 제네릭 및 `Map.of` 관련 컴파일 이슈 1차 보정
- [ ] `PcFriendShortformMediaNewsMatchingApiTest` 재실행 후 실패 케이스 최종 보정
- [ ] 전체 `mvnw test` 통과 확인
