# JavaScript Bible

`javascript-express-npm-prisma-postgresql` 실행/검증 매뉴얼입니다.

## 1. 경로

- 백엔드 앱: `BackEnd/JavaScript/javascript-express-npm-prisma-postgresql`
- JavaScript 문서: `Document/JavaScript/`

## 2. 사전 준비

- Node.js 20+
- PostgreSQL

## 3. 실행 순서

```bash
cd BackEnd/JavaScript/javascript-express-npm-prisma-postgresql
npm install
cp .env.example .env
npm run prisma:generate
npm run prisma:migrate:dev
npm run dev
```

## 4. 기본 확인 경로

- Health: `http://localhost:8000/health`
- API Health: `http://localhost:8000/api/v1/health`
- Categories: `http://localhost:8000/api/v1/categories`
- Products: `http://localhost:8000/api/v1/products`
