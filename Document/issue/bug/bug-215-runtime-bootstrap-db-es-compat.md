---
name: "🐛 Bug Report"
about: 버그 신고
title: "[BUG] Nestshop 부팅 시 DB/Elasticsearch 초기화 실패 수정"
labels: bug
issue: "[BUG] Nestshop 부팅 시 DB/Elasticsearch 초기화 실패 수정"
commit: "bug: (#215) DB synchronize 불리언 파싱 및 ES 호환 헤더/부팅 보호 적용"
branch: "bug/#215/runtime-bootstrap-db-es-compat"
assignees: ""
---

## 🐛 버그 요약

> Nestshop 실행 시 TypeORM/Elasticsearch 초기화 단계에서 앱이 부팅 실패하는 문제를 수정했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `DB_SYNCHRONIZE=false` 문자열이 true로 해석되는 문제 수정 (환경변수 불리언 파싱 함수 적용)
- [x] TypeORM CLI `DataSource` 다중 export 오류 수정 (`data-source.ts` 단일 export)
- [x] 빈 DB에서 증분 마이그레이션 실패하지 않도록 `crawler_runs` 존재 가드 추가
- [x] Elasticsearch 9 클라이언트/8 서버 헤더 충돌 수정 (`compatible-with=8` 강제)
- [x] 검색 인덱스 초기화 실패 시 앱 부팅 중단되지 않도록 예외 보호 및 경고 로그 처리
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
