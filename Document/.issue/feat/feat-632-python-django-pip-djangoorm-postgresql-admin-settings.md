---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Python Django Pip DjangoORM PostgreSQL Admin Settings"
labels: feature
assignees: ""
issue: "[FEAT] Python Django Pip DjangoORM PostgreSQL Admin Settings 구현"
commit: "feat: (#632) Python Django Pip DjangoORM PostgreSQL Admin Settings 구현"
branch: "feat/#632/python-django-pip-djangoorm-postgresql-admin-settings"
---

## ✨ 기능 요약

> Python Django ORM PostgreSQL 대표 구현체에 관리자 시스템 설정 기능을 구현하고 테스트를 포함한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `ADMN-10 ~ ADMN-13` 요구사항을 반영해 허용 확장자 관리, 업로드 파일 제어, 파일 용량 제한 설정, 리뷰 정책 관리 기능을 구현한다.
- [x] 화이트리스트 기반 확장자 검증, 변조 MIME 타입 차단, 미디어 타입별 업로드 제한, 리뷰 이미지 개수/포인트 정책 관리를 반영한다.
- [x] 관리자 설정 API, 서비스, 모델, 저장소와 정책 캐시/검증 로직을 Python 구조에 맞게 구성한다.
- [x] 정책 조회/수정/업로드 제어/리뷰 정책 테스트를 포함하고 `manage.py test` 기준 회귀 시나리오를 추가한다.
