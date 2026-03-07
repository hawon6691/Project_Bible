---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] 카테고리 페이지 전체 카테고리 드롭다운 UI 정렬"
labels: feature
issue: "[FEAT] 카테고리 페이지 전체 카테고리 드롭다운 UI 정렬"
commit: "feat: (#229) 카테고리 전체보기 패널을 메인 우측 확장형 드롭다운으로 변경"
branch: "feat/#229/category-all-dropdown-alignment"
assignees: ""
---

## ✨ 기능 요약

> 카테고리 페이지에서 `전체 카테고리` 클릭 시 전체 화면 오버레이 대신 메인과 동일한 우측 확장형 드롭다운 패널이 나타나도록 UI를 변경했습니다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 카테고리 상단의 `전체 카테고리` 버튼을 토글형 드롭다운 동작으로 변경 (`FrontEnd/src/app/categories/[id]/page.tsx`)
- [x] 기존 전체 화면 오버레이/모달 구조 제거 (`FrontEnd/src/app/categories/[id]/page.tsx`)
- [x] 버튼 외부 클릭 시 드롭다운 닫힘 처리 추가 (`FrontEnd/src/app/categories/[id]/page.tsx`)
- [x] 드롭다운 패널 레이아웃을 좌측(상위 카테고리) + 우측(하위 카테고리 컬럼) 구조로 구성 (`FrontEnd/src/app/categories/[id]/page.tsx`)
- [x] 드롭다운 패널 전용 스타일 추가 및 반응형 보완 (`FrontEnd/src/app/categories/[id]/category-page.css`)
- [x] FrontEnd 빌드 검증 통과 (`npm run build`)
