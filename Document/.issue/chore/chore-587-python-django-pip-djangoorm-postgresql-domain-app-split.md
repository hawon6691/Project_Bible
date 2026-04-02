---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] Python Django Pip DjangoORM PostgreSQL Domain App Split"
labels: chore
assignees: ""
issue: "[CHORE] Python Django Pip DjangoORM PostgreSQL Domain App Split 구현"
commit: "chore: (#587) Python Django Pip DjangoORM PostgreSQL Domain App Split 구현"
branch: "chore/#587/python-django-pip-djangoorm-postgresql-domain-app-split"
---

## 🛠️ 작업 요약

> Python Django ORM PostgreSQL 대표 구현체에서 `apps.commerce` 단일 앱 구조를 기능별 도메인 앱으로 재분리하고, 기존 테이블을 유지한 채 모델 소속, migration state, 테스트 구조를 정리한다.

## 📋 요구사항

> 수행해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `apps.cart`, `apps.address`, `apps.order`, `apps.payment` 앱을 추가한다.
- [x] `CartItem`, `Address`, `Order`, `OrderItem`, `Payment` 모델을 기능별 앱으로 이동한다.
- [x] `config/settings.py`에 새 앱 등록을 반영한다.
- [x] 기존 DB 테이블은 유지하고 state-only migration으로 모델 소속만 이동한다.
- [x] `apps.commerce`는 레거시 migration anchor 역할만 남기고 런타임 모델 의존성을 제거한다.
- [x] `test_commerce_entities.py`를 제거하고 `cart/address/order/payment` 기준 테스트로 재분리한다.
- [x] `.\.venv\Scripts\python manage.py check`, `.\.venv\Scripts\python manage.py test`, `DJANGO_USE_SQLITE_FOR_TESTS=true .\.venv\Scripts\python manage.py makemigrations --check --dry-run` 검증을 통과한다.
