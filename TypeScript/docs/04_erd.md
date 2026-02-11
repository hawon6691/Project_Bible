# 쇼핑몰 프로젝트 ERD (Entity Relationship Diagram)

---

## 1. 테이블 목록

| # | 테이블명 | 설명 |
|---|---------|------|
| 1 | users | 회원 |
| 2 | categories | 카테고리 (계층형) |
| 3 | products | 상품 |
| 4 | product_options | 상품 옵션 (색상, 사이즈 등) |
| 5 | product_images | 상품 이미지 |
| 6 | spec_definitions | 스펙 항목 정의 (CPU, RAM 등) |
| 7 | product_specs | 상품별 스펙 값 |
| 8 | spec_scores | 스펙 항목별 정규화 점수 |
| 9 | sellers | 판매처 (쇼핑몰) |
| 10 | price_entries | 판매처별 현재 가격 |
| 11 | price_history | 가격 변동 이력 (일별 스냅샷) |
| 12 | price_alerts | 최저가 알림 |
| 13 | price_predictions | 가격 변동 예측 결과 |
| 14 | cart_items | 장바구니 항목 |
| 15 | addresses | 배송지 |
| 16 | orders | 주문 |
| 17 | order_items | 주문 상품 |
| 18 | payments | 결제 |
| 19 | reviews | 리뷰 |
| 20 | wishlists | 위시리스트 |
| 21 | point_transactions | 포인트 거래 내역 |
| 22 | boards | 게시판 |
| 23 | posts | 게시글 |
| 24 | comments | 댓글 (대댓글 자기참조) |
| 25 | post_likes | 게시글 좋아요 |
| 26 | inquiries | 상품 문의 |
| 27 | support_tickets | 고객센터 1:1 문의 |
| 28 | ticket_replies | 1:1 문의 답변 |
| 29 | faqs | 자주 묻는 질문 |
| 30 | notices | 공지사항 |
| 31 | view_history | 최근 본 상품 |
| 32 | search_history | 검색 기록 |
| 33 | chat_rooms | 채팅방 |
| 34 | chat_messages | 채팅 메시지 |
| 35 | recommendations | 추천 상품 |
| 36 | deals | 특가/세일 |
| 37 | deal_products | 특가 대상 상품 |
| 38 | search_logs | 검색 로그 (Elasticsearch 분석용) |
| 39 | search_synonyms | 동의어 사전 |
| 40 | crawler_jobs | 크롤러 작업 정의 |
| 41 | crawler_logs | 크롤링 실행 로그 |
| 42 | push_subscriptions | 브라우저 푸시 구독 |
| 43 | push_notifications | 푸시 알림 발송 이력 |
| 44 | seller_trust_metrics | 판매처 신뢰도 지표 |
| 45 | seller_reviews | 판매처 리뷰 |
| 46 | translations | 다국어 번역 키-값 |
| 47 | exchange_rates | 환율 정보 |
| 48 | image_variants | 이미지 변환본 (리사이즈/WebP) |
| 49 | badges | 배지 정의 |
| 50 | user_badges | 사용자 획득 배지 |
| 51 | pc_builds | PC 견적 |
| 52 | build_parts | 견적 부품 |
| 53 | compatibility_rules | 호환성 규칙 |
| 54 | email_verifications | 이메일 인증코드 (회원가입/비밀번호 재설정) |

---

## 2. 테이블 상세

### 2.1 users (회원)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 로그인 이메일 |
| password | VARCHAR(255) | NOT NULL | bcrypt 해시 |
| name | VARCHAR(50) | NOT NULL | 이름 |
| phone | VARCHAR(20) | NOT NULL | 전화번호 |
| role | ENUM('USER','SELLER','ADMIN') | NOT NULL, DEFAULT 'USER' | 역할 |
| status | ENUM('ACTIVE','INACTIVE','BLOCKED') | NOT NULL, DEFAULT 'ACTIVE' | 계정 상태 |
| email_verified | BOOLEAN | NOT NULL, DEFAULT false | 이메일 인증 완료 여부 |
| email_verified_at | TIMESTAMP | NULLABLE | 이메일 인증 완료 시각 |
| point | INT | NOT NULL, DEFAULT 0 | 보유 포인트 (비정규화) |
| preferred_locale | VARCHAR(5) | NOT NULL, DEFAULT 'ko' | 선호 언어 |
| preferred_currency | VARCHAR(3) | NOT NULL, DEFAULT 'KRW' | 선호 화폐 |
| refresh_token | VARCHAR(500) | NULLABLE | Refresh Token 해시 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW | |
| deleted_at | TIMESTAMP | NULLABLE | 소프트 삭제 |

**인덱스:**
- `idx_users_email` → email (UNIQUE)
- `idx_users_status` → status
- `idx_users_role` → role

---

### 2.2 categories (카테고리)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| name | VARCHAR(50) | NOT NULL | 카테고리명 |
| parent_id | INT | FK → categories.id, NULLABLE | 부모 카테고리 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 정렬 순서 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_categories_parent` → parent_id
**관계:** 자기 참조 (parent_id → categories.id)

---

### 2.3 products (상품)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| name | VARCHAR(200) | NOT NULL | 상품명 |
| description | TEXT | NOT NULL | 상품 설명 |
| price | INT | NOT NULL | 정가 (원) |
| discount_price | INT | NULLABLE | 할인가 |
| stock | INT | NOT NULL, DEFAULT 0 | 재고 수량 |
| status | ENUM('ON_SALE','SOLD_OUT','HIDDEN') | NOT NULL, DEFAULT 'ON_SALE' | 판매 상태 |
| category_id | INT | FK → categories.id, NOT NULL | 카테고리 |
| thumbnail_url | VARCHAR(500) | NULLABLE | 대표 이미지 URL |
| lowest_price | INT | NULLABLE | 현재 최저가 (비정규화) |
| seller_count | INT | NOT NULL, DEFAULT 0 | 판매처 수 (비정규화) |
| view_count | INT | NOT NULL, DEFAULT 0 | 조회수 |
| review_count | INT | NOT NULL, DEFAULT 0 | 리뷰 수 (비정규화) |
| average_rating | DECIMAL(2,1) | NOT NULL, DEFAULT 0.0 | 평균 별점 (비정규화) |
| version | INT | NOT NULL, DEFAULT 1 | 낙관적 잠금 버전 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |
| deleted_at | TIMESTAMP | NULLABLE | 소프트 삭제 |

**인덱스:**
- `idx_products_category` → category_id
- `idx_products_status` → status
- `idx_products_lowest_price` → lowest_price
- `idx_products_created` → created_at DESC
- `idx_products_view_count` → view_count DESC
- `idx_products_name` → name (전문 검색용)

---

### 2.4 product_options (상품 옵션)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| name | VARCHAR(50) | NOT NULL | 옵션명 |
| values | JSON | NOT NULL | 옵션값 배열 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_product_options_product` → product_id

