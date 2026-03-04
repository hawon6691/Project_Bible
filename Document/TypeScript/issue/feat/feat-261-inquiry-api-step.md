---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 상품 문의 API (Step 16) 프론트엔드 연동"
labels: feature
issue: "[FEAT] 상품 문의 API (Step 16) 프론트엔드 연동"
commit: "feat: (#261) inquiry API 연동 및 Inquiry API 테스트 페이지 추가"
branch: "feat/#261/inquiry-api-step"
assignees: ""
---

## ✨ 기능 요약

> 커뮤니티 API(15번) 다음 단계인 상품 문의 API(16번)를 프론트엔드에 연동하고, 수동 검증용 Inquiry API 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 상품 문의 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `InquiryItem`
- [x] 상품 문의 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchProductInquiries` (`GET /products/:productId/inquiries`)
  - `createProductInquiry` (`POST /products/:productId/inquiries`)
  - `answerInquiry` (`POST /inquiries/:id/answer`)
  - `fetchMyInquiries` (`GET /inquiries/me`)
  - `removeInquiry` (`DELETE /inquiries/:id`)
- [x] 상품 문의 API 테스트 페이지 추가 (`FrontEnd/src/pages/InquiryApiPage.tsx`)
  - 상품 문의 목록 조회
  - 문의 작성
  - 문의 답변
  - 내 문의 목록 조회
  - 문의 삭제
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/inquiry-api` 경로 추가
  - 상단 메뉴 `InquiryAPI` 링크 추가

## ⚠ 검증 메모

- [ ] `npm run build` 전체 통과
  - 현재 저장소 기준 기존 타입/의존성 이슈(`react-router-dom` 타입, `import.meta.env`, 일부 implicit any)로 전체 빌드 실패
  - 이번 단계에서 추가한 Inquiry 관련 파일 자체의 문법/타입 오류는 확인되지 않음
