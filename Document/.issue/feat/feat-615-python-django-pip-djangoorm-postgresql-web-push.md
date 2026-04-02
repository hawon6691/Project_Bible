---
name: "✨ Feature Request"
about: 새로운 기능 제안
title: "[FEAT] Python Django Pip DjangoORM PostgreSQL Web Push"
labels: feature
assignees: ""
issue: "[FEAT] Python Django Pip DjangoORM PostgreSQL Web Push 구현"
commit: "feat: (#615) Python Django Pip DjangoORM PostgreSQL Web Push 구현"
branch: "feat/#615/python-django-pip-djangoorm-postgresql-web-push"
---

## ✨ 기능 요약

> Python Django ORM PostgreSQL 대표 구현체에 브라우저 푸시 구독, 알림 발송, 알림 설정 관리 기능을 구현하고 테스트를 포함한다.

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] `PUSH-01 ~ PUSH-07` 요구사항을 반영해 푸시 구독 등록/해제, 최저가 도달 알림, 주문 상태 알림, 채팅 메시지 알림, 특가 시작 알림, 알림 설정 관리 기능을 구현한다.
- [x] Web Push API, VAPID 기반 구독 정보 관리, 시스템 이벤트 연계, 유형별 on/off 설정을 반영한다.
- [x] 푸시 API, 서비스, 구독 모델, 발송 파이프라인과 사용자 설정 저장소를 Python 구조에 맞게 구성한다.
- [x] 구독/해제/설정/이벤트 알림 테스트를 포함하고 `manage.py test` 기준 회귀 시나리오를 추가한다.

