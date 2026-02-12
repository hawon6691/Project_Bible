# 쇼핑몰 프로젝트 폴더/파일 트리 명세서

## 전체 구조

```bash
nestshop/
├── .env                          # 환경변수 (DB, JWT, Redis, Elasticsearch 등)
├── .env.example                  # 환경변수 예시
├── .eslintrc.js                  # ESLint 설정
├── .prettierrc                   # Prettier 설정
├── .gitignore
├── nest-cli.json                 # NestJS CLI 설정
├── package.json
├── tsconfig.json
├── tsconfig.build.json
├── docker-compose.yml            # PostgreSQL, Redis, Elasticsearch 로컬 환경
│
├── src/
│   ├── main.ts                   # 앱 진입점 (포트, CORS, Swagger, ValidationPipe)
│   ├── app.module.ts             # 루트 모듈
│   │
│   ├── common/                   # 공통 모듈
│   │   ├── common.module.ts
│   │   ├── constants/
│   │   │   ├── error-codes.ts            # 에러 코드 상수
│   │   │   ├── order-status.enum.ts      # 주문 상태 Enum
│   │   │   ├── point-type.enum.ts        # 포인트 타입 Enum (적립/사용/환원/만료)
│   │   │   └── locale.enum.ts            # 지원 언어/화폐 Enum (ko/en/ja, KRW/USD/JPY)
│   │   ├── decorators/
│   │   │   ├── current-user.decorator.ts # @CurrentUser() 파라미터 데코레이터
│   │   │   ├── roles.decorator.ts        # @Roles() 메서드 데코레이터
│   │   │   ├── public.decorator.ts       # @Public() 인증 스킵 데코레이터
│   │   │   └── locale.decorator.ts       # @Locale() 요청 언어 추출 데코레이터
│   │   ├── dto/
│   │   │   └── pagination.dto.ts         # 페이징 요청/응답 DTO
│   │   ├── entities/
│   │   │   └── base.entity.ts            # id, createdAt, updatedAt, deletedAt
│   │   ├── exceptions/
│   │   │   └── business.exception.ts     # 커스텀 비즈니스 예외
│   │   ├── filters/
│   │   │   └── http-exception.filter.ts  # 전역 예외 필터
│   │   ├── guards/
│   │   │   ├── jwt-auth.guard.ts         # JWT 인증 가드
│   │   │   ├── roles.guard.ts            # 역할 기반 인가 가드
│   │   │   └── ws-auth.guard.ts          # WebSocket 인증 가드
│   │   ├── interceptors/
│   │   │   ├── response.interceptor.ts   # 통일 응답 포맷 래핑
│   │   │   ├── logging.interceptor.ts    # 요청/응답 로깅
│   │   │   └── locale.interceptor.ts     # Accept-Language 헤더 → 로케일 설정
│   │   ├── middlewares/
│   │   │   └── transaction.middleware.ts # Transaction Isolation Level 설정
│   │   ├── pipes/
│   │   │   └── parse-int.pipe.ts             # 정수 변환 파이프
│   │   ├── utils/
│   │   │   ├── hash.util.ts                  # 해싱 유틸 (bcrypt 래퍼 등)
│   │   │   ├── slug.util.ts                  # 슬러그 생성 유틸
│   │   │   └── date.util.ts                  # 날짜 포맷/변환 유틸
│   │   └── validators/
│   │       ├── is-strong-password.validator.ts  # 비밀번호 강도 검증
│   │       ├── is-korean-phone.validator.ts     # 한국 전화번호 검증
│   │       └── match.validator.ts               # 필드 일치 검증 (비밀번호 확인 등)
│   │
│   ├── routes/                    # 라우트 상수 모듈
│   │   └── api-routes.ts                    # API 경로 상수 정의 (/api/v1/...)
│   │
│   ├── config/                   # 설정 모듈
│   │   ├── config.module.ts
│   │   ├── database.config.ts            # TypeORM 설정
│   │   ├── jwt.config.ts                 # JWT 설정
│   │   ├── redis.config.ts              # Redis 설정
│   │   ├── websocket.config.ts          # WebSocket/Socket.IO 설정
│   │   ├── elasticsearch.config.ts      # Elasticsearch 연결 설정
│   │   ├── bull.config.ts               # Bull Queue 설정 (Redis 기반)
│   │   ├── push.config.ts              # VAPID 키, Web Push 설정
│   │   ├── image.config.ts             # 이미지 최적화 설정 (사이즈, 품질, WebP)
│   │   ├── mail.config.ts              # SMTP 설정 (Nodemailer)
│   │   ├── social.config.ts              # OAuth 2.0 공급자별 설정 (Client ID/Secret)
│   │   ├── s3.config.ts                  # AWS S3 스토리지 설정
│   │   └── ffmpeg.config.ts              # FFmpeg 트랜스코딩 설정
│   │
│   ├── mail/                      # 메일 발송 모듈
│   │   ├── mail.module.ts
│   │   ├── mail.service.ts                # Nodemailer 기반 메일 발송
│   │   └── templates/
│   │       ├── email-verification.template.ts  # 이메일 인증코드 메일 템플릿
│   │       └── password-reset.template.ts      # 비밀번호 재설정 메일 템플릿
│   │
│   ├── auth/                     # 인증 모듈
│   │   ├── auth.module.ts
│   │   ├── auth.controller.ts            # 회원가입, 로그인, 로그아웃, 토큰갱신, 이메일인증, 비밀번호재설정
│   │   ├── auth.service.ts
│   │   ├── dto/
│   │   │   ├── signup.dto.ts
│   │   │   ├── login.dto.ts
│   │   │   ├── token-response.dto.ts
│   │   │   ├── verify-email.dto.ts             # 이메일 인증코드 검증 DTO
│   │   │   ├── resend-verification.dto.ts      # 인증 메일 재발송 DTO
│   │   │   ├── request-password-reset.dto.ts   # 비밀번호 재설정 요청 DTO (email + phone)
│   │   │   ├── verify-reset-code.dto.ts        # 재설정 인증코드 검증 DTO
│   │   │   ├── reset-password.dto.ts           # 새 비밀번호 설정 DTO
│   │   │   ├── social-login.dto.ts             # 소셜 로그인 요청 DTO
│   │   │   └── social-complete-signup.dto.ts    # 소셜 회원 추가정보 DTO
│   │   ├── entities/
│   │   │   ├── email-verification.entity.ts    # 이메일 인증코드 엔티티
│   │   │   └── user-social-account.entity.ts   # 소셜 계정 연동
│   │   └── strategies/
│   │       ├── jwt.strategy.ts           # Access Token 검증
│   │       ├── jwt-refresh.strategy.ts   # Refresh Token 검증
│   │       ├── google.strategy.ts          # Google OAuth 2.0
│   │       ├── kakao.strategy.ts           # Kakao OAuth 2.0
│   │       ├── naver.strategy.ts           # Naver OAuth 2.0
│   │       ├── facebook.strategy.ts        # Facebook OAuth 2.0
│   │       └── instagram.strategy.ts       # Instagram OAuth 2.0
│   │
│   ├── user/                     # 회원 모듈
│   │   ├── user.module.ts
│   │   ├── user.controller.ts            # 내 정보, 회원관리 (Admin)
│   │   ├── user.service.ts
│   │   ├── dto/
│   │   │   ├── update-user.dto.ts
│   │   │   ├── user-response.dto.ts
│   │   │   ├── update-profile.dto.ts       # 닉네임, 소개글 수정 DTO
│   │   │   └── profile-response.dto.ts     # 프로필 응답 DTO
│   │   └── entities/
│   │       └── user.entity.ts
│   │
│   ├── category/                 # 카테고리 모듈
│   │   ├── category.module.ts
│   │   ├── category.controller.ts
│   │   ├── category.service.ts
│   │   ├── dto/
│   │   │   ├── create-category.dto.ts
│   │   │   └── update-category.dto.ts
│   │   └── entities/
│   │       └── category.entity.ts
│   │
│   ├── product/                  # 상품 모듈
│   │   ├── product.module.ts
│   │   ├── product.controller.ts
│   │   ├── product.service.ts
│   │   ├── dto/
│   │   │   ├── create-product.dto.ts
│   │   │   ├── update-product.dto.ts
│   │   │   ├── product-query.dto.ts      # 검색/필터/정렬/스펙필터 쿼리
│   │   │   ├── product-response.dto.ts
│   │   │   └── product-sort.dto.ts         # 정렬 옵션 DTO (인기순/가격순/평점순)
│   │   └── entities/
│   │       ├── product.entity.ts
│   │       ├── product-option.entity.ts
│   │       └── product-image.entity.ts
│   │
│   ├── spec/                     # 스펙 모듈 (상품 스펙 정의/필터/비교)
│   │   ├── spec.module.ts
│   │   ├── spec.controller.ts
│   │   ├── spec.service.ts
│   │   ├── spec-score.service.ts         # 스펙 점수 환산 엔진 (벤치마크 매핑)
│   │   ├── dto/
│   │   │   ├── create-spec-definition.dto.ts
│   │   │   ├── set-product-spec.dto.ts
│   │   │   ├── compare-products.dto.ts
│   │   │   └── spec-score-response.dto.ts  # 점수 비교 결과 DTO
│   │   └── entities/
│   │       ├── spec-definition.entity.ts     # 스펙 항목 정의 (CPU, RAM 등)
│   │       ├── product-spec.entity.ts        # 상품별 스펙 값
│   │       └── spec-score.entity.ts          # 스펙 항목별 정규화 점수
│   │
│   ├── search/                   # 검색 모듈 (Elasticsearch)
│   │   ├── search.module.ts
│   │   ├── search.controller.ts          # 통합 검색, 자동완성, 인기 검색어
│   │   ├── search.service.ts             # Elasticsearch 쿼리 빌더
│   │   ├── search-index.service.ts       # 인덱스 생성/동기화/매핑 관리
│   │   ├── search-sync.listener.ts       # TypeORM 이벤트 → ES 동기화 리스너
│   │   ├── dto/
│   │   │   ├── search-query.dto.ts       # 검색 요청 (keyword, filters, facets)
│   │   │   ├── search-response.dto.ts    # 검색 결과 (hits, facets, suggestions)
│   │   │   └── autocomplete.dto.ts       # 자동완성 응답
│   │   └── entities/
│   │       ├── search-log.entity.ts      # 검색 로그 (분석용)
│   │       └── search-synonym.entity.ts  # 동의어 사전
│   │
│   ├── seller/                   # 판매처 모듈
│   │   ├── seller.module.ts
│   │   ├── seller.controller.ts
│   │   ├── seller.service.ts
│   │   ├── dto/
│   │   │   ├── create-seller.dto.ts
│   │   │   └── update-seller.dto.ts
│   │   └── entities/
│   │       └── seller.entity.ts
│   │
│   ├── trust/                    # 판매처 신뢰도 모듈
│   │   ├── trust.module.ts
│   │   ├── trust.controller.ts           # 신뢰도 조회, 판매처 리뷰
│   │   ├── trust.service.ts              # 신뢰도 종합 점수 계산 알고리즘
│   │   ├── trust.scheduler.ts            # 신뢰도 점수 주기적 재계산 Cron
│   │   ├── dto/
│   │   │   ├── trust-score-response.dto.ts
│   │   │   └── create-seller-review.dto.ts
│   │   └── entities/
│   │       ├── seller-trust-metric.entity.ts  # 판매처 신뢰도 지표
│   │       └── seller-review.entity.ts        # 판매처 리뷰
│   │
│   ├── price/                    # 가격비교/가격추이 모듈
│   │   ├── price.module.ts
│   │   ├── price.controller.ts
│   │   ├── price.service.ts
│   │   ├── price.scheduler.ts            # 가격 이력 스냅샷 Cron Job
│   │   ├── dto/
│   │   │   ├── create-price-entry.dto.ts
│   │   │   ├── price-history-query.dto.ts
│   │   │   └── create-price-alert.dto.ts
│   │   └── entities/
│   │       ├── price-entry.entity.ts     # 판매처별 현재 가격
│   │       ├── price-history.entity.ts   # 가격 변동 이력
│   │       └── price-alert.entity.ts     # 최저가 알림
│   │
│   ├── prediction/               # 가격 변동 예측 모듈
│   │   ├── prediction.module.ts
│   │   ├── prediction.controller.ts      # 예측 결과 조회
│   │   ├── prediction.service.ts         # 이동평균, 계절성, 추세 분석 로직
│   │   ├── prediction.scheduler.ts       # 일별 예측 배치 Cron Job
│   │   ├── dto/
│   │   │   └── prediction-response.dto.ts
│   │   └── entities/
│   │       └── price-prediction.entity.ts  # 예측 결과 저장
│   │
│   ├── crawler/                  # 크롤링/데이터 파이프라인 모듈
│   │   ├── crawler.module.ts
│   │   ├── crawler.controller.ts         # 크롤러 작업 관리 (Admin)
│   │   ├── crawler.service.ts            # 크롤러 작업 생성/관리
│   │   ├── crawler.processor.ts          # Bull Queue 워커 (실제 크롤링 로직)
│   │   ├── parsers/
│   │   │   ├── base.parser.ts            # 파서 인터페이스
│   │   │   ├── coupang.parser.ts         # 쿠팡 파서 (예시)
│   │   │   └── naver.parser.ts           # 네이버 파서 (예시)
│   │   ├── dto/
│   │   │   ├── create-crawler-job.dto.ts
│   │   │   └── crawler-job-response.dto.ts
│   │   └── entities/
│   │       ├── crawler-job.entity.ts     # 크롤러 작업 정의
│   │       └── crawler-log.entity.ts     # 크롤링 실행 로그
│   │
│   ├── push/                     # 브라우저 푸시 알림 모듈 (Web Push API)
│   │   ├── push.module.ts
│   │   ├── push.controller.ts            # 구독 등록/해제, 알림 내역
│   │   ├── push.service.ts               # VAPID 서명, 푸시 발송
│   │   ├── push.scheduler.ts             # 예약 푸시 발송 Cron
│   │   ├── dto/
│   │   │   ├── subscribe-push.dto.ts     # PushSubscription 등록
│   │   │   └── send-push.dto.ts          # 관리자 푸시 발송
│   │   └── entities/
│   │       ├── push-subscription.entity.ts  # 브라우저 구독 정보
│   │       └── push-notification.entity.ts  # 발송 이력
│   │
│   ├── i18n/                     # 다국어/다화폐 모듈
│   │   ├── i18n.module.ts
│   │   ├── i18n.controller.ts            # 번역 관리, 환율 조회
│   │   ├── i18n.service.ts               # 번역 조회, 금액 환산
│   │   ├── exchange-rate.service.ts      # 환율 갱신 (외부 API 연동)
│   │   ├── exchange-rate.scheduler.ts    # 환율 자동 갱신 Cron
│   │   ├── dto/
│   │   │   ├── create-translation.dto.ts
│   │   │   └── exchange-rate-response.dto.ts
│   │   └── entities/
│   │       ├── translation.entity.ts     # 번역 키-값 쌍
│   │       └── exchange-rate.entity.ts   # 환율 정보
│   │
│   ├── image/                    # 이미지 최적화 모듈
│   │   ├── image.module.ts
│   │   ├── image.controller.ts           # 이미지 업로드 + 최적화 요청
│   │   ├── image.service.ts              # Sharp 기반 리사이즈, WebP 변환
│   │   ├── image.processor.ts            # Bull Queue 비동기 이미지 처리 워커
│   │   ├── dto/
│   │   │   └── image-upload-response.dto.ts  # 원본 + 변환본 URL 응답
│   │   └── entities/
│   │       └── image-variant.entity.ts   # 이미지 변환본 (thumb/medium/large/webp)
│   │
│   ├── badge/                    # 배지 시스템 모듈
│   │   ├── badge.module.ts
│   │   ├── badge.controller.ts           # 배지 목록, 내 배지, 관리
│   │   ├── badge.service.ts              # 배지 자동 부여 로직
│   │   ├── badge.listener.ts             # 이벤트 리스너 (리뷰/게시글/주문 → 배지 체크)
│   │   ├── dto/
│   │   │   ├── create-badge.dto.ts
│   │   │   └── badge-response.dto.ts
│   │   └── entities/
│   │       ├── badge.entity.ts           # 배지 정의 (아이콘, 조건)
│   │       └── user-badge.entity.ts      # 사용자 획득 배지
│   │
│   ├── pc-builder/               # PC 견적 짜기 모듈 (Configurator)
│   │   ├── pc-builder.module.ts
│   │   ├── pc-builder.controller.ts      # 견적 CRUD, 호환성 체크, 공유
│   │   ├── pc-builder.service.ts         # 호환성 검증, 병목 감지, 견적 계산
│   │   ├── compatibility.service.ts      # 소켓/슬롯/전력 호환성 규칙 엔진
│   │   ├── dto/
│   │   │   ├── create-build.dto.ts
│   │   │   ├── add-part.dto.ts
│   │   │   ├── build-response.dto.ts
│   │   │   └── compatibility-check.dto.ts
│   │   └── entities/
│   │       ├── pc-build.entity.ts        # PC 견적
│   │       ├── build-part.entity.ts      # 견적 부품
│   │       └── compatibility-rule.entity.ts  # 호환성 규칙
│   │
│   ├── cart/                     # 장바구니 모듈
│   │   ├── cart.module.ts
│   │   ├── cart.controller.ts
│   │   ├── cart.service.ts
│   │   ├── dto/
│   │   │   ├── add-cart-item.dto.ts
│   │   │   └── update-cart-item.dto.ts
│   │   └── entities/
│   │       └── cart-item.entity.ts
│   │
│   ├── order/                    # 주문 모듈
│   │   ├── order.module.ts
│   │   ├── order.controller.ts
│   │   ├── order.service.ts
│   │   ├── dto/
│   │   │   ├── create-order.dto.ts
│   │   │   ├── order-query.dto.ts
│   │   │   └── order-response.dto.ts
│   │   └── entities/
│   │       ├── order.entity.ts
│   │       └── order-item.entity.ts
│   │
│   ├── payment/                  # 결제 모듈
│   │   ├── payment.module.ts
│   │   ├── payment.controller.ts
│   │   ├── payment.service.ts
│   │   ├── dto/
│   │   │   └── create-payment.dto.ts
│   │   └── entities/
│   │       └── payment.entity.ts
│   │
│   ├── address/                  # 배송지 모듈
│   │   ├── address.module.ts
│   │   ├── address.controller.ts
│   │   ├── address.service.ts
│   │   ├── dto/
│   │   │   ├── create-address.dto.ts
│   │   │   └── update-address.dto.ts
│   │   └── entities/
│   │       └── address.entity.ts
│   │
│   ├── review/                   # 리뷰 모듈
│   │   ├── review.module.ts
│   │   ├── review.controller.ts
│   │   ├── review.service.ts
│   │   ├── dto/
│   │   │   ├── create-review.dto.ts
│   │   │   └── update-review.dto.ts
│   │   └── entities/
│   │       ├── review.entity.ts
│   │       ├── review-image.entity.ts  # 리뷰 이미지
│   │       └── review-tag.entity.ts    # 리뷰 태그
│   │
│   ├── wishlist/                 # 위시리스트 모듈
│   │   ├── wishlist.module.ts
│   │   ├── wishlist.controller.ts
│   │   ├── wishlist.service.ts
│   │   └── entities/
│   │       └── wishlist.entity.ts
│   │
│   ├── point/                    # 포인트 모듈
│   │   ├── point.module.ts
│   │   ├── point.controller.ts
│   │   ├── point.service.ts
│   │   ├── point.scheduler.ts            # 포인트 만료 처리 Cron Job
│   │   ├── dto/
│   │   │   ├── point-query.dto.ts
│   │   │   └── admin-grant-point.dto.ts
│   │   └── entities/
│   │       └── point-transaction.entity.ts
│   │
│   ├── community/                # 커뮤니티/게시판 모듈
│   │   ├── community.module.ts
│   │   ├── board.controller.ts           # 게시판 관리
│   │   ├── post.controller.ts            # 게시글 CRUD
│   │   ├── comment.controller.ts         # 댓글 CRUD
│   │   ├── community.service.ts
│   │   ├── dto/
│   │   │   ├── create-post.dto.ts
│   │   │   ├── update-post.dto.ts
│   │   │   ├── post-query.dto.ts
│   │   │   └── create-comment.dto.ts
│   │   └── entities/
│   │       ├── board.entity.ts           # 게시판 (사용기, Q&A 등)
│   │       ├── post.entity.ts            # 게시글
│   │       ├── comment.entity.ts         # 댓글 (대댓글 자기참조)
│   │       └── post-like.entity.ts       # 좋아요
│   │
│   ├── inquiry/                  # 상품 문의 모듈
│   │   ├── inquiry.module.ts
│   │   ├── inquiry.controller.ts
│   │   ├── inquiry.service.ts
│   │   ├── dto/
│   │   │   ├── create-inquiry.dto.ts
│   │   │   └── answer-inquiry.dto.ts
│   │   └── entities/
│   │       └── inquiry.entity.ts
│   │
│   ├── support/                  # 고객센터 모듈
│   │   ├── support.module.ts
│   │   ├── support.controller.ts
│   │   ├── support.service.ts
│   │   ├── dto/
│   │   │   ├── create-ticket.dto.ts
│   │   │   └── reply-ticket.dto.ts
│   │   └── entities/
│   │       ├── support-ticket.entity.ts  # 1:1 문의 티켓
│   │       └── ticket-reply.entity.ts    # 티켓 답변
│   │
│   ├── faq/                      # FAQ / 도움말 / 공지사항 모듈
│   │   ├── faq.module.ts
│   │   ├── faq.controller.ts
│   │   ├── notice.controller.ts          # 공지사항
│   │   ├── faq.service.ts
│   │   ├── dto/
│   │   │   ├── create-faq.dto.ts
│   │   │   └── create-notice.dto.ts
│   │   └── entities/
│   │       ├── faq.entity.ts
│   │       └── notice.entity.ts
│   │
│   ├── activity/                 # 활동 내역 모듈
│   │   ├── activity.module.ts
│   │   ├── activity.controller.ts
│   │   ├── activity.service.ts
│   │   └── entities/
│   │       ├── view-history.entity.ts    # 최근 본 상품
│   │       └── search-history.entity.ts  # 검색 기록
│   │
│   ├── chat/                     # 실시간 채팅 모듈
│   │   ├── chat.module.ts
│   │   ├── chat.gateway.ts               # WebSocket Gateway (Socket.IO)
│   │   ├── chat.controller.ts            # REST (기록 조회)
│   │   ├── chat.service.ts
│   │   ├── dto/
│   │   │   ├── create-chat-room.dto.ts
│   │   │   └── send-message.dto.ts
│   │   └── entities/
│   │       ├── chat-room.entity.ts
│   │       └── chat-message.entity.ts
│   │
│   ├── ranking/                  # 랭킹/인기차트 모듈
│   │   ├── ranking.module.ts
│   │   ├── ranking.controller.ts
│   │   ├── ranking.service.ts
│   │   └── ranking.scheduler.ts          # 랭킹 갱신 Cron Job
│   │
│   ├── recommendation/           # 추천 모듈
│   │   ├── recommendation.module.ts
│   │   ├── recommendation.controller.ts
│   │   ├── recommendation.service.ts
│   │   ├── dto/
│   │   │   └── set-recommendation.dto.ts
│   │   └── entities/
│   │       └── recommendation.entity.ts
│   │
│   ├── deal/                     # 특가 세일 모듈
│   │   ├── deal.module.ts
│   │   ├── deal.controller.ts
│   │   ├── deal.service.ts
│   │   ├── dto/
│   │   │   ├── create-deal.dto.ts
│   │   │   └── update-deal.dto.ts
│   │   └── entities/
│   │       ├── deal.entity.ts
│   │       └── deal-product.entity.ts    # 특가 대상 상품
│   │
│   ├── friend/                    # 친구/팔로우 모듈
│   │   ├── friend.module.ts
│   │   ├── friend.controller.ts           # 친구 신청/수락/거절/차단, 팔로우
│   │   ├── friend.service.ts
│   │   ├── friend.gateway.ts              # 친구 활동 실시간 알림 (WebSocket)
│   │   ├── dto/
│   │   │   ├── friend-request.dto.ts
│   │   │   └── friend-query.dto.ts
│   │   └── entities/
│   │       └── friendship.entity.ts       # 친구 관계 (PENDING/ACCEPTED/BLOCKED)
│   │
│   ├── video/                     # 숏폼/영상 모듈
│   │   ├── video.module.ts
│   │   ├── video.controller.ts            # 숏폼 업로드, 스트리밍, 인터랙션
│   │   ├── video.service.ts
│   │   ├── video.processor.ts             # Bull Queue 트랜스코딩 Worker (FFmpeg)
│   │   ├── dto/
│   │   │   ├── upload-video.dto.ts
│   │   │   └── video-query.dto.ts
│   │   └── entities/
│   │       ├── short-form.entity.ts       # 숏폼 영상
│   │       ├── short-form-product.entity.ts # 숏폼-상품 태깅 (N:M)
│   │       └── short-form-like.entity.ts  # 숏폼 좋아요
│   │
│   ├── news/                      # 뉴스/콘텐츠 모듈
│   │   ├── news.module.ts
│   │   ├── news.controller.ts             # 뉴스 CRUD, 탭 기반 조회
│   │   ├── news.service.ts
│   │   ├── dto/
│   │   │   ├── create-news.dto.ts
│   │   │   └── news-query.dto.ts
│   │   └── entities/
│   │       ├── news-article.entity.ts     # 뉴스 게시글
│   │       ├── news-category.entity.ts    # 뉴스 카테고리
│   │       └── news-product.entity.ts     # 뉴스-상품 매핑 (N:M)
│   │
│   ├── media/                     # 멀티미디어 리소스 모듈
│   │   ├── media.module.ts
│   │   ├── media.controller.ts            # 파일 업로드, 스트리밍, 삭제
│   │   ├── media.service.ts
│   │   ├── media.processor.ts             # Bull Queue 미디어 프로세싱 Worker
│   │   ├── dto/
│   │   │   └── upload-media.dto.ts
│   │   └── entities/
│   │       └── attachment.entity.ts       # 통합 첨부파일 (다형성 관계)
│   │
│   ├── matching/                  # 상품 매핑 모듈
│   │   ├── matching.module.ts
│   │   ├── matching.controller.ts         # 매핑 승인/거절, 매핑 현황 (Admin)
│   │   ├── matching.service.ts            # 모델명 추출, 자동 매핑 알고리즘
│   │   ├── dto/
│   │   │   └── approve-matching.dto.ts
│   │   └── entities/
│   │       └── product-mapping.entity.ts  # 판매처 상품 ↔ 대표 상품 매핑
│   │
│   ├── fraud/                     # 이상 가격 탐지 모듈
│   │   ├── fraud.module.ts
│   │   ├── fraud.service.ts               # 이상 가격 탐지 알고리즘
│   │   ├── fraud.listener.ts              # 가격 등록 이벤트 리스너
│   │   └── entities/
│   │       └── fraud-alert.entity.ts      # 이상 가격 알림 기록
│   │
│   ├── used-market/               # 중고 마켓 모듈
│   │   ├── used-market.module.ts
│   │   ├── used-market.controller.ts      # 중고 시세 조회, 매입가 산정
│   │   ├── used-market.service.ts
│   │   └── entities/
│   │       └── used-price.entity.ts       # 중고 시세 데이터
│   │
│   ├── auction/                   # 역경매 모듈
│   │   ├── auction.module.ts
│   │   ├── auction.controller.ts          # 역경매 등록, 입찰, 낙찰
│   │   ├── auction.service.ts
│   │   ├── auction.gateway.ts             # 실시간 입찰 알림 (WebSocket)
│   │   ├── dto/
│   │   │   ├── create-auction.dto.ts
│   │   │   └── place-bid.dto.ts
│   │   └── entities/
│   │       ├── auction.entity.ts          # 역경매 요청
│   │       └── bid.entity.ts              # 입찰 내역
│   │
│   ├── auto/                      # 자동차 특화 모듈
│   │   ├── auto.module.ts
│   │   ├── auto.controller.ts             # 신차 견적, 렌트/리스 비교
│   │   ├── auto.service.ts
│   │   ├── dto/
│   │   │   └── car-estimate.dto.ts
│   │   └── entities/
│   │       ├── car-model.entity.ts        # 자동차 모델/트림
│   │       └── lease-offer.entity.ts      # 렌트/리스 조건
│   │
│   ├── health/                    # 헬스체크 모듈
│   │   ├── health.module.ts
│   │   └── health.controller.ts           # DB, Redis, Elasticsearch 상태 확인
│   │
│   └── upload/                   # 파일 업로드 모듈 (레거시, image 모듈로 대체 가능)
│       ├── upload.module.ts
│       ├── upload.controller.ts
│       └── upload.service.ts
│
└── test/                         # E2E 테스트
    ├── jest-e2e.json
    ├── auth.e2e-spec.ts
    ├── product.e2e-spec.ts
    ├── order.e2e-spec.ts
    ├── price.e2e-spec.ts
    ├── community.e2e-spec.ts
    ├── chat.e2e-spec.ts
    ├── search.e2e-spec.ts
    ├── crawler.e2e-spec.ts
    ├── pc-builder.e2e-spec.ts
    ├── push.e2e-spec.ts
    ├── friend.e2e-spec.ts
    ├── video.e2e-spec.ts
    ├── news.e2e-spec.ts
    ├── auction.e2e-spec.ts
    └── social-auth.e2e-spec.ts
```

