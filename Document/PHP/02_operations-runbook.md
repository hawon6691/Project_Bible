# PHP Operations Runbook

## 1. 개요

이 문서는 `BackEnd/PHP/php-laravel-composer-eloquent-postgresql`의 로컬 실행, 테스트, CI 확인 절차를 정리한다.

## 2. 환경 전제

- PHP 8.3
- Composer
- MySQL 또는 MariaDB
- 프로젝트 루트: `BackEnd/PHP/php-laravel-composer-eloquent-postgresql`

## 3. 설치

```bash
composer install
cp .env.example .env
php artisan key:generate
```

## 4. 환경 변수 핵심값

```env
APP_NAME=PBShop
APP_URL=http://localhost:8000

DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=pbdb
DB_USERNAME=project_bible
DB_PASSWORD=project_bible
```

## 5. 마이그레이션

```bash
php artisan migrate
```

초기화 후 재적용:

```bash
php artisan migrate:fresh --force
```

## 6. 개발 서버 실행

```bash
php artisan serve --host=127.0.0.1 --port=8000
```

프론트 연동 시 프론트의 API base URL을 PHP 백엔드 포트로 맞춘다.

## 7. 라우트 확인

```bash
php artisan route:list
php artisan route:list --path=api/v1
```

## 8. 테스트

전체 테스트:

```bash
php artisan test
```

특정 테스트:

```bash
php artisan test tests/Feature/Api/AuthApiTest.php
```

## 9. 코드 스타일

자동 정리:

```bash
vendor/bin/pint
```

검사 전용:

```bash
vendor/bin/pint --test
```

## 10. CI

GitHub Actions:

- `.github/workflows/php-laravel-composer-eloquent-postgresql-ci.yml`

검사 단계:

1. Composer install
2. `.env` 준비
3. `php artisan key:generate --force`
4. `php artisan route:list`
5. `vendor/bin/pint --test`
6. `php artisan test`

## 11. Swagger / OpenAPI 생성

Swagger 문서 생성 명령:

```bash
php artisan l5-swagger:generate
```

생성 산출물:

```text
BackEnd/PHP/php-laravel-composer-eloquent-postgresql/storage/api-docs/api-docs.json
BackEnd/PHP/php-laravel-composer-eloquent-postgresql/storage/api-docs/api-docs.yaml
```

문서 접근 경로:

```text
/docs/swagger
/docs/openapi
```

## 12. 장애 대응 체크

### DB 연결 실패

- `.env`의 `DB_*` 값 확인
- MySQL/MariaDB 컨테이너 기동 여부 확인
- `project_bible` 계정과 `pbdb` DB 존재 여부 확인

### route:list 실패

- 최근 추가한 라우트 파일 include 여부 확인
- 컨트롤러 namespace/클래스 오타 확인

### pint 실패

- 로컬에서 `vendor/bin/pint` 실행 후 재검사

### test 실패

- 최근 migration 누락 여부 확인
- 테스트 시드/팩토리 사용 여부 확인
- 권한 미들웨어, 인증 토큰 처리 확인

### swagger generate 실패

- `config/l5-swagger.php`의 OpenAPI 버전 기본값 확인
- `config/scribe.php`의 패키지 참조 설정 확인
- `php artisan package:discover` 재실행
- `php C:\ProgramData\ComposerSetup\bin\composer install`로 문서 패키지 복구

## 13. 운영 원칙

- API 계약은 `Document/02_api-specification.md`를 우선 기준으로 삼는다.
- 스키마는 `Document/03_erd.md`와 실제 migration을 함께 관리한다.
- 새 기능 추가 시 구현, 테스트, 이슈 문서를 같은 단계에서 정리한다.
