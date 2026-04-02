---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Python Django Pip DjangoORM PostgreSQL Category"
labels: feature
assignees: ""
issue: "[FEAT] Python Django Pip DjangoORM PostgreSQL Category 구현"
commit: "feat: (#590) Python Django Pip DjangoORM PostgreSQL Category 구현"
branch: "feat/#590/python-django-pip-djangoorm-postgresql-category"
---

## ✨ 기능 요약

> Python Django ORM PostgreSQL 대표 구현체에 계층형 카테고리 조회 및 관리자 카테고리 관리 기능을 구현하고 테스트를 포함한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `CAT-01 ~ CAT-04` 요구사항을 반영해 계층형 카테고리 목록 조회와 생성, 수정, 삭제 기능을 구현한다.
- [x] 부모-자식 관계, 정렬순서, 하위 카테고리 존재 시 삭제 제한, 관리자 권한 검증을 반영한다.
- [x] 카테고리 API, 서비스, 도메인 모델, 저장소 계층을 Python 구조에 맞게 구성한다.
- [x] 트리 조회, 수정, 삭제 제한, 권한 경계 테스트를 포함하고 `manage.py test` 기준 회귀 시나리오를 추가한다.

