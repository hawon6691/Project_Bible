---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 관리자 시스템 설정 모듈 구현"
labels: feature
issue: "[FEAT] 관리자 시스템 설정 모듈 구현"
commit: "feat: (#83) 확장자/업로드제한/리뷰정책 관리 API 구현"
branch: "feat/#83/admin-settings-module"
assignees: ""
---

## ✨ 기능 요약

> 관리자 시스템 설정(허용 확장자, 업로드 용량 제한, 리뷰 정책) 조회/수정 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 시스템 설정 엔티티 구현 (`system_settings`)
- [x] 설정 DTO 구현 (허용 확장자, 업로드 제한, 리뷰 정책)
- [x] Admin Settings 모듈/서비스/컨트롤러 추가
- [x] 허용 확장자 조회 API 구현 (`GET /admin/settings/extensions`)
- [x] 허용 확장자 추가/변경 API 구현 (`POST /admin/settings/extensions`)
- [x] 업로드 용량 제한 조회 API 구현 (`GET /admin/settings/upload-limits`)
- [x] 업로드 용량 제한 변경 API 구현 (`PATCH /admin/settings/upload-limits`)
- [x] 리뷰 정책 조회 API 구현 (`GET /admin/settings/review-policy`)
- [x] 리뷰 정책 변경 API 구현 (`PATCH /admin/settings/review-policy`)
- [x] key-value(jsonb) 기반 설정 upsert/getOrCreate 로직 구현
- [x] 확장자 정규화(소문자/중복제거/정렬) 로직 구현
- [x] 앱 모듈 등록 (`AdminSettingsModule`)
- [x] API 라우트 상수 추가 (`ADMIN_SETTINGS`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
