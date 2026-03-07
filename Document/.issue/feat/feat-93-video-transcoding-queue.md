---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Video Transcoding Queue 모듈 구현"
labels: feature
issue: "[FEAT] Video Transcoding Queue 모듈 구현"
commit: "feat: (#93) 숏폼 트랜스코딩 Bull Queue 처리 및 상태 API 구현"
branch: "feat/#93/video-transcoding-queue"
assignees: ""
---

## ✨ 기능 요약

> 숏폼 업로드 후 비디오 트랜스코딩을 Bull Queue 워커로 비동기 처리하고, 상태 조회/재시도 API를 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 숏폼 트랜스코딩 상태 enum 추가 (`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`)
- [x] `shortforms` 엔티티에 트랜스코딩 상태/결과 컬럼 추가
- [x] `video-transcode` Bull Queue 등록
- [x] 트랜스코딩 워커 프로세서 추가 (`VideoProcessor`)
- [x] 숏폼 업로드 시 트랜스코딩 잡 큐 등록 로직 추가
- [x] 트랜스코딩 상태 조회 API 구현 (`GET /shortforms/:id/transcode-status`)
- [x] 트랜스코딩 재시도 API 구현 (`POST /shortforms/:id/transcode/retry`)
- [x] 숏폼 응답에 트랜스코딩 상태 필드 포함
- [x] 앱 레벨 Bull Redis 설정 추가 (`BullModule.forRootAsync`)
- [x] API 라우트 상수 확장 (`SHORTFORM.TRANSCODE_STATUS`, `SHORTFORM.TRANSCODE_RETRY`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
