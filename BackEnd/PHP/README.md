# PHP Bible

`laravelshop` 실행/검증 매뉴얼입니다.

## 1. 경로

- 백엔드 앱: `BackEnd/PHP/laravelshop`
- 공용 인프라: `Database/docker/docker-compose.yml`
- PHP 문서: `Document/PHP/`

## 2. 사전 준비

- PHP 8.3+
- Composer
- Docker Desktop

## 3. 실행 순서

### 3.1 인프라 기동

```bash
docker compose -f Database/docker/docker-compose.yml up -d
```

### 3.2 백엔드 실행

```bash
cd BackEnd/PHP/laravelshop
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate --force
php artisan serve --host=127.0.0.1 --port=8000
```

- 기본 API: `http://localhost:8000/api/v1`
- Scribe 문서: `http://localhost:8000/docs/api`
- Swagger UI: `http://localhost:8000/docs/swagger`
- OpenAPI 문서: `http://localhost:8000/docs/api.openapi`

### 3.3 프론트엔드 실행 (선택)

```bash
cd FrontEnd
npm ci
npm run dev
```

- 기본 UI: `http://localhost:3000`

## 4. 주요 검증 명령

```bash
cd BackEnd/PHP/laravelshop
vendor/bin/pint --test
php artisan test
php artisan test tests/E2E
```

## 5. CI / 수동 검증 대응 명령

```bash
cd BackEnd/PHP/laravelshop
php tests/scripts/validate-migrations.php
MIGRATION_ROUNDTRIP_ALLOW=true php tests/scripts/migration-roundtrip.php
php tests/scripts/live-smoke.php
php tests/scripts/analyze-stability.php
```

성능 자산:

```bash
php -S 127.0.0.1:3310 tests/performance/mock-perf-server.php
```

## 6. 문서

- 폴더 구조: `Document/PHP/01_folder-structure.md`
- 운영 런북: `Document/PHP/02_operations-runbook.md`
- 구현 상태: `Document/PHP/03_implementation-status.md`
- 완료 리포트: `Document/PHP/04_completion-report.md`
- 최종 게이트: `Document/PHP/05_pre-release-final-gate.md`

