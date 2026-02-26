# TypeScript Bible

`nestshop` 실행/검증 매뉴얼입니다.

## 1. 경로

- 백엔드 앱: `BackEnd/TypeScript/nestshop`
- 공용 인프라: `Database/docker/docker-compose.yml`
- TypeScript 문서: `Document/TypeScript/`

## 2. 사전 준비

- Node.js 20+
- Docker Desktop

## 3. 실행 순서

### 3.1 인프라 기동

```bash
docker compose -f Database/docker/docker-compose.yml up -d
```

### 3.2 백엔드 실행

```bash
cd BackEnd/TypeScript/nestshop
npm ci
npm run migration:run
npm start
```

- 기본 API: `http://localhost:3000/api/v1`
- Swagger: `http://localhost:3000/docs`

### 3.3 프론트엔드 실행 (선택)

```bash
cd FrontEnd
npm ci
npm run dev
```

- 기본 UI: `http://localhost:3001`

## 4. 주요 검증 명령

```bash
cd BackEnd/TypeScript/nestshop
npx tsc -p tsconfig.json --noEmit --incremental false
npm run test:e2e:critical
```

## 5. 문서

- 운영 런북: `Document/TypeScript/02_operations-runbook.md`
- 릴리스 체크리스트: `Document/TypeScript/03_release-checklist.md`
- 완료 리포트: `Document/TypeScript/04_completion-report.md`

