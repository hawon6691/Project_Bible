---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] Python Django Pip DjangoORM PostgreSQL Price Core Entities"
labels: chore
assignees: ""
issue: "[CHORE] Python Django Pip DjangoORM PostgreSQL Price Core Entities 구현"
commit: "chore: (#583) Python Django Pip DjangoORM PostgreSQL Price Core Entities 구현"
branch: "chore/#583/python-django-pip-djangoorm-postgresql-price-core-entities"
---

## 🛠️ 작업 요약

> Python Django ORM PostgreSQL 대표 구현체에 가격비교 핵심 엔티티 `sellers`, `spec_definitions`, `product_specs`, `price_entries`를 추가하고, `catalog`/`pricing` 앱 분리와 초기 마이그레이션 및 관계 테스트까지 기본 세팅한다.

## 📋 요구사항

> 수행해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `apps.catalog`에 `SpecDefinition`, `ProductSpec`를 추가한다.
- [x] `apps.pricing` 앱을 추가하고 `Seller`, `PriceEntry`를 구성한다.
- [x] `config/settings.py`에 `apps.pricing` 등록을 반영한다.
- [x] enum, JSONField, 복합 unique, FK, index 제약을 Django ORM에 반영한다.
- [x] `price_entries.total_price`를 `price + shipping_fee` 기반 `GeneratedField`로 반영한다.
- [x] `catalog` 후속 migration과 `pricing` 초기 migration을 생성한다.
- [x] `SpecDefinition`, `ProductSpec`, `Seller`, `PriceEntry` 관계 및 제약 테스트를 추가한다.
- [x] `.\.venv\Scripts\python manage.py check`, `.\.venv\Scripts\python manage.py test`, `DJANGO_USE_SQLITE_FOR_TESTS=true .\.venv\Scripts\python manage.py makemigrations --check --dry-run` 검증을 통과한다.
