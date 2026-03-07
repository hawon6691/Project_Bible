---
name: "🧾 Document "
about: 문서 작업
title: "[DOCS] 배포 직전 최종 게이트 문서 추가"
labels: document
issue: "[DOCS] 배포 직전 최종 게이트 문서 추가"
commit: "docs: (#213) 수동 CI 기반 배포 직전 최종 게이트 문서화"
branch: "docs/#213/pre-release-final-gate"
assignees: ""
---

## 📌 관련 이슈

> 이 PR과 연관된 이슈 번호를 작성해주세요.

- #213

---

## 🧾 문서 요약

> 어떤 문서인지 한 줄로 설명해주세요.

배포 직전 `workflow_dispatch` 수동 실행 조합과 합격/중단 기준을 표준 문서로 정리했습니다.

## 🎯 목적 및 배경

> 왜 이 문서가 필요한가요?

- 구현/테스트 단계 완료 후 배포 승인 절차를 일관되게 수행해야 함
- 수동 CI 입력값과 판정 기준이 누락되면 배포 품질이 흔들릴 수 있음

---

## 📋 요구사항

> 구현해야 할 세부 사항을 체크리스트로 작성해주세요.

- [x] 최종 게이트 문서 신규 작성 (`Document/05_pre-release-final-gate.md`)
- [x] 수동 CI 실행 위치/입력값 조합 정리
- [x] 합격 기준/중단 기준 명시
- [x] 배포 승인 체크리스트 포함

---

## 🚀 Render 적용 가이드

> `live_smoke_base_url` 입력을 위해 Render에서 API URL을 확보하는 절차입니다.

1. Render 가입 후 `New +` → `Web Service` 선택
2. GitHub 저장소 연결 후 `03_Project_Bible/Project_Bible/TypeScript/nestshop` 기준으로 배포 설정
3. Build Command: `npm ci && npm run build`
4. Start Command: `npm run start:prod`
5. Environment Variables에 `.env.example` 기준 필수 값 입력
6. 배포 완료 후 Render 도메인 확인 (`https://<service-name>.onrender.com`)
7. 해당 주소를 CI 수동 실행 값에 입력
: `live_smoke_base_url=https://<service-name>.onrender.com`

## ✅ 수동 CI 입력 예시

- `run_release_gate=true`
- `run_live_smoke=true`
- `live_smoke_base_url=https://<service-name>.onrender.com`
- `target_ref=main` (또는 배포할 태그/브랜치)