---

## 모듈별 역할 요약

| 모듈             | 역할                                               | 주요 의존성                                      |
| ---------------- | -------------------------------------------------- | ------------------------------------------------ |
| `common`         | 공통 가드, 필터, 인터셉터, 데코레이터, 기본 엔티티 | -                                                |
| `config`         | 환경변수 기반 DB/JWT/Redis/ES/Bull/Push/Image/Mail 설정 | `@nestjs/config`                                 |
| `mail`           | 이메일 발송 (인증코드, 비밀번호 재설정)            | `nodemailer`                                     |
| `auth`           | 인증 (가입, 로그인, 토큰, 이메일인증, 비번재설정, 소셜로그인) | `user`, `mail`, `@nestjs/jwt`, `passport`, `passport-google-oauth20`, `passport-kakao` |
| `user`           | 회원 CRUD, 관리자 회원관리                         | -                                                |
| `category`       | 카테고리 계층 구조 관리                            | -                                                |
| `product`        | 상품 CRUD, 옵션, 이미지 관리                       | `category`, `image`                              |
| `spec`           | 스펙 정의, 스펙 값, 스펙 비교, 스펙 점수 엔진      | `product`, `category`                            |
| `search`         | Elasticsearch 통합 검색, 자동완성, 동의어          | `@nestjs/elasticsearch`, `product`               |
| `seller`         | 판매처(쇼핑몰) 등록/관리                           | -                                                |
| `trust`          | 판매처 신뢰도 점수 계산, 판매처 리뷰               | `seller`, `price`, `order`                       |
| `price`          | 가격비교, 가격추이, 최저가 알림                    | `product`, `seller`                              |
| `prediction`     | 가격 변동 예측 (이동평균, 계절성 분석)             | `price`, `product`                               |
| `crawler`        | 크롤링 작업 관리, Bull Queue 워커                  | `@nestjs/bull`, `price`, `product`               |
| `push`           | 브라우저 푸시 알림 (VAPID, Web Push API)           | `web-push`, `price`, `deal`                      |
| `i18n`           | 다국어 번역, 다화폐 환율 관리                      | -                                                |
| `image`          | 이미지 업로드, Sharp 리사이즈, WebP 변환           | `@nestjs/bull`, `sharp`                          |
| `badge`          | 배지 정의, 자동 부여, 사용자 배지 관리             | `user`, `review`, `community`, `order`           |
| `pc-builder`     | PC 견적, 호환성 체크, 병목 감지, 견적 공유         | `product`, `spec`, `price`                       |
| `cart`           | 장바구니 CRUD                                      | `product`, `seller`                              |
| `order`          | 주문 생성, 상태 관리, 재고 차감                    | `product`, `cart`, `payment`, `address`, `point` |
| `payment`        | 결제/환불 처리 (모의)                              | `order`                                          |
| `address`        | 배송지 CRUD                                        | -                                                |
| `review`         | 리뷰 CRUD                                          | `product`, `order`, `point`                      |
| `wishlist`       | 찜하기 토글                                        | `product`                                        |
| `point`          | 포인트 적립/사용/환원/만료                         | -                                                |
| `community`      | 게시판, 게시글, 댓글, 좋아요                       | `image`                                          |
| `inquiry`        | 상품 문의 + 답변                                   | `product`                                        |
| `support`        | 고객센터 1:1 문의 티켓                             | `image`                                          |
| `faq`            | FAQ, 공지사항 관리                                 | -                                                |
| `activity`       | 최근 본 상품, 검색 기록                            | `product`                                        |
| `chat`           | 실시간 1:1 채팅 (WebSocket)                        | `@nestjs/websockets`, `socket.io`                |
| `ranking`        | 인기 상품, 실시간 검색어, 가격 하락 랭킹           | `product`, `price`, Redis                        |
| `recommendation` | 오늘의 추천, 맞춤 추천                             | `product`, `activity`                            |
| `deal`           | 특가/타임세일 관리                                 | `product`                                        |
| `friend`         | 친구 신청/팔로우, 활동 피드, 차단               | `user`, `@nestjs/websockets`                     |
| `video`          | 숏폼 영상 업로드, 트랜스코딩, 스트리밍           | `@nestjs/bull`, `ffmpeg`, `product`              |
| `news`           | 뉴스/콘텐츠 관리, 상품 연동                      | `product`, `media`                               |
| `media`          | 멀티미디어 업로드, 프로세싱, 스트리밍            | `@nestjs/bull`, `s3`, `ffmpeg`                   |
| `matching`       | 크롤링 상품 → 대표 상품 자동/수동 매핑          | `product`, `crawler`                             |
| `fraud`          | 이상 가격 탐지, 관리자 알림                      | `price`, `push`                                  |
| `used-market`    | 중고 시세 조회, 매입가 산정                      | `product`, `pc-builder`                          |
| `auction`        | 역경매 요청, 입찰, 실시간 알림                   | `product`, `@nestjs/websockets`                  |
| `auto`           | 자동차 견적, 렌트/리스 비교                      | `product`                                        |
| `health`         | 서버/DB/Redis/ES 상태 모니터링                   | `@nestjs/terminus`                               |
| `upload`         | 이미지 파일 업로드 (레거시)                        | `multer`                                         |

---

## 네이밍 컨벤션

| 대상             | 규칙               | 예시                      |
| ---------------- | ------------------ | ------------------------- |
| 파일명           | kebab-case         | `create-product.dto.ts`   |
| 클래스           | PascalCase         | `CreateProductDto`        |
| 변수/함수        | camelCase          | `findOneById()`           |
| 상수             | UPPER_SNAKE_CASE   | `ORDER_STATUS`            |
| DB 테이블        | snake_case         | `product_options`         |
| DB 컬럼          | snake_case         | `created_at`              |
| API 경로         | kebab-case, 복수형 | `/api/v1/products`        |
| WebSocket 이벤트 | camelCase          | `sendMessage`, `joinRoom` |
| Bull Queue 이름  | kebab-case         | `crawler-queue`, `image-queue` |
