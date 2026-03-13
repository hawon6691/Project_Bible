---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Java Maven Analytics / Used Market / Auto / Auction / Compare API"
labels: feature
assignees: ""
issue: "[FEAT] Java Maven Analytics Used Market Auto Auction Compare API 구현"
commit: "feat: (#415) Java Maven Analytics Used Market Auto Auction Compare API 구현"
branch: "feat/#415/java-maven-analytics-used-market-auto-auction-compare-api"
---

## ✨ 기능 요약

> Spring Boot Maven 백엔드에 가격 분석, 중고 시세, 자동차 견적, 역경매, 비교함 API를 추가하고 관련 마이그레이션과 테스트를 함께 정리한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `V13__analytics_used_market_auto_auction_compare_support.sql` 마이그레이션 추가
- [x] `analytics` 도메인 컨트롤러/서비스 추가
- [x] `usedmarket` 도메인 엔티티, 리포지토리, 서비스, 컨트롤러 추가
- [x] `automotive` 도메인 엔티티, 리포지토리, 서비스, 컨트롤러 추가
- [x] `auction` 도메인 엔티티, 리포지토리, 서비스, 컨트롤러 추가
- [x] `compare` 도메인 엔티티, 리포지토리, 서비스, 컨트롤러 추가
- [x] `SecurityConfig`에 공개 조회 및 비교/자동차 견적 경로 반영
- [x] `FlywayMigrationTest`에 버전 `13` 및 신규 테이블 검증 반영
- [x] `AnalyticsUsedMarketAutoAuctionCompareApiTest` 통합 테스트 추가
- [x] `cmd /c mvnw.cmd -Dtest=AnalyticsUsedMarketAutoAuctionCompareApiTest test` 통과
- [x] `cmd /c mvnw.cmd test` 전체 테스트 통과 확인
- [x] 전체 테스트 과정에서 드러난 `#409` 응답 `Map.of(null)` 및 `405` 처리 안정화 보정