---

### 2.5 product_images (상품 이미지)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| url | VARCHAR(500) | NOT NULL | 이미지 URL |
| is_main | BOOLEAN | NOT NULL, DEFAULT false | 대표 이미지 여부 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 정렬 순서 |
| image_variant_id | INT | FK → image_variants.id, NULLABLE | 최적화 변환본 연결 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_product_images_product` → product_id

---

### 2.6 spec_definitions (스펙 항목 정의)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| category_id | INT | FK → categories.id, NOT NULL | 소속 카테고리 |
| name | VARCHAR(50) | NOT NULL | 스펙명 (CPU, RAM 등) |
| type | ENUM('TEXT','NUMBER','SELECT') | NOT NULL | 입력 타입 |
| options | JSON | NULLABLE | SELECT 타입의 선택지 배열 |
| unit | VARCHAR(20) | NULLABLE | 단위 (GB, kg 등) |
| is_comparable | BOOLEAN | NOT NULL, DEFAULT true | 비교 대상 여부 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 정렬 순서 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_spec_definitions_category` → category_id

---

### 2.7 product_specs (상품별 스펙 값)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| spec_definition_id | INT | FK → spec_definitions.id, NOT NULL | 스펙 정의 |
| value | VARCHAR(200) | NOT NULL | 스펙 값 |
| numeric_value | DECIMAL(10,2) | NULLABLE | 수치화된 값 (비교/정렬용) |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `uq_product_specs` → (product_id, spec_definition_id) UNIQUE
- `idx_product_specs_value` → (spec_definition_id, value) — 필터링용

---

### 2.8 spec_scores (스펙 점수 매핑)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| spec_definition_id | INT | FK → spec_definitions.id, NOT NULL | 스펙 정의 |
| value | VARCHAR(200) | NOT NULL | 스펙 값 (예: "i7-1360P") |
| score | INT | NOT NULL | 정규화 점수 (0~100) |
| benchmark_source | VARCHAR(100) | NULLABLE | 벤치마크 출처 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `uq_spec_scores` → (spec_definition_id, value) UNIQUE
- `idx_spec_scores_definition` → spec_definition_id

---

### 2.9 sellers (판매처)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | NOT NULL | 판매처명 |
| url | VARCHAR(500) | NOT NULL | 판매처 사이트 URL |
| logo_url | VARCHAR(500) | NULLABLE | 로고 이미지 |
| trust_score | INT | NOT NULL, DEFAULT 0 | 종합 신뢰도 점수 (0~100, 비정규화) |
| trust_grade | VARCHAR(2) | NULLABLE | 신뢰도 등급 (A+~F, 비정규화) |
| description | VARCHAR(200) | NULLABLE | 설명 |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 활성 여부 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_sellers_active` → is_active

---

### 2.10 price_entries (판매처별 현재 가격)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| seller_id | INT | FK → sellers.id, NOT NULL | 판매처 |
| price | INT | NOT NULL | 판매 가격 |
| shipping_cost | INT | NOT NULL, DEFAULT 0 | 배송비 |
| shipping_info | VARCHAR(100) | NULLABLE | 배송 정보 |
| product_url | VARCHAR(1000) | NOT NULL | 판매처 상품 페이지 URL |
| is_available | BOOLEAN | NOT NULL, DEFAULT true | 구매 가능 여부 |
| crawled_at | TIMESTAMP | NULLABLE | 마지막 크롤링 시각 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `uq_price_entries` → (product_id, seller_id) UNIQUE
- `idx_price_entries_product_price` → (product_id, price)

---

### 2.11 price_history (가격 변동 이력)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| date | DATE | NOT NULL | 기록 날짜 |
| lowest_price | INT | NOT NULL | 해당일 최저가 |
| average_price | INT | NOT NULL | 해당일 평균가 |
| highest_price | INT | NOT NULL | 해당일 최고가 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `uq_price_history` → (product_id, date) UNIQUE
- `idx_price_history_date` → date DESC

---

### 2.12 price_alerts (최저가 알림)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 회원 |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| target_price | INT | NOT NULL | 목표 가격 |
| is_triggered | BOOLEAN | NOT NULL, DEFAULT false | 알림 발송 여부 |
| triggered_at | TIMESTAMP | NULLABLE | 알림 발송 시각 |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 활성 여부 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `uq_price_alerts` → (user_id, product_id) UNIQUE
- `idx_price_alerts_active` → (is_active, is_triggered)

---

### 2.13 price_predictions (가격 변동 예측)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| prediction_date | DATE | NOT NULL | 예측 대상 날짜 |
| predicted_price | INT | NOT NULL | 예측 가격 |
| confidence | DECIMAL(3,2) | NOT NULL | 신뢰도 (0.00~1.00) |
| trend | ENUM('RISING','FALLING','STABLE') | NOT NULL | 추세 방향 |
| trend_strength | DECIMAL(3,2) | NOT NULL | 추세 강도 (0~1) |
| moving_avg_7d | INT | NULLABLE | 7일 이동평균 |
| moving_avg_30d | INT | NULLABLE | 30일 이동평균 |
| recommendation | ENUM('BUY_NOW','BUY_SOON','WAIT','HOLD') | NOT NULL | 구매 추천 |
| seasonality_note | VARCHAR(200) | NULLABLE | 계절성 분석 메모 |
| calculated_at | TIMESTAMP | NOT NULL | 계산 시각 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `uq_price_predictions` → (product_id, prediction_date) UNIQUE
- `idx_price_predictions_date` → prediction_date

---

### 2.14 cart_items (장바구니)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 회원 |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| seller_id | INT | FK → sellers.id, NOT NULL | 판매처 |
| selected_options | VARCHAR(200) | NULLABLE | 선택한 옵션 |
| quantity | INT | NOT NULL, DEFAULT 1 | 수량 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_cart_items_user` → user_id
- `uq_cart_items` → (user_id, product_id, seller_id, selected_options) UNIQUE

