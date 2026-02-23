# 쇼핑몰 프로젝트 운영 런북

> 대상: `TypeScript/nestshop` 백엔드 운영 및 장애 대응
> 기준일: 2026-02-23

---

## 1. 운영 환경 기본 체크

배포/장애 대응 전에 아래 항목을 먼저 확인한다.

- API 서버: `GET /health` 응답 상태
- DB 연결: PostgreSQL 연결/쿼리 가능 여부
- Redis 연결: Bull Queue 및 캐시 연결 여부
- Elasticsearch 연결: 검색 API 및 인덱스 상태
- 스토리지/트랜스코딩: 업로드 디렉토리 접근 권한, FFmpeg 실행 가능 여부

핵심 확인 API:

- `GET /health`
- `GET /errors/codes`
- `GET /search/admin/index/status`
- `GET /search/admin/index/outbox/summary`
- `GET /crawler/admin/monitoring`
- `GET /resilience/circuit-breakers`

---

## 2. 서비스 기동 순서

권장 기동 순서:

1. PostgreSQL
2. Redis
3. Elasticsearch
4. Nestshop API
5. Bull Worker(동일 프로세스 또는 분리 프로세스)

개발/검증 기준 명령:

```bash
cd TypeScript/nestshop
npm ci
npm run build
npm run start:prod
```

---

## 3. 배포 전 체크리스트

- DB 마이그레이션 상태 확인 (`migration:run` 필요 여부)
- `.env`와 `.env.example` 키 정합성 확인
- `npx tsc -p tsconfig.json --noEmit --incremental false` 통과
- CI 통과 여부 확인
  - quality
  - e2e-critical
  - perf-smoke

---

## 4. 장애 분류와 1차 대응

### 4.1 API 장애 (5xx 급증)

1. `GET /health`로 인프라 체크
2. 최근 배포/설정 변경 확인
3. 로그에서 공통 에러코드/스택트레이스 확인
4. 필요 시 직전 안정 버전 롤백

### 4.2 검색 장애 (Elasticsearch)

1. `GET /search/admin/index/status` 확인
2. Outbox 누적 확인: `GET /search/admin/index/outbox/summary`
3. 실패 Outbox 재큐잉: `POST /search/admin/index/outbox/requeue-failed`
4. 필요 시 전체 재색인: `POST /search/admin/index/reindex`

### 4.3 큐 지연/실패 증가 (Bull)

1. 큐별 실패 Job 조회: `GET /admin/queues/:queueName/failed`
2. 개별 재시도: `POST /admin/queues/:queueName/jobs/:jobId/retry`
3. 일괄 재시도: `POST /admin/queues/:queueName/failed/retry?limit=50`
4. 불필요/유해 Job 삭제: `DELETE /admin/queues/:queueName/jobs/:jobId`

지원 큐:

- `activity-log`
- `video-transcode`
- `crawler-collect`
- `search-index-sync`

### 4.4 Circuit Breaker OPEN 상태 지속

1. `GET /resilience/circuit-breakers`로 OPEN 대상 확인
2. 외부 의존성(결제/크롤링/검색) 상태 확인
3. 원인 복구 후 수동 초기화: `POST /resilience/circuit-breakers/:name/reset`

---

## 5. 주요 운영 시나리오

### 5.1 검색 동기화 누락 대응

1. `GET /search/admin/index/outbox/summary`
2. failed 수치 증가 시 `POST /search/admin/index/outbox/requeue-failed`
3. 특정 상품 누락 시 `POST /search/admin/index/products/:id/reindex`
4. 전체 불일치 시 `POST /search/admin/index/reindex`

### 5.2 크롤러 적재 실패 대응

1. `GET /crawler/admin/runs?status=FAILED&page=1&limit=20`
2. 실패 원인별 분류(네트워크/파싱/DB)
3. `POST /crawler/admin/jobs/:id/run` 또는 `POST /crawler/admin/triggers` 재실행
4. 반복 실패 판매처는 작업 비활성화 후 원인 수정

### 5.3 비디오 트랜스코딩 실패 대응

1. `GET /shortforms/:id/transcode-status` 확인
2. 사용자 재시도: `POST /shortforms/:id/transcode/retry`
3. 운영자 큐 재시도: `POST /admin/queues/video-transcode/failed/retry`
4. FFmpeg 바이너리/옵션(`FFMPEG_BIN`, `FFMPEG_PRESET`, `FFMPEG_CRF`) 확인

---

## 6. 롤백 기준

아래 중 하나라도 만족하면 롤백을 우선 고려한다.

- 결제/주문 흐름 장애가 10분 이상 지속
- 검색/가격비교 핵심 API 오류율이 기준치 초과
- 큐 실패가 급증하고 재시도로도 복구되지 않음
- 데이터 무결성 손상 가능성이 확인됨

롤백 후 필수 조치:

1. 에러율/지연 복구 확인
2. 누적된 실패 Job/Outbox 정리
3. 장애 원인과 재발 방지 액션 문서화

---

## 7. 운영자 실행 명령 모음

```bash
# 타입 체크
npx tsc -p tsconfig.json --noEmit --incremental false

# 핵심 E2E
npm run test:e2e:critical

# 성능 스모크 (로컬 mock 서버 기반)
npm run test:perf:mock-server
# 별도 터미널에서
BASE_URL=http://127.0.0.1:3310 npm run test:perf:smoke
```

---

## 8. 변경 이력

- 2026-02-23: 초안 작성 (운영/장애 대응 절차, 큐 복구 API 포함)
