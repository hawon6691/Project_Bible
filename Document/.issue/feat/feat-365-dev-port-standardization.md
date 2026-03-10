---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 프론트 3000 / 백엔드 8000 개발 포트 표준화"
labels: feature
issue: "[FEAT] 프론트 3000 / 백엔드 8000 개발 포트 표준화"
commit: "feat: (#365) 프론트/백엔드 개발 포트 기본값을 3000/8000으로 표준화"
branch: "feat/#365/dev-port-standardization"
assignees: ""
---

## ✨ 기능 요약

> 프론트엔드 기본 포트를 `3000`, 백엔드 기본 포트를 `8000`으로 통일하고, 프론트 기본 API URL, 백엔드 콜백 URL, 성능 테스트 기본 URL, 예제 환경 파일, 실행 문서까지 동일 기준으로 정리했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 프론트엔드 개발 서버 기본 포트를 `3000`으로 변경 (`FrontEnd/vite.config.ts`, `FrontEnd/package.json`)
- [x] 프론트엔드 기본 API URL을 `http://localhost:8000/api/v1`로 변경 (`FrontEnd/src/lib/config.ts`, `FrontEnd/.env.example`)
- [x] TypeScript 백엔드 기본 포트를 `8000`으로 변경 (`BackEnd/TypeScript/nestshop/src/main.ts`)
- [x] TypeScript 소셜 로그인 콜백 기본 URL을 `8000` 기준으로 변경 (`BackEnd/TypeScript/nestshop/src/config/social.config.ts`, `BackEnd/TypeScript/nestshop/src/auth/auth.service.ts`)
- [x] PHP 설정의 프론트 기본 URL을 `http://localhost:3000`으로 변경 (`BackEnd/PHP/laravelshop/config/pbshop.php`)
- [x] TypeScript/PHP 실행 문서의 기본 포트와 접속 URL을 최신 기준으로 정리 (`FrontEnd/README.md`, `BackEnd/TypeScript/README.md`, `BackEnd/PHP/README.md`)
- [x] TypeScript 성능 테스트 기본 `BASE_URL`을 `8000` 기준으로 변경 (`BackEnd/TypeScript/nestshop/test/performance/*.js`, `README.md`)
- [x] 백엔드 예제 환경 파일의 기본 포트/URL을 최신 기준으로 정리 (`BackEnd/TypeScript/nestshop/.env.example`, `BackEnd/PHP/laravelshop/.env.example`)
- [x] 실제 실행용 `.env`는 저장소에 없음을 확인하고, 예제 파일 기준으로만 기본값을 정리

