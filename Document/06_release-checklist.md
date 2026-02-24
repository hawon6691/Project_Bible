# Nestshop 1차 릴리스 체크리스트

> 범위: `TypeScript/nestshop`
> 목적: 배포 전/배포 직후 품질 게이트를 표준화하고 운영 리스크를 최소화한다.

---

## 1. 사전 준비

- [ ] 대상 브랜치 최신화 (`main` 기준)
- [ ] DB 마이그레이션 적용 계획 확인 (`migration:run`/롤백 절차)
- [ ] `.env` 필수 키 누락 여부 확인
- [ ] 외부 의존성 상태 확인 (PostgreSQL, Redis, Elasticsearch, SMTP, S3, FFmpeg)

---

## 2. 정적 검증

- [ ] `npm run lint:check`
- [ ] `npx tsc -p tsconfig.json --noEmit --incremental false`
- [ ] `npm run build`

---

## 3. 테스트 검증

- [ ] `npm test -- --runInBand`
- [ ] `npm run test:e2e:critical`
- [ ] `npm run test:e2e:ops`
- [ ] `npm run test:e2e -- ops-dashboard-thresholds.e2e-spec.ts --runInBand`
- [ ] (선택) `BASE_URL=http://127.0.0.1:3310 npm run test:perf:smoke`

핵심 E2E 포함 범위:

- Auth/Search
- Public API(health/error-code)
- Admin Platform(search/crawler/resilience)
- Queue Admin(stats/failed/retry/delete)
- Ops Dashboard(summary/degraded/threshold)

---

## 4. CI 확인

- [ ] `quality` 잡 통과
- [ ] `e2e-critical` 잡 통과
- [ ] `perf-smoke` 잡 통과

CI 아티팩트 확인:

- [ ] `e2e-critical-report` (`test-results/e2e-critical-report.json`)
- [ ] `perf-smoke-artifacts` (`test-results/perf-smoke-summary.json`, `perf-server.log`)

---

## 5. 운영 설정 확인

- [ ] Ops Dashboard 임계치 점검
  - [ ] `OPS_ALERT_SEARCH_FAILED_THRESHOLD`
  - [ ] `OPS_ALERT_CRAWLER_FAILED_RUNS_THRESHOLD`
  - [ ] `OPS_ALERT_QUEUE_FAILED_THRESHOLD`
- [ ] 임계치 판정 규칙 확인 (`현재값 >= 임계치`, `<=0`은 경보 비활성화)

---

## 6. 배포 직후 점검

- [ ] `GET /health` 정상 확인
- [ ] `GET /admin/ops-dashboard/summary` 정상 확인
- [ ] `GET /search/admin/index/status` 정상 확인
- [ ] `GET /search/admin/index/outbox/summary` 정상 확인
- [ ] `GET /admin/queues/stats` 정상 확인

---

## 7. 장애 대응 준비

- [ ] 롤백 기준/절차 공유 완료
- [ ] 담당자/연락 체계 공유 완료
- [ ] 장애 대응 런북 확인 (`Document/05_operations-runbook.md`)

---

## 8. 릴리스 완료 기준

- [ ] 기능/버그/문서/테스트 이슈 문서 최신화 완료
- [ ] CI 3종 게이트 통과
- [ ] 운영 API/대시보드 정상
- [ ] 릴리스 노트 작성 완료
