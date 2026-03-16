---
name: "🐛 Bug Report"
about: 버그 신고
title: "[BUG] JavaScript Express Prisma Category Delete Guard"
labels: bug
issue: "[BUG] JavaScript Express Prisma Category Delete Guard 수정"
commit: "fix: (#453) JavaScript Express Prisma Category Delete Guard 수정"
branch: "bug/#453/javascript-express-prisma-category-delete-guard"
assignees: ""
---

## 🐛 버그 요약

> soft delete 된 product 가 연결된 category 삭제 시 FK 예외로 `500` 이 발생하는 문제를 수정합니다.

## 🔍 재현 방법

> 버그를 재현하는 순서를 작성해주세요.

1. 관리자 권한으로 category 를 생성합니다.
2. 생성한 category 에 product 를 연결해 생성합니다.
3. product 를 soft delete 한 뒤 `DELETE /categories/:id` 를 호출합니다.
4. `products_category_id_fkey` 위반으로 `500` 이 발생합니다.

## 🤔 예상 동작

> 원래 어떻게 동작해야 하나요?

`DELETE /categories/:id` 는 하위 category 또는 연결된 product 존재 여부를 먼저 검사하고, 삭제 불가 조건이면 명시적인 business error(`400`)를 반환해야 합니다.

## 😱 실제 동작

> 실제로는 어떻게 동작하고 있나요?

삭제 전 가드가 없어 category 실삭제가 바로 실행되고, soft delete 상태의 product 레코드가 FK 를 유지하고 있어 `products_category_id_fkey` 위반으로 `500` 이 발생합니다.

## 🌍 환경 정보

> 버그가 발생한 환경을 작성해주세요.

- OS: Windows
- Node.js 버전: (로컬 개발 환경)
- 브랜치: `bug/#453/javascript-express-prisma-category-delete-guard`

## 💡 원인 추정

> 원인이 무엇인지 추측이 있다면 작성해주세요.

- category 삭제가 하위 category 존재 여부만 아니라 연결된 product 존재 여부도 확인해야 하는데, 현재 가드가 없습니다.
- product 는 soft delete(`deleted_at`)를 사용하지만 category 는 실삭제라서 FK 제약이 그대로 남습니다.
- Prisma 삭제 시점의 FK 예외가 비즈니스 오류로 변환되지 않아 `500` 으로 노출됩니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `categories` repository 에 하위 category 존재 여부 조회 추가
- [x] `categories` repository 에 연결 product 존재 여부 조회 추가
- [x] `DELETE /categories/:id` 에 child category 가드 추가
- [x] `DELETE /categories/:id` 에 linked product 가드 추가
- [x] FK 예외가 발생해도 `500` 대신 business error 로 변환되도록 보강
- [x] bug 문서에 재현 절차와 완료 조건 기록

## ✅ 완료 조건

> 이 버그가 해결됐다고 판단하는 기준을 작성해주세요.

- [x] 빈 category 삭제 시 `DELETE /categories/:id` 가 정상 성공한다.
- [x] child category 가 있는 parent 삭제 시 `400 BAD_REQUEST` 를 반환한다.
- [x] active product 가 연결된 category 삭제 시 `400 BAD_REQUEST` 를 반환한다.
- [x] soft delete 된 product 가 연결된 category 삭제 시 `400 BAD_REQUEST` 를 반환한다.
