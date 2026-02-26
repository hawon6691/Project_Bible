---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 뉴스 모듈 구현"
labels: feature
issue: "[FEAT] 뉴스 모듈 구현"
commit: "feat: (#67) 뉴스/카테고리 조회 및 관리자 CRUD API 구현"
branch: "feat/#67/news-module"
assignees: ""
---

## ✨ 기능 요약

> 뉴스 목록/상세/카테고리 조회와 관리자 뉴스 및 카테고리 CRUD 기능을 구현했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 뉴스 카테고리 엔티티 구현 (`news_categories`)
- [x] 뉴스 엔티티 구현 (`news`)
- [x] 뉴스-상품 매핑 엔티티 구현 (`news_products`)
- [x] News DTO 구현 (목록 쿼리, 생성/수정, 카테고리 생성)
- [x] News 모듈/서비스/컨트롤러 추가
- [x] 뉴스 목록 조회 API 구현 (`GET /news`)
- [x] 뉴스 카테고리 목록 조회 API 구현 (`GET /news/categories`)
- [x] 뉴스 상세 조회 API 구현 (`GET /news/:id`) 및 조회수 증가
- [x] 관리자 뉴스 작성 API 구현 (`POST /news`)
- [x] 관리자 뉴스 수정 API 구현 (`PATCH /news/:id`)
- [x] 관리자 뉴스 삭제 API 구현 (`DELETE /news/:id`)
- [x] 관리자 카테고리 생성 API 구현 (`POST /news/categories`)
- [x] 관리자 카테고리 삭제 API 구현 (`DELETE /news/categories/:id`)
- [x] 뉴스 작성/수정 시 카테고리 및 연관 상품 유효성 검증 로직 구현
- [x] 뉴스-상품 매핑 동기화 로직 구현
- [x] 앱 모듈 등록 (`NewsModule`)
- [x] API 라우트 상수 추가 (`NEWS`)
- [x] 주요 코드 주석 추가
- [x] 타입 체크 통과 (`npx tsc -p tsconfig.json --noEmit --incremental false`)