---

### 2.15 addresses (배송지)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 회원 |
| label | VARCHAR(50) | NOT NULL | 배송지 별칭 |
| recipient_name | VARCHAR(50) | NOT NULL | 수령인 이름 |
| phone | VARCHAR(20) | NOT NULL | 수령인 전화번호 |
| zip_code | VARCHAR(10) | NOT NULL | 우편번호 |
| address | VARCHAR(200) | NOT NULL | 주소 |
| address_detail | VARCHAR(100) | NULLABLE | 상세주소 |
| is_default | BOOLEAN | NOT NULL, DEFAULT false | 기본 배송지 여부 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_addresses_user` → user_id

---

### 2.16 orders (주문)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| order_number | VARCHAR(30) | UNIQUE, NOT NULL | 주문번호 |
| user_id | INT | FK → users.id, NOT NULL | 주문자 |
| status | ENUM(※) | NOT NULL, DEFAULT 'ORDER_PLACED' | 주문 상태 |
| total_amount | INT | NOT NULL | 총 주문 금액 |
| point_used | INT | NOT NULL, DEFAULT 0 | 사용 포인트 |
| final_amount | INT | NOT NULL | 최종 결제 금액 (total - point) |
| recipient_name | VARCHAR(50) | NOT NULL | 수령인 (스냅샷) |
| recipient_phone | VARCHAR(20) | NOT NULL | 수령인 전화번호 (스냅샷) |
| zip_code | VARCHAR(10) | NOT NULL | 우편번호 (스냅샷) |
| address | VARCHAR(200) | NOT NULL | 주소 (스냅샷) |
| address_detail | VARCHAR(100) | NULLABLE | 상세주소 (스냅샷) |
| memo | VARCHAR(200) | NULLABLE | 배송 메모 |
| version | INT | NOT NULL, DEFAULT 1 | 낙관적 잠금 버전 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

> ※ status ENUM: `ORDER_PLACED`, `PAYMENT_PENDING`, `PAYMENT_CONFIRMED`, `PREPARING`, `SHIPPING`, `DELIVERED`, `CONFIRMED`, `CANCELLED`, `RETURN_REQUESTED`, `RETURNED`

**인덱스:**
- `idx_orders_user` → user_id
- `idx_orders_status` → status
- `idx_orders_created` → created_at DESC
- `idx_orders_number` → order_number (UNIQUE)

> **Transaction Isolation:** 주문 생성 시 `SERIALIZABLE` 레벨 적용 (재고 차감 + 포인트 사용 동시성 보호)

---

### 2.17 order_items (주문 상품)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| order_id | INT | FK → orders.id, NOT NULL | 주문 |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| seller_id | INT | FK → sellers.id, NOT NULL | 판매처 |
| product_name | VARCHAR(200) | NOT NULL | 상품명 (스냅샷) |
| seller_name | VARCHAR(100) | NOT NULL | 판매처명 (스냅샷) |
| selected_options | VARCHAR(200) | NULLABLE | 선택 옵션 (스냅샷) |
| quantity | INT | NOT NULL | 수량 |
| unit_price | INT | NOT NULL | 개당 가격 (스냅샷) |
| total_price | INT | NOT NULL | 소계 |
| is_reviewed | BOOLEAN | NOT NULL, DEFAULT false | 리뷰 작성 여부 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_order_items_order` → order_id
- `idx_order_items_product` → product_id

---

### 2.18 payments (결제)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| order_id | INT | FK → orders.id, NOT NULL | 주문 |
| method | ENUM('CARD','BANK_TRANSFER','VIRTUAL_ACCOUNT') | NOT NULL | 결제 수단 |
| amount | INT | NOT NULL | 결제 금액 |
| status | ENUM('PENDING','COMPLETED','FAILED','REFUNDED') | NOT NULL, DEFAULT 'PENDING' | |
| paid_at | TIMESTAMP | NULLABLE | 결제 완료 시각 |
| refunded_at | TIMESTAMP | NULLABLE | 환불 시각 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_payments_order` → order_id

---

### 2.19 reviews (리뷰)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 작성자 |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| order_id | INT | FK → orders.id, NOT NULL | 주문 |
| rating | SMALLINT | NOT NULL, CHECK (1~5) | 별점 |
| content | TEXT | NOT NULL | 리뷰 내용 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |
| deleted_at | TIMESTAMP | NULLABLE | 소프트 삭제 |

**인덱스:**
- `idx_reviews_product` → product_id
- `uq_reviews_user_order` → (user_id, order_id) UNIQUE

---

### 2.20 wishlists (위시리스트)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 회원 |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:** `uq_wishlists_user_product` → (user_id, product_id) UNIQUE

---

### 2.21 point_transactions (포인트 거래 내역)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 회원 |
| type | ENUM('EARN','USE','REFUND','EXPIRE','ADMIN_GRANT') | NOT NULL | 거래 유형 |
| amount | INT | NOT NULL | 금액 (+적립, -사용) |
| balance | INT | NOT NULL | 거래 후 잔액 |
| description | VARCHAR(200) | NOT NULL | 거래 설명 |
| reference_type | VARCHAR(50) | NULLABLE | 관련 엔티티 (ORDER, REVIEW 등) |
| reference_id | INT | NULLABLE | 관련 엔티티 ID |
| expires_at | TIMESTAMP | NULLABLE | 만료일 (적립 포인트) |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_point_transactions_user` → user_id
- `idx_point_transactions_type` → type
- `idx_point_transactions_expires` → expires_at

