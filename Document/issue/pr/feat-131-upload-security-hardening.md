---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 업로드 보안 강화 (Magic Number + 확장자 화이트리스트)"
labels: feature
issue: "[FEAT] 업로드 보안 강화 (Magic Number + 확장자 화이트리스트)"
commit: "feat: (#131) upload security service 추가 및 image media video 보안 검증 적용"
branch: "feat/#131/upload-security-hardening"
assignees: ""
---

## ✨ 기능 요약

> 업로드 파일에 대해 확장자 화이트리스트와 Magic Number를 함께 검증하여 MIME 위조 업로드를 차단하도록 강화했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 업로드 보안 모듈 추가 (`UploadSecurityModule`)
- [x] 공통 업로드 보안 서비스 추가 (`UploadSecurityService`)
- [x] DB 설정(`allowed_extensions`) 기반 확장자 화이트리스트 검증 적용
- [x] 화이트리스트 조회값 Redis 캐시 적용 (`settings:allowed_extensions`)
- [x] Admin 설정 변경 시 캐시 무효화 연동 (`setAllowedExtensions`)
- [x] Magic Number 파일 시그니처 검증 로직 구현 (jpg/png/gif/webp/mp4/mov/mp3/wav/pdf)
- [x] `image` 업로드 검증에 보안 서비스 연동
- [x] `media` 업로드 검증에 보안 서비스 연동
- [x] `video` 업로드 검증에 보안 서비스 연동
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
