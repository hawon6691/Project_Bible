# JavaScript Next Steps Checklist

`BackEnd/JavaScript/expressshop_prismaorm` 구현은 공통 문서 기준으로 아래 순서대로 진행한다.

기준 문서
- `Document/01_requirements.md`
- `Document/02_api-specification.md`
- `Document/03_erd.md`
- `Document/04_language.md`
- `Document/05_test-specification.md`
- `Document/06_ci-specification.md`

공통 DB 자산
- `Database/docker/docker-compose.yml`
- `Database/postgresql/postgres_table.sql`
- `Database/postgresql/sample_data.sql`
- `Database/postgresql/setting.sql`
- `Database/mysql/mysql_table.sql`
- `Database/mysql/mysql_sample_data.sql`
- `Database/marie/marie_table.sql`
- `Database/marie/marie_sample_data.sql`

DB 원칙
- JavaScript 구현은 자체 임의 스키마를 기준으로 만들지 않는다.
- 공통 `Database` 폴더의 SQL과 Docker 자산을 기준으로 맞춘다.
- ORM 모델은 공통 SQL 스키마에 맞게 역으로 정렬한다.
- 로컬 실행과 테스트 환경은 공통 `docker-compose.yml`을 우선 사용한다.

## 1. 프로젝트 식별자 정리

목표
- JavaScript ORM 구현체를 공통 규칙에 맞는 프로젝트로 확정한다.

해야 할 일
- `package.json` 이름이 `javascript-express-prisma` 규칙과 맞는지 검토하고 필요 시 수정
- `README.md`에 프로젝트 개요, 실행 방법, 환경 변수, Swagger 경로 초안 추가
- `Document/JavaScript/README.md`에 단계 문서 링크 유지

완료 기준
- 프로젝트 이름, 폴더 이름, 문서 이름이 JavaScript Prisma ORM 구현체로 일관됨

## 2. 기본 애플리케이션 구조 확장

목표
- Express 애플리케이션을 공통 API 구조에 맞는 서버로 확장한다.

해야 할 일
- `src/app.js`, `src/server.js` 구조를 모듈형으로 정리
- 공통 라우터 루트 `/api/v1` 적용
- 전역 에러 처리기 추가
- 공통 응답 envelope `success`, `data`, `meta`, `error` 적용
- 환경 변수 로더 추가
- 로깅, CORS, JSON body limit, request id 기본 미들웨어 추가

완료 기준
- 최소 `health`, `docs`, `api/v1` 진입 구조가 공통 규약대로 동작

## 3. Prisma 스키마를 ERD 기준으로 확장

목표
- 현재 최소 스키마를 공통 ERD에 맞는 핵심 도메인 스키마로 확장한다.

우선 엔티티
- `User`
- `AuthAccessToken`
- `Category`
- `Product`
- `ProductSpec`
- `Seller`
- `Price`
- `Cart`
- `CartItem`
- `Address`
- `Order`
- `OrderItem`
- `Payment`
- `Review`
- `Wishlist`
- `Point`

다음 엔티티
- `Post`
- `Comment`
- `Inquiry`
- `SupportTicket`
- `Activity`
- `ChatRoom`
- `ChatMessage`
- `Ranking`
- `Recommendation`
- `Deal`
- `Friend`
- `Shortform`
- `MediaAsset`
- `NewsArticle`
- `Matching`
- `FraudSignal`
- `UsedMarketItem`
- `AutoListing`
- `Auction`
- `CompareSnapshot`
- `AdminSetting`

해야 할 일
- `Database/postgresql/postgres_table.sql`을 우선 기준으로 읽고 `prisma/schema.prisma`에 모델, relation, enum, index, unique 제약 추가
- PostgreSQL 기준으로 먼저 작성하되 `Database/mysql/mysql_table.sql`과 충돌하지 않게 설계
- MySQL 호환 가능성이 깨지는 타입 사용은 피함
- 공통 SQL의 테이블명과 컬럼명을 임의 변경하지 않음
- Prisma 모델명과 실제 테이블명 매핑 규칙 문서화

완료 기준
- 핵심 도메인 모델과 관계가 ERD 기준으로 Prisma schema에 반영됨

## 4. DB 초기화 및 마이그레이션 자산 작성

목표
- 로컬과 CI에서 동일하게 사용할 수 있는 DB 초기화 체계를 만든다.

해야 할 일
- `.env.example` 확장
- 공통 `Database/docker/docker-compose.yml` 기준 연결 정보 정리
- 공통 SQL을 적용하는 초기화 절차 작성
- Prisma migration 사용 여부를 공통 SQL과 충돌하지 않게 결정
- seed 스크립트는 공통 sample data를 우선 활용하고 부족한 테스트 데이터만 보강
- 개발 DB와 테스트 DB 분리
- migration 검증용 명령 정리

완료 기준
- 공통 Docker로 DB를 띄운 뒤 공통 SQL 적용 후 앱이 기동 가능

## 5. 공통 도메인별 API 구현

목표
- 공통 요구사항과 API 명세 범위를 벗어나지 않는 기능 구현을 진행한다.

구현 순서
1. `auth`, `user`
2. `category`, `product`, `spec`, `seller`, `price`
3. `cart`, `address`, `order`, `payment`
4. `review`, `wishlist`, `point`
5. `community`, `inquiry`, `support`
6. `activity`, `chat`
7. `ranking`, `recommendation`, `deal`
8. `friend`, `shortform`, `media`, `news`, `matching`
9. `fraud`, `used-market`, `auto`, `auction`, `compare`
10. `admin-settings`, `ops`, `observability`, `queue-admin`, `health`

