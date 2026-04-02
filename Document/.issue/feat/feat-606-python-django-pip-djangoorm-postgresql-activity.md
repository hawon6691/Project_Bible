---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Python Django Pip DjangoORM PostgreSQL Activity"
labels: feature
assignees: ""
issue: "[FEAT] Python Django Pip DjangoORM PostgreSQL Activity 구현"
commit: "feat: (#606) Python Django Pip DjangoORM PostgreSQL Activity 구현"
branch: "feat/#606/python-django-pip-djangoorm-postgresql-activity"
---

## ✨ 기능 요약

> Python Django ORM PostgreSQL 대표 구현체에 활동 내역, 최근 본 상품, 검색 기록, 동적 기준 알림, 비회원 장바구니 유지 기능을 구현하고 테스트를 포함한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `ACT-01 ~ ACT-06` 요구사항을 반영해 활동 내역 조회, 최근 본 상품, 검색 기록, 검색 기록 삭제, 동적 기준 알림, 비회원 장바구니 유지 기능을 구현한다.
- [x] 최근 본 상품 최대 개수, 검색 기록 개별/전체 삭제, 가격 하락/역대 최저가 조건 알림, Redis 세션 기반 게스트 장바구니 병합을 반영한다.
- [x] 활동 API, 서비스, 모델, 저장소와 세션/알림 정책을 Python 구조에 맞게 구성한다.
- [x] 활동 조회, 기록 관리, 동적 알림, 게스트 장바구니 병합 테스트를 포함하고 `manage.py test` 기준 회귀 시나리오를 추가한다.

