---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Image Attachments API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Image Attachments API 구현"
commit: "feat: (#491) JavaScript Express Prisma Image Attachments API 구현"
branch: "feat/#491/javascript-express-prisma-image-attachments-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 프로필 이미지, 상품 이미지, 레거시 이미지 업로드 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `users/me/profile-image` 프로필 이미지 업로드 API 추가
- [x] `users/me/profile-image` 프로필 이미지 삭제 API 추가
- [x] `products/:id/images` 상품 이미지 업로드 API 추가
- [x] `products/:id/images/:imageId` 상품 이미지 삭제 API 추가
- [x] `upload/image` 레거시 이미지 업로드 API 추가
- [x] `product_images`와 `image_variants` relation 매핑 보강
- [x] 기존 `images/upload` 공용 업로드 로직 재사용
- [x] 기존 `users`, `products`, `images` 라우트에 경로 연결
- [x] README 노출 경로 요약 갱신
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
