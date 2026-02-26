---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] 프로젝트 루트 README 1차 정비"
labels: document
issue: "[DOCS] 프로젝트 루트 README 1차 정비"
commit: "docs: (#219) README 역할 분리 및 문서 체계 재정렬"
branch: "docs/#219/project-readme-bootstrap"
assignees: ""
---

## 📌 관련 이슈

> 이 PR과 연관된 이슈 번호를 작성해주세요.

- #219

---

## 🧾 문서 요약

> 어떤 문서인지 한 줄로 설명해주세요.

루트 README를 표지형으로 정리하고, TypeScript README에 실행 매뉴얼을 분리했으며, 문서 체계를 공용/언어별로 재배치했습니다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

- 구조 개편(`BackEnd`, `Database`, `FrontEnd`, `Document`) 이후 진입 가이드가 필요함
- 루트 README와 언어별 README의 역할을 분리해 문서 책임을 명확히 해야 함
- 공용 문서와 TypeScript 전용 문서를 분리해 다중 백엔드 확장 시 혼선을 줄여야 함

---

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 루트 README를 표지 문서 형태로 정리 (실행 절차 제거)
- [x] TypeScript README에 실행 매뉴얼 이관 (`BackEnd/TypeScript/README.md`)
- [x] Document 공용/언어별 문서 구조 분리 (`Document/<Language>/`)
- [x] TypeScript 전용 문서 및 이슈 폴더 이동 (`Document/TypeScript/*`)
- [x] 공용 문서 범위 확정 (`01_requirements`, `02_api-specification`, `03_erd`)
- [x] TypeScript 문서 번호 체계 재정렬 (`01~05`)
- [x] 문서 내 경로/파일명 참조 일괄 갱신 (README 포함)
