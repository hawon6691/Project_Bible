---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] JavaScript Express Prisma Pc Builder API"
labels: feature
assignees: ""
issue: "[FEAT] JavaScript Express Prisma Pc Builder API 구현"
commit: "feat: (#487) JavaScript Express Prisma Pc Builder API 구현"
branch: "feat/#487/javascript-express-prisma-pc-builder-api"
---

## ✨ 기능 요약

> JavaScript Express Prisma 백엔드에 PC 견적 관리 및 호환성 규칙 API를 추가한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `pc-builds` 내 견적 목록 조회 API 추가
- [x] `pc-builds` 견적 생성 API 추가
- [x] `pc-builds/:id` 견적 상세 조회 API 추가
- [x] `pc-builds/:id` 견적 수정 API 추가
- [x] `pc-builds/:id` 견적 삭제 API 추가
- [x] `pc-builds/:id/parts` 견적 부품 추가 API 추가
- [x] `pc-builds/:id/parts/:partId` 견적 부품 제거 API 추가
- [x] `pc-builds/:id/compatibility` 호환성 체크 API 추가
- [x] `pc-builds/:id/share` 공유 링크 생성 API 추가
- [x] `pc-builds/shared/:shareCode` 공유 견적 조회 API 추가
- [x] `pc-builds/popular` 인기 견적 목록 조회 API 추가
- [x] `admin/compatibility-rules` 호환성 규칙 목록/생성/수정/삭제 API 추가
- [x] Prisma schema에 `pc_build_parts`, `pc_compatibility_rules` 매핑 추가
- [x] 라우트 인덱스에 `pc-builds` 라우터 연결
- [x] README 노출 경로 요약 갱신
- [x] Prisma Client 재생성 완료
- [x] 대표 엔드포인트 수동 검증 완료
