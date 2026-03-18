---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Resilience API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Resilience API 구현"
commit: "feat: (#479) JavaScript Express Prisma Resilience API 구현"
branch: "feat/#479/javascript-express-prisma-resilience-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 Circuit Breaker 상태 조회 및 초기화 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `resilience/circuit-breakers` Circuit Breaker 상태 목록 조회 API 추가
- [x] `resilience/circuit-breakers/policies` Circuit Breaker 정책/통계 조회 API 추가
- [x] `resilience/circuit-breakers/:name` Circuit Breaker 단건 상태 조회 API 추가
- [x] `resilience/circuit-breakers/:name/reset` Circuit Breaker 수동 초기화 API 추가
- [x] 라우트 인덱스에 `resilience` 라우터 연결
- [x] README 노출 경로 요약 갱신
- [x] 대표 엔드포인트 수동 검증 완료
