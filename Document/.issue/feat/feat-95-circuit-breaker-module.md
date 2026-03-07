---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Circuit Breaker 모듈 구현"
labels: feature
issue: "[FEAT] Circuit Breaker 모듈 구현"
commit: "feat: (#95) 외부 결제 호출 Circuit Breaker 및 관리자 모니터링 API 구현"
branch: "feat/#95/circuit-breaker-module"
assignees: ""
---

## ✨ 기능 요약

> 외부 결제 호출 안정화를 위해 Circuit Breaker(닫힘/열림/반열림) 로직을 구현하고 관리자 상태 조회/초기화 API를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Resilience 모듈/서비스/컨트롤러 추가
- [x] Circuit Breaker 상태 모델 구현 (`CLOSED`, `OPEN`, `HALF_OPEN`)
- [x] 실패 임계치/열림 유지 시간/반열림 성공 임계치 옵션 지원
- [x] Circuit Breaker 상태 목록 조회 API 구현 (`GET /resilience/circuit-breakers`)
- [x] Circuit Breaker 단건 조회 API 구현 (`GET /resilience/circuit-breakers/:name`)
- [x] Circuit Breaker 수동 초기화 API 구현 (`POST /resilience/circuit-breakers/:name/reset`)
- [x] 결제 요청 경로에 Circuit Breaker 적용 (`OrderService.requestPayment`)
- [x] 외부 결제 실패 시 결제 상태 FAILED 처리 로직 추가
- [x] 회로 열림 시 사용자 친화 오류 메시지 반환 로직 추가
- [x] 결제 게이트웨이 모의 호출 함수 분리 (`simulateExternalPayment`)
- [x] 앱 모듈 등록 (`ResilienceModule`)
- [x] API 라우트 상수 확장 (`RESILIENCE`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
