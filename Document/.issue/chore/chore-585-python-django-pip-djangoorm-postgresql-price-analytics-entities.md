---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] Python Django Pip DjangoORM PostgreSQL Price Analytics Entities"
labels: chore
assignees: ""
issue: "[CHORE] Python Django Pip DjangoORM PostgreSQL Price Analytics Entities 구현"
commit: "chore: (#585) Python Django Pip DjangoORM PostgreSQL Price Analytics Entities 구현"
branch: "chore/#585/python-django-pip-djangoorm-postgresql-price-analytics-entities"
---

## 🛠️ 작업 요약

> Python Django ORM PostgreSQL 대표 구현체에 가격 분석 엔티티 `price_history`, `price_alerts`, `price_predictions`를 추가하고, `pricing` 앱 확장과 후속 마이그레이션 및 관계 테스트까지 기본 세팅한다.

## 📋 요구사항

> 수행해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `apps.pricing`에 `PriceHistory`, `PriceAlert`, `PricePrediction`을 추가한다.
- [x] `Product`, `CustomUser`와의 FK 관계를 반영한다.
- [x] enum, 복합 unique, 날짜 기반 index 제약을 Django ORM에 반영한다.
- [x] `pricing` 후속 migration을 생성한다.
- [x] `PriceHistory`, `PriceAlert`, `PricePrediction` 관계 및 제약 테스트를 추가한다.
- [x] `.\.venv\Scripts\python manage.py check`, `.\.venv\Scripts\python manage.py test`, `DJANGO_USE_SQLITE_FOR_TESTS=true .\.venv\Scripts\python manage.py makemigrations --check --dry-run` 검증을 통과한다.