> **Transaction Isolation:** 포인트 사용/적립 시 `REPEATABLE READ` 레벨 적용 (잔액 정합성)

---

### 2.22 boards (게시판)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| name | VARCHAR(50) | NOT NULL | 게시판명 (사용기, Q&A 등) |
| slug | VARCHAR(50) | UNIQUE, NOT NULL | URL 슬러그 |
| description | VARCHAR(200) | NULLABLE | 게시판 설명 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 정렬 순서 |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 활성 여부 |
| created_at | TIMESTAMP | NOT NULL | |

---

### 2.23 posts (게시글)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| board_id | INT | FK → boards.id, NOT NULL | 게시판 |
| user_id | INT | FK → users.id, NOT NULL | 작성자 |
| title | VARCHAR(200) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 내용 |
| view_count | INT | NOT NULL, DEFAULT 0 | 조회수 |
| like_count | INT | NOT NULL, DEFAULT 0 | 좋아요 수 (비정규화) |
| comment_count | INT | NOT NULL, DEFAULT 0 | 댓글 수 (비정규화) |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |
| deleted_at | TIMESTAMP | NULLABLE | 소프트 삭제 |

**인덱스:**
- `idx_posts_board` → board_id
- `idx_posts_user` → user_id
- `idx_posts_created` → created_at DESC

---

### 2.24 comments (댓글)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| post_id | INT | FK → posts.id, NOT NULL | 게시글 |
| user_id | INT | FK → users.id, NOT NULL | 작성자 |
| parent_id | INT | FK → comments.id, NULLABLE | 부모 댓글 (대댓글) |
| content | TEXT | NOT NULL | 댓글 내용 |
| created_at | TIMESTAMP | NOT NULL | |
| deleted_at | TIMESTAMP | NULLABLE | 소프트 삭제 |

**인덱스:**
- `idx_comments_post` → post_id
- `idx_comments_parent` → parent_id

---

### 2.25 post_likes (게시글 좋아요)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| post_id | INT | FK → posts.id, NOT NULL | 게시글 |
| user_id | INT | FK → users.id, NOT NULL | 회원 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:** `uq_post_likes` → (post_id, user_id) UNIQUE

---

### 2.26 inquiries (상품 문의)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| user_id | INT | FK → users.id, NOT NULL | 질문자 |
| title | VARCHAR(200) | NOT NULL | 문의 제목 |
| content | TEXT | NOT NULL | 문의 내용 |
| is_secret | BOOLEAN | NOT NULL, DEFAULT false | 비밀글 여부 |
| answer | TEXT | NULLABLE | 답변 내용 |
| answered_by | INT | FK → users.id, NULLABLE | 답변자 |
| answered_at | TIMESTAMP | NULLABLE | 답변 시각 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_inquiries_product` → product_id
- `idx_inquiries_user` → user_id

---

### 2.27 support_tickets (고객센터 1:1 문의)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| ticket_number | VARCHAR(30) | UNIQUE, NOT NULL | 문의번호 |
| user_id | INT | FK → users.id, NOT NULL | 문의자 |
| category | ENUM('ORDER','PAYMENT','DELIVERY','ACCOUNT','OTHER') | NOT NULL | 문의 유형 |
| title | VARCHAR(200) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 내용 |
| status | ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') | NOT NULL, DEFAULT 'OPEN' | 상태 |
| attachment_urls | JSON | NULLABLE | 첨부파일 URL 배열 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_support_tickets_user` → user_id
- `idx_support_tickets_status` → status

---

### 2.28 ticket_replies (1:1 문의 답변)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| ticket_id | INT | FK → support_tickets.id, NOT NULL | 티켓 |
| user_id | INT | FK → users.id, NOT NULL | 작성자 (고객 or 관리자) |
| content | TEXT | NOT NULL | 답변 내용 |
| is_admin | BOOLEAN | NOT NULL | 관리자 답변 여부 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_ticket_replies_ticket` → ticket_id

---

### 2.29 faqs (자주 묻는 질문)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| category | ENUM('GENERAL','ORDER','PAYMENT','DELIVERY','ACCOUNT') | NOT NULL | 카테고리 |
| question | VARCHAR(300) | NOT NULL | 질문 |
| answer | TEXT | NOT NULL | 답변 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 정렬 순서 |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 노출 여부 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_faqs_category` → category

---

### 2.30 notices (공지사항)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| title | VARCHAR(200) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 내용 |
| is_pinned | BOOLEAN | NOT NULL, DEFAULT false | 상단 고정 |
| view_count | INT | NOT NULL, DEFAULT 0 | 조회수 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_notices_pinned_created` → (is_pinned DESC, created_at DESC)

---

### 2.31 view_history (최근 본 상품)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 회원 |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| viewed_at | TIMESTAMP | NOT NULL | 조회 시각 |

**인덱스:**
- `idx_view_history_user` → (user_id, viewed_at DESC)
- `uq_view_history` → (user_id, product_id) UNIQUE — UPSERT용

---

### 2.32 search_history (검색 기록)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 회원 |
| keyword | VARCHAR(100) | NOT NULL | 검색어 |
| searched_at | TIMESTAMP | NOT NULL | 검색 시각 |

**인덱스:** `idx_search_history_user` → (user_id, searched_at DESC)

---

### 2.33 chat_rooms (채팅방)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 고객 |
| admin_id | INT | FK → users.id, NULLABLE | 상담원 |
| status | ENUM('OPEN','ACTIVE','CLOSED') | NOT NULL, DEFAULT 'OPEN' | 상태 |
| last_message_at | TIMESTAMP | NULLABLE | 마지막 메시지 시각 |
| created_at | TIMESTAMP | NOT NULL | |
| closed_at | TIMESTAMP | NULLABLE | 종료 시각 |

**인덱스:**
- `idx_chat_rooms_user` → user_id
- `idx_chat_rooms_status` → status

---

### 2.34 chat_messages (채팅 메시지)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| room_id | INT | FK → chat_rooms.id, NOT NULL | 채팅방 |
| sender_id | INT | FK → users.id, NOT NULL | 발신자 |
| content | TEXT | NOT NULL | 메시지 내용 |
| is_read | BOOLEAN | NOT NULL, DEFAULT false | 읽음 여부 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_chat_messages_room` → (room_id, created_at)
- `idx_chat_messages_unread` → (room_id, is_read)

