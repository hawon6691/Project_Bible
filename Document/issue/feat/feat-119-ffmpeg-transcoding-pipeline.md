---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 숏폼 FFmpeg 실트랜스코딩 파이프라인 구현"
labels: feature
issue: "[FEAT] 숏폼 FFmpeg 실트랜스코딩 파이프라인 구현"
commit: "feat: (#119) video queue ffmpeg 실트랜스코딩 처리 적용"
branch: "feat/#119/ffmpeg-transcoding-pipeline"
assignees: ""
---

## ✨ 기능 요약

> 숏폼 업로드 후 Bull 워커에서 FFmpeg로 실제 mp4 트랜스코딩과 썸네일 생성을 수행하도록 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 숏폼 업로드를 디스크 저장 방식으로 변경 (`uploads/shortforms/raw`)
- [x] 업로드 파일 타입/용량 필터 추가 (mp4/webm/mov, 최대 200MB)
- [x] 숏폼 저장 시 원본 raw 경로/썸네일 경로 저장 로직 반영
- [x] `video-transcode` 워커에서 FFmpeg 실제 실행 로직 구현
- [x] 트랜스코딩 결과 mp4 저장 경로 반영 (`uploads/shortforms/transcoded`)
- [x] 트랜스코딩 완료 후 썸네일 jpg 생성 로직 구현 (`uploads/shortforms/thumb`)
- [x] 워커 실패 시 상태 `FAILED` 및 에러 메시지 저장 로직 유지
- [x] 완료 시 `transcodedVideoUrl`, `thumbnailUrl`, `transcodedAt` 갱신
- [x] FFmpeg 실행 환경 변수 추가 (`FFMPEG_BIN`, `FFMPEG_PRESET`, `FFMPEG_CRF`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
