---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 멀티미디어 리소스 모듈 구현"
labels: feature
issue: "[FEAT] 멀티미디어 리소스 모듈 구현"
commit: "feat: (#65) 미디어 업로드/presigned-url/스트림/메타데이터/삭제 API 구현"
branch: "feat/#65/media-module"
assignees: ""
---

## ✨ 기능 요약

> 파일 업로드, Presigned URL 발급, 스트림 정보 조회, 메타데이터 조회, 소유자 삭제 기능을 제공하는 Media 모듈을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 미디어 엔티티 구현 (`media_assets`)
- [x] Media DTO 구현 (업로드, presigned-url)
- [x] Media 모듈/서비스/컨트롤러 추가
- [x] 파일 업로드 API 구현 (`POST /media/upload`)
- [x] Presigned URL 발급 API 구현 (`POST /media/presigned-url`)
- [x] 파일 스트리밍 정보 조회 API 구현 (`GET /media/stream/:id`)
- [x] 파일 메타데이터 조회 API 구현 (`GET /media/:id/metadata`)
- [x] 파일 삭제 API 구현 (`DELETE /media/:id`)
- [x] 미디어 타입 자동 판별(이미지/영상/음원/문서) 로직 구현
- [x] 파일 최대 용량(500MB) 검증 로직 구현
- [x] 업로더 소유권 검증 삭제 로직 구현
- [x] 앱 모듈 등록 (`MediaModule`)
- [x] API 라우트 상수 추가 (`MEDIA`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