---

### 2.35 recommendations (추천 상품)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| type | ENUM('TODAY','EDITOR_PICK','NEW_ARRIVAL') | NOT NULL | 추천 유형 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 정렬 순서 |
| start_date | DATE | NULLABLE | 노출 시작일 |
| end_date | DATE | NULLABLE | 노출 종료일 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_recommendations_type` → type
- `idx_recommendations_date` → (start_date, end_date)

---

### 2.36 deals (특가/세일)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| title | VARCHAR(200) | NOT NULL | 세일명 |
| type | ENUM('SPECIAL','TIME_SALE','CLEARANCE') | NOT NULL | 세일 유형 |
| description | TEXT | NULLABLE | 설명 |
| discount_rate | INT | NOT NULL | 기본 할인율 (%) |
| banner_url | VARCHAR(500) | NULLABLE | 배너 이미지 |
| start_date | TIMESTAMP | NOT NULL | 시작일시 |
| end_date | TIMESTAMP | NOT NULL | 종료일시 |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 활성 여부 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_deals_active` → (is_active, start_date, end_date)
- `idx_deals_type` → type

---

### 2.37 deal_products (특가 대상 상품)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| deal_id | INT | FK → deals.id, NOT NULL | 특가 |
| product_id | INT | FK → products.id, NOT NULL | 상품 |
| deal_price | INT | NOT NULL | 특가 가격 |
| stock | INT | NOT NULL | 특가 한정 수량 |
| sold_count | INT | NOT NULL, DEFAULT 0 | 판매 수량 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `uq_deal_products` → (deal_id, product_id) UNIQUE
- `idx_deal_products_deal` → deal_id

---

### 2.38 search_logs (검색 로그)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NULLABLE | 검색자 (비로그인 시 NULL) |
| keyword | VARCHAR(200) | NOT NULL | 검색 키워드 |
| result_count | INT | NOT NULL | 검색 결과 수 |
| category_id | INT | NULLABLE | 카테고리 필터 |
| filters | JSON | NULLABLE | 적용된 필터 (가격, 스펙 등) |
| response_time_ms | INT | NOT NULL | 응답 시간 (ms) |
| searched_at | TIMESTAMP | NOT NULL | 검색 시각 |

**인덱스:**
- `idx_search_logs_keyword` → keyword
- `idx_search_logs_searched_at` → searched_at DESC
- `idx_search_logs_user` → user_id

---

### 2.39 search_synonyms (동의어 사전)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| word | VARCHAR(100) | UNIQUE, NOT NULL | 기준 단어 |
| synonyms | JSON | NOT NULL | 동의어 배열 |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 활성 여부 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_search_synonyms_word` → word (UNIQUE)

---

### 2.40 crawler_jobs (크롤러 작업)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | NOT NULL | 작업명 |
| seller_id | INT | FK → sellers.id, NOT NULL | 대상 판매처 |
| target_url | VARCHAR(1000) | NOT NULL | 크롤링 대상 URL |
| category_id | INT | FK → categories.id, NOT NULL | 매핑 카테고리 |
| schedule | VARCHAR(50) | NOT NULL | Cron 표현식 |
| parser_type | VARCHAR(50) | NOT NULL | 파서 유형 (COUPANG, NAVER 등) |
| config | JSON | NULLABLE | 파서별 추가 설정 |
| status | ENUM('IDLE','RUNNING','FAILED') | NOT NULL, DEFAULT 'IDLE' | 현재 상태 |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 활성 여부 |
| last_run_at | TIMESTAMP | NULLABLE | 마지막 실행 시각 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_crawler_jobs_seller` → seller_id
- `idx_crawler_jobs_active` → is_active

---

### 2.41 crawler_logs (크롤링 실행 로그)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| job_id | INT | FK → crawler_jobs.id, NOT NULL | 크롤러 작업 |
| status | ENUM('SUCCESS','PARTIAL','FAILED') | NOT NULL | 실행 결과 |
| started_at | TIMESTAMP | NOT NULL | 시작 시각 |
| finished_at | TIMESTAMP | NULLABLE | 종료 시각 |
| duration_ms | INT | NULLABLE | 소요 시간 (ms) |
| items_processed | INT | NOT NULL, DEFAULT 0 | 처리 건수 |
| items_created | INT | NOT NULL, DEFAULT 0 | 신규 생성 건수 |
| items_updated | INT | NOT NULL, DEFAULT 0 | 갱신 건수 |
| items_failed | INT | NOT NULL, DEFAULT 0 | 실패 건수 |
| error_message | TEXT | NULLABLE | 에러 메시지 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_crawler_logs_job` → job_id
- `idx_crawler_logs_started` → started_at DESC

---

### 2.42 push_subscriptions (푸시 구독)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 구독자 |
| endpoint | VARCHAR(1000) | UNIQUE, NOT NULL | 푸시 엔드포인트 URL |
| p256dh | VARCHAR(500) | NOT NULL | P-256 Diffie-Hellman 키 |
| auth | VARCHAR(500) | NOT NULL | 인증 시크릿 |
| device_name | VARCHAR(100) | NULLABLE | 디바이스 이름 |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 구독 활성 여부 |
| last_used_at | TIMESTAMP | NULLABLE | 마지막 발송 시각 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_push_subscriptions_user` → user_id
- `idx_push_subscriptions_endpoint` → endpoint (UNIQUE)

---

