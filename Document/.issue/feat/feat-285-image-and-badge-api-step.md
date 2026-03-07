---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Image + Badge API (Step 31-32) 프론트엔드 연동"
labels: feature
issue: "[FEAT] Image + Badge API (Step 31-32) 프론트엔드 연동"
commit: "feat: (#285) image+badge API 연동 및 테스트 페이지 추가"
branch: "feat/#285/image-badge-api-step"
assignees: ""
---

## ✨ 기능 요약

> i18n API 다음 2단계인 Image API(31번), Badge API(32번)를 프론트엔드에 연동하고 수동 검증용 테스트 페이지를 추가했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] Image 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `ImageVariantItem`
  - `ImageUploadResult`
- [x] Image 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `uploadImage` (`POST /images/upload`, multipart/form-data)
  - `fetchImageVariants` (`GET /images/:id/variants`)
  - `removeImageAdmin` (`DELETE /images/:id`)
- [x] Image API 테스트 페이지 추가 (`FrontEnd/src/pages/ImageApiPage.tsx`)
  - 이미지 업로드 + 카테고리 선택
  - 변환본 조회
  - 관리자 삭제
- [x] Badge 타입 정의 추가 (`FrontEnd/src/lib/types.ts`)
  - `BadgeItem`
  - `UserBadgeItem`
- [x] Badge 엔드포인트 함수 추가 (`FrontEnd/src/lib/endpoints.ts`)
  - `fetchBadges` (`GET /badges`)
  - `fetchMyBadges` (`GET /badges/me`)
  - `fetchUserBadges` (`GET /users/:id/badges`)
  - `createBadgeAdmin` (`POST /admin/badges`)
  - `updateBadgeAdmin` (`PATCH /admin/badges/:id`)
  - `removeBadgeAdmin` (`DELETE /admin/badges/:id`)
  - `grantBadgeAdmin` (`POST /admin/badges/:id/grant`)
  - `revokeBadgeAdmin` (`DELETE /admin/badges/:id/revoke/:userId`)
- [x] Badge API 테스트 페이지 추가 (`FrontEnd/src/pages/BadgeApiPage.tsx`)
  - 전체/내/특정 유저 배지 조회
  - 관리자 배지 생성/수정/삭제
  - 관리자 배지 부여/회수
- [x] 라우트/헤더 메뉴 연결 (`FrontEnd/src/App.tsx`)
  - `/image-api` 경로 추가
  - 상단 메뉴 `ImageAPI` 링크 추가
  - `/badge-api` 경로 추가
  - 상단 메뉴 `BadgeAPI` 링크 추가
- [x] FrontEnd 프로덕션 빌드 검증 통과 (`npm run build`)

## ℹ️ 서버 구현 기준 차이

- [x] 이미지 업로드는 `multipart/form-data`가 필요해 공통 JSON request 래퍼 대신 전용 업로드 함수를 사용
- [x] 이번 프론트 단계는 현재 서버 구현(`/images/upload`, `/images/:id/variants`, `/images/:id`) 기준으로 반영
- [x] Badge API는 명세 문서와 서버 구현 라우트가 동일하여 그대로 반영
