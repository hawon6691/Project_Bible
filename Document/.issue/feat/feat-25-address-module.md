---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 배송지 모듈 구현"
labels: feature
issue: "[FEAT] 배송지 모듈 구현"
commit: "feat: (#25) 배송지 CRUD 및 주문 배송지 엔티티 연동"
branch: "feat/#25/address-module"
assignees: ""
---

## ✨ 기능 요약

> 사용자 배송지 CRUD 기능을 구현하고, 주문 생성 시 배송지 정보를 문자열 테이블 조회가 아닌 Address 엔티티 기반으로 연동했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 배송지 엔티티 구현 (`addresses`)
- [x] 배송지 모듈/서비스/컨트롤러 추가
- [x] 내 배송지 목록 조회 API 구현 (`GET /addresses`)
- [x] 배송지 추가 API 구현 (`POST /addresses`)
- [x] 배송지 수정 API 구현 (`PATCH /addresses/:id`)
- [x] 배송지 삭제 API 구현 (`DELETE /addresses/:id`)
- [x] 기본 배송지 정책 구현 (최초 생성 시 기본 지정, 기본 배송지 삭제 시 재지정)
- [x] 배송지 DTO 검증 추가 (한국 전화번호 validator, 우편번호 형식 검증)
- [x] 주문 생성 로직의 배송지 조회를 Address 엔티티 기반으로 변경
- [x] 앱 모듈 등록 (`AddressModule`)
- [x] API 라우트 상수 보정 및 추가 (`POINTS` 괄호 누락 수정, `ADDRESSES` 추가)
- [x] 주요 로직/엔티티/DTO 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