### 2.43 push_notifications (푸시 발송 이력)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 수신자 |
| title | VARCHAR(200) | NOT NULL | 알림 제목 |
| body | TEXT | NOT NULL | 알림 내용 |
| url | VARCHAR(500) | NULLABLE | 클릭 시 이동 URL |
| icon_url | VARCHAR(500) | NULLABLE | 아이콘 URL |
| type | ENUM('PRICE_ALERT','DEAL','SYSTEM','ADMIN') | NOT NULL | 알림 유형 |
| is_read | BOOLEAN | NOT NULL, DEFAULT false | 읽음 여부 |
| sent_at | TIMESTAMP | NOT NULL | 발송 시각 |
| read_at | TIMESTAMP | NULLABLE | 읽은 시각 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_push_notifications_user` → (user_id, is_read)
- `idx_push_notifications_sent` → sent_at DESC

---

### 2.44 seller_trust_metrics (판매처 신뢰도 지표)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| seller_id | INT | FK → sellers.id, UNIQUE, NOT NULL | 판매처 |
| delivery_score | INT | NOT NULL, DEFAULT 0 | 배송 정확도 (0~100) |
| price_accuracy | INT | NOT NULL, DEFAULT 0 | 가격 정확도 (0~100) |
| return_rate | DECIMAL(5,2) | NOT NULL, DEFAULT 0 | 반품율 (%) |
| response_time_hours | DECIMAL(5,1) | NOT NULL, DEFAULT 0 | 평균 응답시간 (시간) |
| review_score | DECIMAL(2,1) | NOT NULL, DEFAULT 0 | 평균 리뷰 점수 |
| order_count | INT | NOT NULL, DEFAULT 0 | 총 주문 수 |
| dispute_rate | DECIMAL(5,2) | NOT NULL, DEFAULT 0 | 분쟁 비율 (%) |
| overall_score | INT | NOT NULL, DEFAULT 0 | 종합 신뢰도 점수 |
| grade | VARCHAR(2) | NOT NULL, DEFAULT 'C' | 등급 (A+, A, B+, B, C, D, F) |
| trend | ENUM('IMPROVING','STABLE','DECLINING') | NOT NULL, DEFAULT 'STABLE' | 추세 |
| calculated_at | TIMESTAMP | NOT NULL | 마지막 계산 시각 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `uq_seller_trust_metrics_seller` → seller_id (UNIQUE)

---

### 2.45 seller_reviews (판매처 리뷰)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| seller_id | INT | FK → sellers.id, NOT NULL | 판매처 |
| user_id | INT | FK → users.id, NOT NULL | 작성자 |
| order_id | INT | FK → orders.id, NOT NULL | 관련 주문 |
| rating | SMALLINT | NOT NULL, CHECK (1~5) | 종합 평점 |
| delivery_rating | SMALLINT | NOT NULL, CHECK (1~5) | 배송 평점 |
| content | TEXT | NOT NULL | 리뷰 내용 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |
| deleted_at | TIMESTAMP | NULLABLE | 소프트 삭제 |

**인덱스:**
- `idx_seller_reviews_seller` → seller_id
- `uq_seller_reviews_user_order` → (user_id, order_id) UNIQUE

---

### 2.46 translations (다국어 번역)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| locale | VARCHAR(5) | NOT NULL | 언어 코드 (ko, en, ja) |
| namespace | VARCHAR(50) | NOT NULL | 네임스페이스 (product, common, error 등) |
| key | VARCHAR(200) | NOT NULL | 번역 키 |
| value | TEXT | NOT NULL | 번역 값 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `uq_translations` → (locale, namespace, key) UNIQUE
- `idx_translations_locale_ns` → (locale, namespace)

---

### 2.47 exchange_rates (환율)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| base_currency | VARCHAR(3) | NOT NULL | 기준 통화 (KRW) |
| target_currency | VARCHAR(3) | NOT NULL | 대상 통화 (USD, JPY) |
| rate | DECIMAL(12,6) | NOT NULL | 환율 |
| source | VARCHAR(50) | NOT NULL | 데이터 출처 |
| fetched_at | TIMESTAMP | NOT NULL | 조회 시각 |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `uq_exchange_rates` → (base_currency, target_currency) UNIQUE
- `idx_exchange_rates_fetched` → fetched_at DESC

---

### 2.48 image_variants (이미지 변환본)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| original_url | VARCHAR(500) | NOT NULL | 원본 이미지 URL |
| variant_type | ENUM('THUMBNAIL','MEDIUM','LARGE') | NOT NULL | 변환 유형 |
| url | VARCHAR(500) | NOT NULL | 변환본 URL |
| format | ENUM('JPEG','PNG','WEBP') | NOT NULL | 출력 포맷 |
| width | INT | NOT NULL | 가로 픽셀 |
| height | INT | NOT NULL | 세로 픽셀 |
| file_size | INT | NOT NULL | 파일 크기 (bytes) |
| processing_status | ENUM('PENDING','PROCESSING','COMPLETED','FAILED') | NOT NULL, DEFAULT 'PENDING' | 처리 상태 |
| category | VARCHAR(50) | NOT NULL | 용도 (product, community, seller) |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_image_variants_original` → original_url
- `idx_image_variants_status` → processing_status

---

