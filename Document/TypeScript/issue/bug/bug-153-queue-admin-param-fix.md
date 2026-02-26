---
name: "🐛 Bug Report"
about: 버그 신고
title: "[BUG] Queue Admin 파라미터 파싱 오류 수정"
labels: bug
issue: "[BUG] Queue Admin 파라미터 파싱 오류 수정"
commit: "bug: (#153) queue-admin newestFirst/jobId 파라미터 처리 버그 수정"
branch: "bug/#153/queue-admin-param-fix"
assignees: " "
---

## 🐛 버그 요약

> Queue Admin API에서 `newestFirst=false`가 `true`로 처리되고, 문자열 Job ID가 라우팅에서 거부되는 문제를 수정했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `FailedJobsQueryDto` 불리언 파싱 로직 보강 (`newestFirst=false` 정상 처리)
- [x] Queue Admin 컨트롤러에서 문자열 Job ID 허용 (`ParseIntPipe` 제거)
- [x] 실패 잡 목록 E2E에 `newestFirst=false` 검증 추가
- [x] Job 삭제 E2E에 문자열 Job ID 시나리오 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
