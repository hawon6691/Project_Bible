# PHP Pre-Release Final Gate

## 1. 목적

릴리즈 또는 다음 언어 트랙 전환 전에 PHP 구현체가 기준 상태를 만족하는지 확인한다.

## 2. 게이트 체크리스트

### 문서 정합성

- [x] `01_requirements.md`와 구현 범위 정렬
- [x] `02_api-specification.md`와 API 경로 정렬
- [x] `03_erd.md`와 migration 기준 정렬
- [x] PHP 전용 문서 세트 작성

### 애플리케이션 구조

- [x] 모듈 단위 구조 유지
- [x] API v1 라우트 분리
- [x] 공통 응답 포맷 적용
- [x] 공통 예외 처리 적용

### 데이터베이스

- [x] migration 생성 및 적용 가능
- [x] MySQL 기준 연결 확인
- [x] 핵심 도메인 테이블 반영

### 품질 검증

- [x] `php artisan route:list`
- [x] `php artisan test`
- [x] `vendor/bin/pint`
- [x] GitHub Actions workflow 추가
- [x] `php artisan l5-swagger:generate`
- [x] `storage/api-docs/api-docs.json` 생성 확인
- [x] `storage/api-docs/api-docs.yaml` 생성 확인

### 운영 체크

- [x] Health API 존재
- [x] 운영 계열 API 구현
- [x] CI에서 route/test/style 검사 가능

## 3. 오픈 이슈

- 소켓 기반 실시간 채팅은 PHP에서 별도 확장 여부를 결정해야 한다.
- 운영 환경 배포 스크립트는 아직 문서화 중심이며 자동화 수준은 추가 여지가 있다.

## 4. 릴리즈 판단

현재 PHP 구현은 다음 조건을 만족한다.

- 문서 기준 공개 API 구현 완료
- 운영 API 구현 완료
- 테스트 및 스타일 기준 확보
- CI 등록 완료
- Swagger / OpenAPI 생성 가능

따라서 PHP 트랙은 `기준 구현 완료` 상태로 판단한다.
