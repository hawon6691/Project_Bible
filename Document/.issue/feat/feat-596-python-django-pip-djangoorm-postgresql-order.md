---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Python Django Pip DjangoORM PostgreSQL Order"
labels: feature
assignees: ""
issue: "[FEAT] Python Django Pip DjangoORM PostgreSQL Order 구현"
commit: "feat: (#596) Python Django Pip DjangoORM PostgreSQL Order 구현"
branch: "feat/#596/python-django-pip-djangoorm-postgresql-order"
---

## ✨ 기능 요약

> Python Django ORM PostgreSQL 대표 구현체에 주문 생성, 조회, 취소, 관리자 주문 관리 기능을 구현하고 테스트를 포함한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `ORD-01 ~ ORD-06` 요구사항을 반영해 주문 생성, 내 주문 목록, 상세 조회, 취소, 전체 주문 관리, 주문 상태 변경 기능을 구현한다.
- [x] 장바구니/바로구매 진입, 재고 차감, 포인트 사용/환원, 관리자 상태 전이, 주문 목록 필터/페이징을 반영한다.
- [x] 주문 API, 서비스, 모델, 저장소와 트랜잭션 처리 및 권한 검증을 Python 구조에 맞게 구성한다.
- [x] 주문 생성/취소/상태 변경/관리자 조회 테스트를 포함하고 `manage.py test` 기준 회귀 시나리오를 추가한다.

