---
name: "🛠️ Chore Request"
about: 유지보수 및 운영 작업
title: "[CHORE] Python Django Pip DjangoORM PostgreSQL Core Entity Setup"
labels: chore
assignees: ""
issue: "[CHORE] Python Django Pip DjangoORM PostgreSQL Core Entity Setup 구현"
commit: "chore: (#581) Python Django Pip DjangoORM PostgreSQL Core Entity Setup 구현"
branch: "chore/#581/python-django-pip-djangoorm-postgresql-core-entity-setup"
---

## 🛠️ 작업 요약

> Python Django ORM PostgreSQL 대표 구현체에 PBShop 핵심 엔티티 `users`, `categories`, `products`를 추가하고, 커스텀 사용자 모델과 초기 마이그레이션까지 기본 세팅한다.

## 📋 요구사항

> 수행해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `apps.users`, `apps.catalog` 도메인 앱을 추가한다.
- [x] `CustomUser`를 Django 기본 User 확장이 아닌 PBShop 전용 인증 모델로 추가한다.
- [x] `AUTH_USER_MODEL` 설정과 앱 등록을 반영한다.
- [x] `Category`, `Product` 모델을 ERD 핵심 컬럼 기준으로 추가한다.
- [x] enum, unique, self-reference, FK, index 제약을 Django ORM에 반영한다.
- [x] 초기 migration(`users`, `categories`, `products`)을 생성한다.
- [x] 테스트용 SQLite in-memory DB 경로를 추가해 엔티티 테스트가 독립적으로 동작하도록 정리한다.
- [x] custom user, unique 제약, category parent, product FK/기본값 테스트를 추가한다.
- [x] `.\.venv\Scripts\python manage.py check`, `.\.venv\Scripts\python manage.py test`, `DJANGO_USE_SQLITE_FOR_TESTS=true .\.venv\Scripts\python manage.py makemigrations --check --dry-run` 검증을 통과한다.
