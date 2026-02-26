---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Redlock 분산 락 모듈 구현"
labels: feature
issue: "[FEAT] Redlock 분산 락 모듈 구현"
commit: "feat: (#97) 주문 생성 재고 동시성 제어를 위한 Redis 분산 락 적용"
branch: "feat/#97/redlock-module"
assignees: ""
---

## ✨ 기능 요약

> 동시 주문 시 재고 정합성을 보장하기 위해 Redis 기반 분산 락(Redlock 패턴)을 구현하고 주문 생성 경로에 적용했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Redlock 모듈/서비스 추가
- [x] 다중 키 분산 락 획득 로직 구현 (정렬 기반 순차 획득)
- [x] 락 획득 재시도 옵션 구현 (TTL/재시도 횟수/지연시간)
- [x] 토큰 검증 기반 락 해제 로직 구현 (Lua compare-and-delete)
- [x] Redis 연결 관리 로직 구현 (`ensureConnection`, `onModuleDestroy`)
- [x] 주문 생성 경로에 분산 락 적용 (`OrderService.create`)
- [x] 상품 단위 락 키 적용 (`lock:stock:product:{productId}`)
- [x] 주문 처리 종료 시 락 해제 보장 (`finally`)
- [x] 주문 모듈 의존성 추가 (`RedlockModule`)
- [x] 앱 모듈 등록 (`RedlockModule`)
- [x] 동시 주문 경합 시 Conflict 오류 메시지 처리
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
