---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 랭킹 모듈 구현"
labels: feature
issue: "[FEAT] 랭킹 모듈 구현"
commit: "feat: (#41) 인기 상품/검색어 랭킹 및 점수 재계산 API 구현"
branch: "feat/#41/ranking-module"
assignees: ""
---

## ✨ 기능 요약

> 인기 상품 랭킹, 인기 검색어 랭킹 조회 기능과 관리자 인기 점수 재계산 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 랭킹 DTO 구현 (`RankingQueryDto`)
- [x] 랭킹 모듈/서비스/컨트롤러 추가
- [x] 인기 상품 랭킹 API 구현 (`GET /rankings/products/popular`)
- [x] 인기 검색어 랭킹 API 구현 (`GET /rankings/keywords/popular`)
- [x] 인기 점수 재계산 API 구현 (`POST /rankings/admin/recalculate`)
- [x] 인기 점수 공식 반영 (`조회수*0.3 + 리뷰수*0.5 + 판매량*0.2`)
- [x] 앱 모듈 등록 (`RankingModule`)
- [x] API 라우트 상수 추가 (`RANKING`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
