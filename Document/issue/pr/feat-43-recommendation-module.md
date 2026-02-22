---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 추천 모듈 구현"
labels: feature
issue: "[FEAT] 추천 모듈 구현"
commit: "feat: (#43) 개인화/트렌딩 추천 API 구현"
branch: "feat/#43/recommendation-module"
assignees: ""
---

## ✨ 기능 요약

> 찜/리뷰 기반 개인화 추천과 인기 점수 기반 트렌딩 추천 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 추천 DTO 구현 (`RecommendationQueryDto`)
- [x] 추천 모듈/서비스/컨트롤러 추가
- [x] 개인화 추천 API 구현 (`GET /recommendations/personal`)
- [x] 트렌딩 추천 API 구현 (`GET /recommendations/trending`)
- [x] 찜/리뷰 기반 선호 카테고리 계산 로직 추가
- [x] 사용자 상호작용 상품 제외 로직 추가 (찜/리뷰 상품 제외)
- [x] 개인화 데이터 부족 시 인기 추천 폴백 처리
- [x] 앱 모듈 등록 (`RecommendationModule`)
- [x] API 라우트 상수 추가 (`RECOMMENDATION`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
