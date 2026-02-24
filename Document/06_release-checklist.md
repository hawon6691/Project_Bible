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
- [ ] `npm run test:release:gate`
- [ ] `npm run test:e2e -- ops-dashboard-thresholds.e2e-spec.ts --runInBand`
- [ ] `npm run test:e2e:contract`
- [ ] `npm run test:e2e:admin-boundary`
- [ ] `npm run test:e2e:rate-limit`
- [ ] `npm run test:security:regression`
- [ ] `npm run test:migration:validate`
- [ ] `MIGRATION_ROUNDTRIP_ALLOW=true npm run test:migration:roundtrip` (테스트 DB에서만)
- [ ] `LIVE_SMOKE_BASE_URL=<url> npm run test:smoke:live` (staging/prod smoke)
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
- [ ] `release-gate-report` (`test-results/release-gate-e2e-critical.json`, `test-results/release-gate-e2e-ops.json`)
- [ ] `perf-smoke-manual-artifacts` (`workflow_dispatch run_perf_smoke=true` 실행 시)
- [ ] `contract-e2e-report` (`workflow_dispatch run_contract_e2e=true` 실행 시)
- [ ] `critical-stability-reports` (`workflow_dispatch run_stability_check=true` 실행 시)
- [ ] `perf-extended-artifacts` (`workflow_dispatch run_perf_extended=true` 실행 시)
- [ ] `security-regression-report` (`workflow_dispatch run_security_regression=true` 실행 시)
- [ ] `migration-validation-report` (`workflow_dispatch run_migration_validation=true` 실행 시)
- [ ] `dependency-failure-e2e-report` (`workflow_dispatch run_dependency_failure=true` 실행 시)
- [ ] `admin-boundary-e2e-report` (`workflow_dispatch run_admin_boundary=true` 실행 시)
- [ ] `migration-roundtrip-report` (`workflow_dispatch run_migration_roundtrip=true` 실행 시)
- [ ] `rate-limit-regression-e2e-report` (`workflow_dispatch run_rate_limit_regression=true` 실행 시)
- [ ] `live-smoke-report` (`workflow_dispatch run_live_smoke=true` 실행 시)

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
