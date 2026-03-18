---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Image Core API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Image Core API 구현"
commit: "feat: (#489) JavaScript Express Prisma Image Core API 구현"
branch: "feat/#489/javascript-express-prisma-image-core-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 이미지 업로드, 변환본 조회, 관리자 삭제 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `images/upload` 이미지 업로드 및 최적화 메타데이터 생성 API 추가
- [x] `images/:id/variants` 이미지 변환본 조회 API 추가
- [x] `images/:id` 관리자 이미지 삭제 API 추가
- [x] `multipart/form-data` 처리용 업로드 미들웨어 추가
- [x] Prisma schema에 `image_assets`, `image_variants` 매핑 추가
- [x] 라우트 인덱스에 `images` 라우터 연결
- [x] README 노출 경로 요약 갱신
- [x] `multer` 의존성 추가
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
