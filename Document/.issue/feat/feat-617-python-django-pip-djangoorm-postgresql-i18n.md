---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Python Django Pip DjangoORM PostgreSQL I18n"
labels: feature
assignees: ""
issue: "[FEAT] Python Django Pip DjangoORM PostgreSQL I18n 구현"
commit: "feat: (#617) Python Django Pip DjangoORM PostgreSQL I18n 구현"
branch: "feat/#617/python-django-pip-djangoorm-postgresql-i18n"
---

## ✨ 기능 요약

> Python Django ORM PostgreSQL 대표 구현체에 다국어 응답, 다화폐 표시, 환율 갱신, 사용자 언어 설정 기능을 구현하고 테스트를 포함한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `I18N-01 ~ I18N-05` 요구사항을 반영해 다국어 응답, 번역 리소스 관리, 다화폐 표시, 환율 자동 갱신, 사용자 언어/화폐 설정 기능을 구현한다.
- [x] `Accept-Language` 기반 응답 전환, KRW/USD/JPY 가격 환산, 일별 환율 갱신, 관리자 번역 리소스 관리를 반영한다.
- [x] i18n API, 서비스, 번역/환율 저장소와 사용자 기본값 설정을 Python 구조에 맞게 구성한다.
- [x] 언어 전환/환율 반영/설정 변경/관리자 리소스 테스트를 포함하고 `manage.py test` 기준 회귀 시나리오를 추가한다.

