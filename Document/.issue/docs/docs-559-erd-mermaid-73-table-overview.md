---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] ERD Mermaid 73 Table Overview"
labels: document
assignees: ""
issue: "[DOCS] ERD Mermaid 73 Table Overview 문서 작성"
commit: "docs: (#559) ERD Mermaid 73 Table Overview 문서 작성"
branch: "docs/#559/erd-mermaid-73-table-overview"
---

## 🧾 문서 요약

> 어떤 문서인지 한 줄로 설명해주세요.

`03_erd.md` 상단 Mermaid 개요도를 핵심 일부 테이블 요약이 아니라 전체 73개 테이블이 모두 보이는 도메인별 개요도로 확장해, 문서 첫 화면에서도 전체 스키마 범위를 바로 파악할 수 있도록 정리한다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

기존 상단 Mermaid 개요도는 전체 스키마의 핵심 흐름만 보여주는 요약판이라, 73개 전체 테이블을 한 번에 보고 싶은 경우에는 아래 상세 ERD까지 내려가야 했다.

개요도 설명 문구도 `전체 73개 테이블 중 핵심 흐름`으로 적혀 있어, 전체 테이블이 모두 표현된 것처럼 읽힐 수 있는 여지가 있었다.

상단 개요도 자체를 73개 전체 테이블 기준으로 확장해, `overview = 전체 구조 빠른 파악`, `detailed = PK/FK 중심 정확한 참조` 역할이 더 명확하게 구분되도록 한다.

## 📋 요구사항

> 수행해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `03_erd.md`의 `## 0. Mermaid 개요도`를 전체 73개 테이블이 모두 포함된 도메인별 Mermaid `flowchart`로 확장한다.
- [x] 개요도 설명 문구를 핵심 일부 테이블 요약이 아니라 전체 73개 테이블 개요도라는 의미에 맞게 수정한다.
- [x] 개요도는 도메인별 `subgraph` 구조를 유지하면서 계정, 상품, 주문, 커뮤니티, 추천 계열 테이블을 모두 포함하도록 정리한다.
- [x] 기존 `## 0.1 Mermaid 상세 ERD`와 이후 테이블 목록, 텍스트 관계도, ASCII 다이어그램 구조는 유지한다.
- [x] 수정 후 상단 Mermaid 개요도 안의 테이블 라벨 수가 실제로 73개인지 확인하고, 상세 ERD와 누락 없이 대응되도록 정리한다.
