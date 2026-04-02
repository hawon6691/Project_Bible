---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Python Django Pip DjangoORM PostgreSQL Point"
labels: feature
assignees: ""
issue: "[FEAT] Python Django Pip DjangoORM PostgreSQL Point 구현"
commit: "feat: (#601) Python Django Pip DjangoORM PostgreSQL Point 구현"
branch: "feat/#601/python-django-pip-djangoorm-postgresql-point"
---

## ✨ 기능 요약

> Python Django ORM PostgreSQL 대표 구현체에 포인트 잔액, 내역, 적립, 사용, 환원, 관리자 지급 기능을 구현하고 테스트를 포함한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `PNT-01 ~ PNT-06` 요구사항을 반영해 포인트 잔액 조회, 내역 조회, 자동 적립, 사용, 환원, 관리자 지급 기능을 구현한다.
- [x] 구매확정/리뷰 작성 이벤트 연계, 주문 취소 환원, 사용자/시스템/관리자 트리거를 반영한다.
- [x] 포인트 API, 서비스, 모델, 저장소와 잔액 정합성 검증을 Python 구조에 맞게 구성한다.
- [x] 적립/사용/환원/지급/내역 조회 테스트를 포함하고 `manage.py test` 기준 회귀 시나리오를 추가한다.

