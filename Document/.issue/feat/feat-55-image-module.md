---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 이미지 최적화 모듈 구현"
labels: feature
issue: "[FEAT] 이미지 최적화 모듈 구현"
commit: "feat: (#55) 이미지 업로드/변환본조회/삭제 API 구현"
branch: "feat/#55/image-module"
assignees: ""
---

## ✨ 기능 요약

> 이미지 업로드 시 원본/변환본 메타데이터를 생성하고, 변환본 조회 및 관리자 삭제 기능을 제공하는 Image 모듈을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 이미지 원본 엔티티 구현 (`image_assets`)
- [x] 이미지 변환본 엔티티 구현 (`image_variants`)
- [x] 업로드 DTO 구현 (`UploadImageDto`)
- [x] Image 모듈/서비스/컨트롤러 추가
- [x] 이미지 업로드 API 구현 (`POST /images/upload`)
- [x] 이미지 변환본 조회 API 구현 (`GET /images/:id/variants`)
- [x] 관리자 이미지 삭제 API 구현 (`DELETE /images/:id`)
- [x] 허용 MIME 타입 및 최대 용량(10MB) 검증 로직 구현
- [x] 기본 변환본(THUMBNAIL/MEDIUM/LARGE) 메타데이터 생성 로직 구현
- [x] 앱 모듈 등록 (`ImageModule`)
- [x] API 라우트 상수 추가 (`IMAGE`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