### 2.49 badges (배지 정의)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| name | VARCHAR(50) | UNIQUE, NOT NULL | 배지명 |
| description | VARCHAR(200) | NOT NULL | 배지 설명 |
| icon_url | VARCHAR(500) | NOT NULL | 배지 아이콘 URL |
| type | ENUM('AUTO','MANUAL') | NOT NULL | 부여 방식 |
| condition | JSON | NULLABLE | 자동 부여 조건 ({"metric":"review_count","threshold":10}) |
| rarity | ENUM('COMMON','UNCOMMON','RARE','EPIC','LEGENDARY') | NOT NULL, DEFAULT 'COMMON' | 희귀도 |
| holder_count | INT | NOT NULL, DEFAULT 0 | 보유자 수 (비정규화) |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 활성 여부 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_badges_type` → type

---

### 2.50 user_badges (사용자 배지)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 사용자 |
| badge_id | INT | FK → badges.id, NOT NULL | 배지 |
| granted_by | ENUM('SYSTEM','ADMIN') | NOT NULL | 부여 주체 |
| granted_at | TIMESTAMP | NOT NULL | 부여 시각 |

**인덱스:**
- `uq_user_badges` → (user_id, badge_id) UNIQUE
- `idx_user_badges_user` → user_id

---

### 2.51 pc_builds (PC 견적)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 작성자 |
| name | VARCHAR(100) | NOT NULL | 견적 이름 |
| description | VARCHAR(500) | NULLABLE | 견적 설명 |
| purpose | ENUM('GAMING','OFFICE','DESIGN','DEVELOPMENT','STREAMING') | NOT NULL | 용도 |
| budget | INT | NULLABLE | 예산 (원) |
| total_price | INT | NOT NULL, DEFAULT 0 | 합계 가격 (비정규화) |
| share_code | VARCHAR(20) | UNIQUE, NULLABLE | 공유 코드 |
| is_public | BOOLEAN | NOT NULL, DEFAULT false | 공개 여부 |
| view_count | INT | NOT NULL, DEFAULT 0 | 조회수 |
| like_count | INT | NOT NULL, DEFAULT 0 | 좋아요 수 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |
| deleted_at | TIMESTAMP | NULLABLE | 소프트 삭제 |

**인덱스:**
- `idx_pc_builds_user` → user_id
- `idx_pc_builds_share` → share_code (UNIQUE)
- `idx_pc_builds_public` → (is_public, like_count DESC)

---

### 2.52 build_parts (견적 부품)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| build_id | INT | FK → pc_builds.id, NOT NULL | 견적 |
| product_id | INT | FK → products.id, NOT NULL | 상품 (부품) |
| seller_id | INT | FK → sellers.id, NULLABLE | 구매 판매처 |
| part_type | ENUM('CPU','MOTHERBOARD','RAM','GPU','SSD','HDD','PSU','CASE','COOLER','MONITOR') | NOT NULL | 부품 유형 |
| quantity | INT | NOT NULL, DEFAULT 1 | 수량 |
| price_at_add | INT | NOT NULL | 추가 시점 가격 (스냅샷) |
| created_at | TIMESTAMP | NOT NULL | |

**인덱스:**
- `idx_build_parts_build` → build_id
- `uq_build_parts` → (build_id, part_type, product_id) UNIQUE

---

### 2.53 compatibility_rules (호환성 규칙)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| rule_type | ENUM('SOCKET','RAM_TYPE','FORM_FACTOR','POWER','SLOT') | NOT NULL | 규칙 유형 |
| part_type_a | VARCHAR(20) | NOT NULL | 부품 유형 A (예: CPU) |
| spec_key_a | VARCHAR(50) | NOT NULL | 스펙 키 A (예: socket) |
| part_type_b | VARCHAR(20) | NOT NULL | 부품 유형 B (예: MOTHERBOARD) |
| spec_key_b | VARCHAR(50) | NOT NULL | 스펙 키 B (예: socket) |
| match_type | ENUM('EXACT','CONTAINS','RANGE') | NOT NULL | 매칭 방식 |
| error_message | VARCHAR(200) | NOT NULL | 불일치 시 에러 메시지 |
| severity | ENUM('ERROR','WARNING','INFO') | NOT NULL, DEFAULT 'ERROR' | 심각도 |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 활성 여부 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

**인덱스:** `idx_compatibility_rules_type` → rule_type

---

### 2.54 email_verifications (이메일 인증코드)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | INT | PK, AUTO_INCREMENT | |
| user_id | INT | FK → users.id, NOT NULL | 대상 사용자 |
| type | ENUM('SIGNUP','PASSWORD_RESET') | NOT NULL | 인증 유형 |
| code | VARCHAR(6) | NOT NULL | 6자리 인증코드 |
| attempt_count | INT | NOT NULL, DEFAULT 0 | 인증 시도 횟수 (최대 5회) |
| is_used | BOOLEAN | NOT NULL, DEFAULT false | 사용 완료 여부 |
| expires_at | TIMESTAMP | NOT NULL | 만료 시각 (발급 후 10분) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW | |

**인덱스:**
- `idx_email_verifications_user_type` → (user_id, type)
- `idx_email_verifications_code` → (user_id, code, type)
- `idx_email_verifications_expires` → expires_at

**관계:** email_verifications.user_id → users.id

---

## 3. 엔티티 관계도 (텍스트)

```
users (1) ──── (N) addresses
users (1) ──── (N) cart_items
users (1) ──── (N) orders
users (1) ──── (N) reviews
users (1) ──── (N) wishlists
users (1) ──── (N) point_transactions
users (1) ──── (N) posts
users (1) ──── (N) comments
users (1) ──── (N) post_likes
users (1) ──── (N) inquiries
users (1) ──── (N) support_tickets
users (1) ──── (N) view_history
users (1) ──── (N) search_history
users (1) ──── (N) chat_rooms          [as user_id]
users (1) ──── (N) chat_rooms          [as admin_id]
users (1) ──── (N) chat_messages
users (1) ──── (N) price_alerts
users (1) ──── (N) push_subscriptions
users (1) ──── (N) push_notifications
users (1) ──── (N) seller_reviews
users (1) ──── (N) user_badges
users (1) ──── (N) pc_builds
users (1) ──── (N) search_logs
users (1) ──── (N) email_verifications

categories (1) ──── (N) categories       [자기참조: parent_id]
categories (1) ──── (N) products
categories (1) ──── (N) spec_definitions
categories (1) ──── (N) crawler_jobs

products (1) ──── (N) product_options
products (1) ──── (N) product_images
products (1) ──── (N) product_specs
products (1) ──── (N) price_entries
products (1) ──── (N) price_history
products (1) ──── (N) price_alerts
products (1) ──── (N) price_predictions
products (1) ──── (N) cart_items
products (1) ──── (N) order_items
products (1) ──── (N) reviews
products (1) ──── (N) wishlists
products (1) ──── (N) inquiries
products (1) ──── (N) view_history
products (1) ──── (N) recommendations
products (1) ──── (N) deal_products
products (1) ──── (N) build_parts

