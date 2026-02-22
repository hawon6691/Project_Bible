---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 상품 문의 모듈 구현"
labels: feature
issue: "[FEAT] 상품 문의 모듈 구현"
commit: "feat: (#29) 상품 문의 작성/목록/답변/내역/삭제 API 구현"
branch: "feat/#29/inquiry-module"
assignees: ""
---

## ✨ 기능 요약

> 상품 문의 작성, 상품별 문의 목록, 관리자/판매자 답변, 내 문의 목록 조회, 문의 삭제 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 문의 엔티티 구현 (`inquiries`)
- [x] 문의 상태 enum 정의 (`PENDING`, `ANSWERED`)
- [x] 문의 DTO 구현 (작성/답변/목록 쿼리)
- [x] 문의 모듈/서비스/컨트롤러 추가
- [x] 상품 문의 작성 API 구현 (`POST /products/:productId/inquiries`)
- [x] 상품 문의 목록 API 구현 (`GET /products/:productId/inquiries`)
- [x] 문의 답변 API 구현 (`POST /inquiries/:id/answer`) - 관리자/판매자 권한
- [x] 내 문의 목록 API 구현 (`GET /inquiries/me`)
- [x] 문의 삭제 API 구현 (`DELETE /inquiries/:id`) - 답변 전 작성자만
- [x] 비밀 문의 마스킹 처리 로직 추가
- [x] 앱 모듈 등록 (`InquiryModule`)
- [x] API 라우트 상수 추가 (`INQUIRIES`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
