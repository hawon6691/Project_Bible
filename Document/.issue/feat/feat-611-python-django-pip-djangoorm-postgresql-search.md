---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Python Django Pip DjangoORM PostgreSQL Search"
labels: feature
assignees: ""
issue: "[FEAT] Python Django Pip DjangoORM PostgreSQL Search 구현"
commit: "feat: (#611) Python Django Pip DjangoORM PostgreSQL Search 구현"
branch: "feat/#611/python-django-pip-djangoorm-postgresql-search"
---

## ✨ 기능 요약

> Python Django ORM PostgreSQL 대표 구현체에 고급 검색, 자동완성, 검색 로그, 가중치 관리, 최근 검색어 기능을 구현하고 테스트를 포함한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `SRCH-01 ~ SRCH-10` 요구사항을 반영해 전문 검색, 자동완성, 검색 필터, 연관 검색어, 검색 로그 수집, 검색 가중치 관리, 최근 검색어 관리/삭제, 자동 저장 On/Off, 검색어 하이라이팅 기능을 구현한다.
- [x] Elasticsearch 스타일 검색 경험, 형태소 분석/오타 보정, 복합 필터, Redis 기반 최근 검색어, 관리자 가중치 설정을 반영한다.
- [x] 검색 API, 서비스, 색인/로그 저장소, 캐시/검색 인프라 연동을 Python 구조에 맞게 구성한다.
- [x] 검색/자동완성/최근 검색어/필터/관리자 가중치 테스트를 포함하고 `manage.py test` 기준 회귀 시나리오를 추가한다.

