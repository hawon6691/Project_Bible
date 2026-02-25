# Nestshop 배포 직전 최종 게이트

> 대상: `TypeScript/nestshop`  
> 목적: 배포 직전 수동 CI 실행 및 결과 판정을 표준화한다.

---

## 1. 실행 위치

- GitHub 저장소 `Actions` 탭
- 워크플로우: `Nestshop CI`
- 실행 방식: `Run workflow`

---

## 2. 필수 실행 조합

### A. Release Gate

- `target_ref`: 배포 대상 브랜치/태그
- `run_release_gate=true`
- 나머지 `run_*`는 `false`

### B. 운영 스모크

- `target_ref`: 배포 대상 브랜치/태그
- `run_live_smoke=true`
- `live_smoke_base_url`: 실검증 대상 URL
- `live_smoke_prefix`: 기본 `/api/v1`

---

## 3. 합격 기준

- `validate-dispatch-inputs`: 성공
- `release-gate`: 성공
- `live-smoke-manual`: 성공
- `release-gate-report` 아티팩트 존재
- `live-smoke-report` 아티팩트 존재
- Actions Step Summary에 실패 항목 없음

---

## 4. 실패 시 중단 기준

- `release-gate` 실패 시 배포 중단
- `live-smoke` 실패 시 배포 중단
- 수동 재실행은 원인 수정 후 1회만 허용

---

## 5. 배포 승인 체크

- [ ] Release Gate 성공 확인
- [ ] Live Smoke 성공 확인
- [ ] 주요 아티팩트 다운로드/보관
- [ ] 릴리스 노트 최신화
- [ ] 배포 승인자 확인

