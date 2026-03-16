---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Account Catalog API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Account Catalog API 구현"
commit: "feat: (#451) JavaScript Express Prisma Account Catalog API 구현"
branch: "feat/#451/javascript-express-prisma-account-catalog-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 계정 인증 보완과 카탈로그 쓰기 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `auth` 이메일 인증, 인증 재발송, 비밀번호 재설정 요청/검증/확정 API 추가
- [x] `users` 회원 탈퇴, 상태 변경, 내 프로필 수정 API 추가
- [x] `categories` 단건 조회 및 관리자 CRUD API 추가
- [x] `products` 관리자 CRUD 및 옵션 CRUD API 추가
- [x] `specs` 정의 CRUD, 상품 스펙 설정, 비교, 점수 비교, 점수 매핑 API 추가
- [x] `sellers` 조회 및 관리자 CRUD API 추가
- [x] `prices` 등록/수정/삭제 및 `price-alerts` 조회/등록/삭제 API 추가
- [x] Prisma schema에 `email_verifications`, `spec_scores` 매핑 추가
- [x] 샘플 데이터 기준 생성 시퀀스 보정 처리 반영
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
