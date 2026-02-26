# Project_Bible

멀티 백엔드(언어별) + 공통 데이터베이스 + 단일 프론트엔드로 구성된 쇼핑몰 실전 아키텍처 프로젝트입니다.

## 1. 이 프로젝트를 하는 이유

- 동일한 도메인(쇼핑몰)을 여러 백엔드 언어로 구현해 설계/운영 관점의 차이를 비교하기 위함
- 프론트엔드와 API 계약을 분리해, 백엔드를 교체해도 UI 테스트가 가능하도록 만들기 위함
- 문서/테스트/운영 절차를 코드와 함께 관리하는 실무형 프로젝트 운영 연습을 위함

## 2. 핵심 목표

- `BackEnd/*` 각 언어 구현체가 같은 기능 요구사항과 API 계약을 만족
- `Database/*`에서 DB 자산(DDL/seed/infra)을 일관되게 관리
- `FrontEnd/`에서 백엔드 전환 가능한 검증 UI 제공
- `Document/`에서 요구사항, API 명세, 운영 런북, 릴리스 체크리스트를 지속 관리

## 3. 현재 디렉토리 구조 (1차)

```text
Project_Bible/
├─ BackEnd/
│  ├─ TypeScript/nestshop
│  ├─ Java/springshop
│  ├─ JavaScript/expressshop
│  ├─ Python/djangoshop
│  ├─ Kotlin/ktorshop
│  └─ PHP/laravelshop
├─ Database/
│  ├─ docker/nestshop-docker-compose.yml
│  ├─ postgresql/
│  │  ├─ setting.sql
│  │  ├─ postgres_table.sql
│  │  └─ sample_data.sql
│  ├─ mysql/
│  └─ mongodb/
├─ FrontEnd/
└─ Document/
```

## 4. 운영 원칙

- 코드 변경 시 관련 문서(`Document`)와 이슈 마크다운(`Document/TypeScript/issue/*`) 동시 갱신
- 기능/버그/리팩터/테스트/문서를 이슈 폴더 단위로 분리 기록
- 백엔드별 구현 차이는 허용하되 API 계약 일관성은 유지

## 5. 실행 매뉴얼 위치

- TypeScript 백엔드: `BackEnd/TypeScript/README.md`
- 언어별 백엔드: `BackEnd/<Language>/README.md` (각 폴더에 순차 정리)
- 프론트엔드: `FrontEnd` 내부 README/가이드

## 6. 공용 문서 인덱스

- 요구사항: `Document/01_requirements.md`
- API 명세: `Document/02_api-specification.md`
- ERD: `Document/03_erd.md`

## 7. TypeScript 전용 문서

- 폴더 구조: `Document/TypeScript/01_folder-structure.md`
- 운영 런북: `Document/TypeScript/02_operations-runbook.md`
- 릴리스 체크리스트: `Document/TypeScript/03_release-checklist.md`
- 완료 리포트: `Document/TypeScript/04_completion-report.md`
- 배포 전 최종 게이트: `Document/TypeScript/05_pre-release-final-gate.md`
- 이슈 기록: `Document/TypeScript/issue/`