각 도메인에서 해야 할 일
- route 작성
- controller 또는 handler 작성
- service 작성
- Prisma repository 계층 정리
- request validation 추가
- 인증/인가 연결
- 성공/실패 응답 형식 통일

완료 기준
- `Document/02_api-specification.md` 기준 주요 엔드포인트가 구현됨

## 6. 인증, 권한, 보안 기본선 구현

목표
- 역할 기반 접근 제어와 운영 API 보호를 먼저 닫는다.

해야 할 일
- bearer token 인증 추가
- 역할 `GUEST`, `USER`, `SELLER`, `ADMIN` 처리
- 공개 API와 관리자 API 경계 분리
- rate limit 기본선 추가
- 입력 검증 및 공통 보안 헤더 적용

완료 기준
- 공개 API와 관리자 API 접근 경계가 분리됨

## 7. Swagger/OpenAPI 추가

목표
- API 명세를 실제 서버에서 노출한다.

해야 할 일
- Swagger/OpenAPI 라이브러리 연결
- 경로 초안 결정
- `/docs/swagger`, `/docs/openapi` 또는 동등 경로 구성
- auth bearer 스키마 추가
- 주요 응답 예시 연결

완료 기준
- 브라우저와 JSON 경로에서 명세 확인 가능

## 8. 공통 테스트 명세 구현

목표
- 공통 테스트 명세 05번 기준 테스트 자산을 채운다.

도메인 테스트
- `AuthApi`
- `UserApi`
- `CategoryApi`
- `ProductApi`
- `SpecApi`
- `SellerApi`
- `PriceApi`
- `CartApi`
- `AddressApi`
- `OrderApi`
- `PaymentApi`
- `ReviewApi`
- `WishlistApi`
- `PointApi`
- `CommunityApi`
- `InquiryApi`
- `SupportApi`
- `ActivityApi`
- `ChatApi`
- `RankingApi`
- `RecommendationApi`
- `DealApi`
- `FriendApi`
- `ShortformApi`
- `MediaApi`
- `NewsApi`
- `MatchingApi`
- `FraudApi`
- `UsedMarketApi`
- `AutoApi`
- `AuctionApi`
- `CompareApi`
- `HealthRoute`

플랫폼 E2E
- `PublicApiE2E`
- `ContractPublicApiE2E`
- `AuthSearchE2E`
- `AdminAuthorizationBoundaryE2E`
- `AdminPlatformE2E`
- `ObservabilityE2E`
- `OpsDashboardE2E`
- `OpsDashboardDependencyFailuresE2E`
- `OpsDashboardResilienceE2E`
- `OpsDashboardThresholdsE2E`
- `QueueAdminE2E`
- `RateLimitRegressionE2E`
- `ResilienceAutoTuneE2E`
- `SecurityRegressionE2E`

성능 자산
- `smoke.perf`
- `soak.perf`
- `spike-search.perf`
- `price-compare.perf`
- `search-ranking.perf`

스크립트 자산
- `analyze-stability`
- `live-smoke`
- `migration-roundtrip`
- `validate-migrations`

완료 기준
- 테스트 자산 이름과 역할이 공통 테스트 명세와 맞음

## 9. CI 구성

목표
- 공통 CI 명세 06번 기준 워크플로를 구성한다.

자동 잡
- `validate-dispatch-inputs`
- `quality`
- 핵심 회귀
- DB 또는 migration 검증
- contract/docs export
- `perf-smoke`

수동 잡
- `release-gate`
- `migration-validation`
- `contract-e2e`
- `security-regression`
- `admin-boundary`
- `dependency-failure`
- `perf-extended`
- `live-smoke`

해야 할 일
- JavaScript 전용 workflow 파일 추가
- npm cache, Prisma generate, 공통 SQL 초기화, test, docs export 단계 연결
- 공통 Docker DB를 사용하는 초기화 단계 반영
- artifact 업로드 규칙 추가

완료 기준
- 공통 CI 명세상 필요한 축이 workflow에 반영됨

## 10. JavaScript 문서 세트 작성

목표
- 다른 언어와 동일한 문서 세트를 JavaScript에도 맞춘다.

작성 대상
- `01_folder-structure.md`
- `02_runbook.md`
- `03_implementation-status.md`
- `04_completion-report.md`
- `05_pre-release-final-gate.md`
- `06_requirements-api-gap-analysis.md`
- `language-api-specification.md`

완료 기준
- 구현체, 테스트, CI, 운영 기준을 문서로 추적 가능

## 11. 최종 갭 체크

목표
- 공통 문서 대비 누락을 정리하고 완료 판단을 내린다.

해야 할 일
- 요구사항 대비 기능 누락 점검
- API 명세 대비 경로, 응답, 권한 차이 점검
- ERD 대비 모델 누락 점검
- 테스트 명세 대비 자산 누락 점검
- CI 명세 대비 잡 누락 점검

완료 기준
- `06_requirements-api-gap-analysis.md`에 치명적 미구현 여부가 정리됨

## 12. 실행 순서 요약

1. 프로젝트 이름과 구조 정리
2. Express 공통 구조 정리
3. Prisma schema 확장
4. migration/seed 작성
5. 공통 API 구현
6. 인증/보안 경계 구현
7. Swagger/OpenAPI 연결
8. 테스트 자산 작성
9. CI workflow 작성
10. JavaScript 문서 세트 작성
11. 갭 분석 작성
12. 최종 검증