spec_definitions (1) ──── (N) product_specs
spec_definitions (1) ──── (N) spec_scores

sellers (1) ──── (N) price_entries
sellers (1) ──── (N) cart_items
sellers (1) ──── (N) order_items
sellers (1) ──── (1) seller_trust_metrics
sellers (1) ──── (N) seller_reviews
sellers (1) ──── (N) crawler_jobs
sellers (1) ──── (N) build_parts

orders (1) ──── (N) order_items
orders (1) ──── (1) payments
orders (1) ──── (N) reviews
orders (1) ──── (N) seller_reviews

boards (1) ──── (N) posts
posts (1) ──── (N) comments
posts (1) ──── (N) post_likes
comments (1) ──── (N) comments          [자기참조: parent_id]

support_tickets (1) ──── (N) ticket_replies

chat_rooms (1) ──── (N) chat_messages

deals (1) ──── (N) deal_products

badges (1) ──── (N) user_badges

pc_builds (1) ──── (N) build_parts

crawler_jobs (1) ──── (N) crawler_logs
```

---

## 4. 관계 다이어그램 (ASCII)

```
                         ┌──────────────┐
                         │    users     │
                         └──────┬───────┘
        ┌──────┬──────┬────┬───┼───┬────┬──────┬──────┬─────┬──────┐
        ▼      ▼      ▼    ▼   ▼   ▼    ▼      ▼      ▼     ▼      ▼
   addresses cart  orders review wish  points  posts  inquiries chat  support
             items        lists        trans          rooms tickets
        ▼      ▼      ▼
   push_subs  seller_reviews  user_badges  pc_builds  search_logs

   ┌──────────────┐
   │ categories   │◄── 자기참조
   └──────┬───────┘
     ┌────┴────┐
     ▼         ▼
  products   spec_definitions
     │            │
     ├──── product_specs ◄──┘
     ├──── product_options    spec_scores ◄── spec_definitions
     ├──── product_images ──── image_variants
     ├──── price_entries ──── sellers
     ├──── price_history       │
     ├──── price_alerts        ├──── seller_trust_metrics (1:1)
     ├──── price_predictions   ├──── seller_reviews
     ├──── recommendations     └──── crawler_jobs ──── crawler_logs
     ├──── deal_products ──── deals
     └──── build_parts ──── pc_builds

   ┌────────┐
   │ boards │
   └───┬────┘
       ▼
    ┌───────┐
    │ posts │
    └───┬───┘
    ┌───┼───┐
    ▼   ▼   ▼
 comments  post_likes
    │
    ▼
 comments (대댓글)

   ┌─────────────┐     ┌──────────────┐
   │ badges      │────▶│ user_badges  │
   └─────────────┘     └──────────────┘

   ┌──────────────────┐     ┌────────────────┐
   │ push_subscriptions│    │ push_notifications│
   └──────────────────┘     └────────────────┘

   ┌──────────────┐     ┌────────────────┐
   │ translations │     │ exchange_rates │
   └──────────────┘     └────────────────┘

   ┌──────────────────┐     ┌─────────────────┐
   │ search_synonyms  │     │ compatibility_rules│
   └──────────────────┘     └─────────────────┘
```

---

## 5. 비정규화 필드 관리

| 테이블 | 비정규화 필드 | 갱신 시점 | 설명 |
|--------|-------------|----------|------|
| users | point | 포인트 거래 시 | 잔액 조회 성능 |
| products | lowest_price | 가격 등록/수정/삭제 시 | 최저가 즉시 노출 |
| products | seller_count | 가격 등록/삭제 시 | 판매처 수 즉시 노출 |
| products | view_count | 상품 조회 시 | 인기 랭킹용 |
| products | review_count | 리뷰 생성/삭제 시 | COUNT 쿼리 회피 |
| products | average_rating | 리뷰 생성/삭제/수정 시 | AVG 쿼리 회피 |
| sellers | trust_score | 신뢰도 재계산 시 (Cron) | 빠른 조회 |
| sellers | trust_grade | 신뢰도 재계산 시 (Cron) | 빠른 조회 |
| posts | like_count | 좋아요 토글 시 | COUNT 쿼리 회피 |
| posts | comment_count | 댓글 생성/삭제 시 | COUNT 쿼리 회피 |
| order_items | product_name, unit_price | 주문 생성 시 | 상품 변경 영향 차단 |
| order_items | seller_name | 주문 생성 시 | 판매처 변경 영향 차단 |
| orders | recipient_*, address* | 주문 생성 시 | 배송지 변경 영향 차단 |
| deal_products | sold_count | 특가 주문 시 | 잔여 수량 실시간 표시 |
| badges | holder_count | 배지 부여/회수 시 | 보유자 수 조회 |
| pc_builds | total_price | 부품 추가/삭제 시 | 합계 즉시 표시 |
| build_parts | price_at_add | 부품 추가 시 | 추가 시점 가격 고정 |

---

## 6. Transaction Isolation Level 전략

| 작업 | Isolation Level | 잠금 방식 | 설명 |
|------|----------------|----------|------|
| 주문 생성 (재고 차감) | SERIALIZABLE | 비관적 잠금 (SELECT ... FOR UPDATE) | 재고 동시 차감 방지 |
| 포인트 사용/적립 | REPEATABLE READ | 비관적 잠금 (SELECT ... FOR UPDATE) | 포인트 잔액 정합성 |
| 특가 상품 구매 | SERIALIZABLE | 비관적 잠금 | 한정 수량 동시 구매 방지 |
| 상품 조회/검색 | READ COMMITTED (기본) | - | 일반 조회 성능 우선 |
| 가격 업데이트 | READ COMMITTED | 낙관적 잠금 (version 컬럼) | 크롤러 병렬 업데이트 |
| 배지 자동 부여 | REPEATABLE READ | - | 동일 배지 중복 부여 방지 |
