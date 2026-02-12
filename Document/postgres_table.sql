-- ============================================================
-- NestShop 쇼핑몰 데이터베이스 스키마
-- PostgreSQL 16
-- 총 73개 테이블
-- ============================================================

-- 확장 모듈
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- 1. users (회원)
-- ============================================================
CREATE TYPE user_role AS ENUM ('USER', 'SELLER', 'ADMIN');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'BLOCKED');

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role user_role NOT NULL DEFAULT 'USER',
    status user_status NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT false,
    email_verified_at TIMESTAMP NULL,
    nickname VARCHAR(30) UNIQUE NOT NULL,
    bio VARCHAR(200) NULL,
    profile_image_url VARCHAR(500) NULL,
    search_history_enabled BOOLEAN NOT NULL DEFAULT true,
    point INT NOT NULL DEFAULT 0,
    preferred_locale VARCHAR(5) NOT NULL DEFAULT 'ko',
    preferred_currency VARCHAR(3) NOT NULL DEFAULT 'KRW',
    refresh_token VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_role ON users (role);

-- ============================================================
-- 2. categories (카테고리 - 계층형)
-- ============================================================
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id INT NULL REFERENCES categories(id),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_categories_parent ON categories (parent_id);

-- ============================================================
-- 3. products (상품)
-- ============================================================
CREATE TYPE product_status AS ENUM ('ON_SALE', 'SOLD_OUT', 'HIDDEN');

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    price INT NOT NULL,
    discount_price INT NULL,
    stock INT NOT NULL DEFAULT 0,
    status product_status NOT NULL DEFAULT 'ON_SALE',
    category_id INT NOT NULL REFERENCES categories(id),
    thumbnail_url VARCHAR(500) NULL,
    lowest_price INT NULL,
    seller_count INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    average_rating DECIMAL(2,1) NOT NULL DEFAULT 0.0,
    sales_count INT NOT NULL DEFAULT 0,
    popularity_score DECIMAL(10,2) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_products_lowest_price ON products (lowest_price);
CREATE INDEX idx_products_created ON products (created_at DESC);
CREATE INDEX idx_products_view_count ON products (view_count DESC);
CREATE INDEX idx_products_popularity ON products (popularity_score);

-- ============================================================
-- 4. product_options (상품 옵션)
-- ============================================================
CREATE TABLE product_options (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(id),
    name VARCHAR(50) NOT NULL,
    "values" JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_options_product ON product_options (product_id);

-- ============================================================
-- 5. product_images (상품 이미지)
-- ============================================================
CREATE TABLE product_images (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(id),
    url VARCHAR(500) NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT false,
    sort_order INT NOT NULL DEFAULT 0,
    image_variant_id INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_images_product ON product_images (product_id);

-- ============================================================
-- 6. spec_definitions (스펙 항목 정의)
-- ============================================================
CREATE TYPE spec_input_type AS ENUM ('TEXT', 'NUMBER', 'SELECT');
CREATE TYPE spec_data_type AS ENUM ('NUMBER', 'STRING', 'BOOLEAN');

CREATE TABLE spec_definitions (
    id SERIAL PRIMARY KEY,
    category_id INT NOT NULL REFERENCES categories(id),
    name VARCHAR(50) NOT NULL,
    type spec_input_type NOT NULL,
    options JSON NULL,
    unit VARCHAR(20) NULL,
    is_comparable BOOLEAN NOT NULL DEFAULT true,
    data_type spec_data_type NOT NULL DEFAULT 'STRING',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_spec_definitions_category ON spec_definitions (category_id);

-- ============================================================
-- 7. product_specs (상품별 스펙 값)
-- ============================================================
CREATE TABLE product_specs (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(id),
    spec_definition_id INT NOT NULL REFERENCES spec_definitions(id),
    value VARCHAR(200) NOT NULL,
    numeric_value DECIMAL(10,2) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_product_specs UNIQUE (product_id, spec_definition_id)
);

CREATE INDEX idx_product_specs_value ON product_specs (spec_definition_id, value);

-- ============================================================
-- 8. spec_scores (스펙 점수 매핑)
-- ============================================================
CREATE TABLE spec_scores (
    id SERIAL PRIMARY KEY,
    spec_definition_id INT NOT NULL REFERENCES spec_definitions(id),
    value VARCHAR(200) NOT NULL,
    score INT NOT NULL,
    benchmark_source VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_spec_scores UNIQUE (spec_definition_id, value)
);

CREATE INDEX idx_spec_scores_definition ON spec_scores (spec_definition_id);

-- ============================================================
-- 9. sellers (판매처)
-- ============================================================
CREATE TABLE sellers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    logo_url VARCHAR(500) NULL,
    trust_score INT NOT NULL DEFAULT 0,
    trust_grade VARCHAR(2) NULL,
    description VARCHAR(200) NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sellers_active ON sellers (is_active);

-- ============================================================
-- 10. price_entries (판매처별 현재 가격)
-- ============================================================
CREATE TYPE shipping_type AS ENUM ('FREE', 'PAID', 'CONDITIONAL');

CREATE TABLE price_entries (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(id),
    seller_id INT NOT NULL REFERENCES sellers(id),
    price INT NOT NULL,
    shipping_cost INT NOT NULL DEFAULT 0,
    shipping_info VARCHAR(100) NULL,
    product_url VARCHAR(1000) NOT NULL,
    shipping_fee INT NOT NULL DEFAULT 0,
    shipping_type shipping_type NOT NULL DEFAULT 'PAID',
    total_price INT GENERATED ALWAYS AS (price + shipping_fee) STORED,
    click_count INT NOT NULL DEFAULT 0,
    is_available BOOLEAN NOT NULL DEFAULT true,
    crawled_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_price_entries UNIQUE (product_id, seller_id)
);

CREATE INDEX idx_price_entries_product_price ON price_entries (product_id, price);

-- ============================================================
-- 11. price_history (가격 변동 이력)
-- ============================================================
CREATE TABLE price_history (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(id),
    date DATE NOT NULL,
    lowest_price INT NOT NULL,
    average_price INT NOT NULL,
    highest_price INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_price_history UNIQUE (product_id, date)
);

CREATE INDEX idx_price_history_date ON price_history (date DESC);

-- ============================================================
-- 12. price_alerts (최저가 알림)
-- ============================================================
CREATE TABLE price_alerts (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    product_id INT NOT NULL REFERENCES products(id),
    target_price INT NOT NULL,
    is_triggered BOOLEAN NOT NULL DEFAULT false,
    triggered_at TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_price_alerts UNIQUE (user_id, product_id)
);

CREATE INDEX idx_price_alerts_active ON price_alerts (is_active, is_triggered);

-- ============================================================
-- 13. price_predictions (가격 변동 예측)
-- ============================================================
CREATE TYPE price_trend AS ENUM ('RISING', 'FALLING', 'STABLE');
CREATE TYPE buy_recommendation AS ENUM ('BUY_NOW', 'BUY_SOON', 'WAIT', 'HOLD');

CREATE TABLE price_predictions (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(id),
    prediction_date DATE NOT NULL,
    predicted_price INT NOT NULL,
    confidence DECIMAL(3,2) NOT NULL,
    trend price_trend NOT NULL,
    trend_strength DECIMAL(3,2) NOT NULL,
    moving_avg_7d INT NULL,
    moving_avg_30d INT NULL,
    recommendation buy_recommendation NOT NULL,
    seasonality_note VARCHAR(200) NULL,
    calculated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_price_predictions UNIQUE (product_id, prediction_date)
);

CREATE INDEX idx_price_predictions_date ON price_predictions (prediction_date);

-- ============================================================
-- 14. cart_items (장바구니)
-- ============================================================
CREATE TABLE cart_items (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    product_id INT NOT NULL REFERENCES products(id),
    seller_id INT NOT NULL REFERENCES sellers(id),
    selected_options VARCHAR(200) NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cart_items UNIQUE (user_id, product_id, seller_id, selected_options)
);

CREATE INDEX idx_cart_items_user ON cart_items (user_id);

-- ============================================================
-- 15. addresses (배송지)
-- ============================================================
CREATE TABLE addresses (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    label VARCHAR(50) NOT NULL,
    recipient_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    address VARCHAR(200) NOT NULL,
    address_detail VARCHAR(100) NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_addresses_user ON addresses (user_id);

-- ============================================================
-- 16. orders (주문)
-- ============================================================
CREATE TYPE order_status AS ENUM (
    'ORDER_PLACED', 'PAYMENT_PENDING', 'PAYMENT_CONFIRMED',
    'PREPARING', 'SHIPPING', 'DELIVERED', 'CONFIRMED',
    'CANCELLED', 'RETURN_REQUESTED', 'RETURNED'
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    order_number VARCHAR(30) UNIQUE NOT NULL,
    user_id INT NOT NULL REFERENCES users(id),
    status order_status NOT NULL DEFAULT 'ORDER_PLACED',
    total_amount INT NOT NULL,
    point_used INT NOT NULL DEFAULT 0,
    final_amount INT NOT NULL,
    recipient_name VARCHAR(50) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    address VARCHAR(200) NOT NULL,
    address_detail VARCHAR(100) NULL,
    memo VARCHAR(200) NULL,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_user ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_created ON orders (created_at DESC);

-- ============================================================
-- 17. order_items (주문 상품)
-- ============================================================
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id),
    product_id INT NOT NULL REFERENCES products(id),
    seller_id INT NOT NULL REFERENCES sellers(id),
    product_name VARCHAR(200) NOT NULL,
    seller_name VARCHAR(100) NOT NULL,
    selected_options VARCHAR(200) NULL,
    quantity INT NOT NULL,
    unit_price INT NOT NULL,
    total_price INT NOT NULL,
    is_reviewed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_order_items_product ON order_items (product_id);

-- ============================================================
-- 18. payments (결제)
-- ============================================================
CREATE TYPE payment_method AS ENUM ('CARD', 'BANK_TRANSFER', 'VIRTUAL_ACCOUNT');
CREATE TYPE payment_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');

CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id),
    method payment_method NOT NULL,
    amount INT NOT NULL,
    status payment_status NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP NULL,
    refunded_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_order ON payments (order_id);

-- ============================================================
-- 19. reviews (리뷰)
-- ============================================================
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    product_id INT NOT NULL REFERENCES products(id),
    order_id INT NOT NULL REFERENCES orders(id),
    rating SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT NOT NULL,
    is_best BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL,
    CONSTRAINT uq_reviews_user_order UNIQUE (user_id, order_id)
);

CREATE INDEX idx_reviews_product ON reviews (product_id);

-- ============================================================
-- 20. wishlists (위시리스트)
-- ============================================================
CREATE TABLE wishlists (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    product_id INT NOT NULL REFERENCES products(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_wishlists_user_product UNIQUE (user_id, product_id)
);

-- ============================================================
-- 21. point_transactions (포인트 거래 내역)
-- ============================================================
CREATE TYPE point_type AS ENUM ('EARN', 'USE', 'REFUND', 'EXPIRE', 'ADMIN_GRANT');

CREATE TABLE point_transactions (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    type point_type NOT NULL,
    amount INT NOT NULL,
    balance INT NOT NULL,
    description VARCHAR(200) NOT NULL,
    reference_type VARCHAR(50) NULL,
    reference_id INT NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_point_transactions_user ON point_transactions (user_id);
CREATE INDEX idx_point_transactions_type ON point_transactions (type);
CREATE INDEX idx_point_transactions_expires ON point_transactions (expires_at);

-- ============================================================
-- 22. boards (게시판)
-- ============================================================
CREATE TABLE boards (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 23. posts (게시글)
-- ============================================================
CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    board_id INT NOT NULL REFERENCES boards(id),
    user_id INT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_posts_board ON posts (board_id);
CREATE INDEX idx_posts_user ON posts (user_id);
CREATE INDEX idx_posts_created ON posts (created_at DESC);

-- ============================================================
-- 24. comments (댓글 - 대댓글 자기참조)
-- ============================================================
CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    post_id INT NOT NULL REFERENCES posts(id),
    user_id INT NOT NULL REFERENCES users(id),
    parent_id INT NULL REFERENCES comments(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_comments_post ON comments (post_id);
CREATE INDEX idx_comments_parent ON comments (parent_id);

-- ============================================================
-- 25. post_likes (게시글 좋아요)
-- ============================================================
CREATE TABLE post_likes (
    id SERIAL PRIMARY KEY,
    post_id INT NOT NULL REFERENCES posts(id),
    user_id INT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_post_likes UNIQUE (post_id, user_id)
);

-- ============================================================
-- 26. inquiries (상품 문의)
-- ============================================================
CREATE TABLE inquiries (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(id),
    user_id INT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_secret BOOLEAN NOT NULL DEFAULT false,
    answer TEXT NULL,
    answered_by INT NULL REFERENCES users(id),
    answered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inquiries_product ON inquiries (product_id);
CREATE INDEX idx_inquiries_user ON inquiries (user_id);

-- ============================================================
-- 27. support_tickets (고객센터 1:1 문의)
-- ============================================================
CREATE TYPE ticket_category AS ENUM ('ORDER', 'PAYMENT', 'DELIVERY', 'ACCOUNT', 'OTHER');
CREATE TYPE ticket_status AS ENUM ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED');

CREATE TABLE support_tickets (
    id SERIAL PRIMARY KEY,
    ticket_number VARCHAR(30) UNIQUE NOT NULL,
    user_id INT NOT NULL REFERENCES users(id),
    category ticket_category NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status ticket_status NOT NULL DEFAULT 'OPEN',
    attachment_urls JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_support_tickets_user ON support_tickets (user_id);
CREATE INDEX idx_support_tickets_status ON support_tickets (status);

-- ============================================================
-- 28. ticket_replies (1:1 문의 답변)
-- ============================================================
CREATE TABLE ticket_replies (
    id SERIAL PRIMARY KEY,
    ticket_id INT NOT NULL REFERENCES support_tickets(id),
    user_id INT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    is_admin BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ticket_replies_ticket ON ticket_replies (ticket_id);

-- ============================================================
-- 29. faqs (자주 묻는 질문)
-- ============================================================
CREATE TYPE faq_category AS ENUM ('GENERAL', 'ORDER', 'PAYMENT', 'DELIVERY', 'ACCOUNT');

CREATE TABLE faqs (
    id SERIAL PRIMARY KEY,
    category faq_category NOT NULL,
    question VARCHAR(300) NOT NULL,
    answer TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_faqs_category ON faqs (category);

-- ============================================================
-- 30. notices (공지사항)
-- ============================================================
CREATE TABLE notices (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT false,
    view_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notices_pinned_created ON notices (is_pinned DESC, created_at DESC);

-- ============================================================
-- 31. view_history (최근 본 상품)
-- ============================================================
CREATE TABLE view_history (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    product_id INT NOT NULL REFERENCES products(id),
    viewed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_view_history UNIQUE (user_id, product_id)
);

CREATE INDEX idx_view_history_user ON view_history (user_id, viewed_at DESC);

-- ============================================================
-- 32. search_history (검색 기록)
-- ============================================================
CREATE TABLE search_history (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    keyword VARCHAR(100) NOT NULL,
    searched_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_search_history_user ON search_history (user_id, searched_at DESC);

-- ============================================================
-- 33. chat_rooms (채팅방)
-- ============================================================
CREATE TYPE chat_room_status AS ENUM ('OPEN', 'ACTIVE', 'CLOSED');

CREATE TABLE chat_rooms (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    admin_id INT NULL REFERENCES users(id),
    status chat_room_status NOT NULL DEFAULT 'OPEN',
    last_message_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMP NULL
);

CREATE INDEX idx_chat_rooms_user ON chat_rooms (user_id);
CREATE INDEX idx_chat_rooms_status ON chat_rooms (status);

-- ============================================================
-- 34. chat_messages (채팅 메시지)
-- ============================================================
CREATE TABLE chat_messages (
    id SERIAL PRIMARY KEY,
    room_id INT NOT NULL REFERENCES chat_rooms(id),
    sender_id INT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_messages_room ON chat_messages (room_id, created_at);
CREATE INDEX idx_chat_messages_unread ON chat_messages (room_id, is_read);

-- ============================================================
-- 35. recommendations (추천 상품)
-- ============================================================
CREATE TYPE recommendation_type AS ENUM ('TODAY', 'EDITOR_PICK', 'NEW_ARRIVAL');

CREATE TABLE recommendations (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(id),
    type recommendation_type NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    start_date DATE NULL,
    end_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recommendations_type ON recommendations (type);
CREATE INDEX idx_recommendations_date ON recommendations (start_date, end_date);

-- ============================================================
-- 36. deals (특가/세일)
-- ============================================================
CREATE TYPE deal_type AS ENUM ('SPECIAL', 'TIME_SALE', 'CLEARANCE');

CREATE TABLE deals (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    type deal_type NOT NULL,
    description TEXT NULL,
    discount_rate INT NOT NULL,
    banner_url VARCHAR(500) NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_deals_active ON deals (is_active, start_date, end_date);
CREATE INDEX idx_deals_type ON deals (type);

-- ============================================================
-- 37. deal_products (특가 대상 상품)
-- ============================================================
CREATE TABLE deal_products (
    id SERIAL PRIMARY KEY,
    deal_id INT NOT NULL REFERENCES deals(id),
    product_id INT NOT NULL REFERENCES products(id),
    deal_price INT NOT NULL,
    stock INT NOT NULL,
    sold_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_deal_products UNIQUE (deal_id, product_id)
);

CREATE INDEX idx_deal_products_deal ON deal_products (deal_id);

-- ============================================================
-- 38. search_logs (검색 로그)
-- ============================================================
CREATE TABLE search_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id INT NULL REFERENCES users(id),
    keyword VARCHAR(200) NOT NULL,
    result_count INT NOT NULL,
    category_id INT NULL,
    filters JSON NULL,
    response_time_ms INT NOT NULL,
    searched_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_search_logs_keyword ON search_logs (keyword);
CREATE INDEX idx_search_logs_searched_at ON search_logs (searched_at DESC);
CREATE INDEX idx_search_logs_user ON search_logs (user_id);

-- ============================================================
-- 39. search_synonyms (동의어 사전)
-- ============================================================
CREATE TABLE search_synonyms (
    id SERIAL PRIMARY KEY,
    word VARCHAR(100) UNIQUE NOT NULL,
    synonyms JSON NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 40. crawler_jobs (크롤러 작업)
-- ============================================================
CREATE TYPE crawler_job_status AS ENUM ('IDLE', 'RUNNING', 'FAILED');

CREATE TABLE crawler_jobs (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    seller_id INT NOT NULL REFERENCES sellers(id),
    target_url VARCHAR(1000) NOT NULL,
    category_id INT NOT NULL REFERENCES categories(id),
    schedule VARCHAR(50) NOT NULL,
    parser_type VARCHAR(50) NOT NULL,
    config JSON NULL,
    status crawler_job_status NOT NULL DEFAULT 'IDLE',
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_run_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crawler_jobs_seller ON crawler_jobs (seller_id);
CREATE INDEX idx_crawler_jobs_active ON crawler_jobs (is_active);

-- ============================================================
-- 41. crawler_logs (크롤링 실행 로그)
-- ============================================================
CREATE TYPE crawler_log_status AS ENUM ('SUCCESS', 'PARTIAL', 'FAILED');

CREATE TABLE crawler_logs (
    id BIGSERIAL PRIMARY KEY,
    job_id INT NOT NULL REFERENCES crawler_jobs(id),
    status crawler_log_status NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP NULL,
    duration_ms INT NULL,
    items_processed INT NOT NULL DEFAULT 0,
    items_created INT NOT NULL DEFAULT 0,
    items_updated INT NOT NULL DEFAULT 0,
    items_failed INT NOT NULL DEFAULT 0,
    error_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crawler_logs_job ON crawler_logs (job_id);
CREATE INDEX idx_crawler_logs_started ON crawler_logs (started_at DESC);

-- ============================================================
-- 42. push_subscriptions (푸시 구독)
-- ============================================================
CREATE TABLE push_subscriptions (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    endpoint VARCHAR(1000) UNIQUE NOT NULL,
    p256dh VARCHAR(500) NOT NULL,
    auth VARCHAR(500) NOT NULL,
    device_name VARCHAR(100) NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_push_subscriptions_user ON push_subscriptions (user_id);

-- ============================================================
-- 43. push_notifications (푸시 발송 이력)
-- ============================================================
CREATE TYPE notification_type AS ENUM ('PRICE_ALERT', 'DEAL', 'SYSTEM', 'ADMIN');

CREATE TABLE push_notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    url VARCHAR(500) NULL,
    icon_url VARCHAR(500) NULL,
    type notification_type NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    sent_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_push_notifications_user ON push_notifications (user_id, is_read);
CREATE INDEX idx_push_notifications_sent ON push_notifications (sent_at DESC);

-- ============================================================
-- 44. seller_trust_metrics (판매처 신뢰도 지표)
-- ============================================================
CREATE TYPE trust_trend AS ENUM ('IMPROVING', 'STABLE', 'DECLINING');

CREATE TABLE seller_trust_metrics (
    id SERIAL PRIMARY KEY,
    seller_id INT UNIQUE NOT NULL REFERENCES sellers(id),
    delivery_score INT NOT NULL DEFAULT 0,
    price_accuracy INT NOT NULL DEFAULT 0,
    return_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    response_time_hours DECIMAL(5,1) NOT NULL DEFAULT 0,
    review_score DECIMAL(2,1) NOT NULL DEFAULT 0,
    order_count INT NOT NULL DEFAULT 0,
    dispute_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    overall_score INT NOT NULL DEFAULT 0,
    grade VARCHAR(2) NOT NULL DEFAULT 'C',
    trend trust_trend NOT NULL DEFAULT 'STABLE',
    calculated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 45. seller_reviews (판매처 리뷰)
-- ============================================================
CREATE TABLE seller_reviews (
    id SERIAL PRIMARY KEY,
    seller_id INT NOT NULL REFERENCES sellers(id),
    user_id INT NOT NULL REFERENCES users(id),
    order_id INT NOT NULL REFERENCES orders(id),
    rating SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    delivery_rating SMALLINT NOT NULL CHECK (delivery_rating >= 1 AND delivery_rating <= 5),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL,
    CONSTRAINT uq_seller_reviews_user_order UNIQUE (user_id, order_id)
);

CREATE INDEX idx_seller_reviews_seller ON seller_reviews (seller_id);

-- ============================================================
-- 46. translations (다국어 번역)
-- ============================================================
CREATE TABLE translations (
    id SERIAL PRIMARY KEY,
    locale VARCHAR(5) NOT NULL,
    namespace VARCHAR(50) NOT NULL,
    key VARCHAR(200) NOT NULL,
    value TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_translations UNIQUE (locale, namespace, key)
);

CREATE INDEX idx_translations_locale_ns ON translations (locale, namespace);

-- ============================================================
-- 47. exchange_rates (환율)
-- ============================================================
CREATE TABLE exchange_rates (
    id SERIAL PRIMARY KEY,
    base_currency VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL,
    rate DECIMAL(12,6) NOT NULL,
    source VARCHAR(50) NOT NULL,
    fetched_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_exchange_rates UNIQUE (base_currency, target_currency)
);

CREATE INDEX idx_exchange_rates_fetched ON exchange_rates (fetched_at DESC);

-- ============================================================
-- 48. image_variants (이미지 변환본)
-- ============================================================
CREATE TYPE image_variant_type AS ENUM ('THUMBNAIL', 'MEDIUM', 'LARGE');
CREATE TYPE image_format AS ENUM ('JPEG', 'PNG', 'WEBP');
CREATE TYPE image_processing_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');

CREATE TABLE image_variants (
    id SERIAL PRIMARY KEY,
    original_url VARCHAR(500) NOT NULL,
    variant_type image_variant_type NOT NULL,
    url VARCHAR(500) NOT NULL,
    format image_format NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    file_size INT NOT NULL,
    processing_status image_processing_status NOT NULL DEFAULT 'PENDING',
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_image_variants_original ON image_variants (original_url);
CREATE INDEX idx_image_variants_status ON image_variants (processing_status);

-- product_images FK 추가 (image_variants 테이블 생성 후)
ALTER TABLE product_images
    ADD CONSTRAINT fk_product_images_variant
    FOREIGN KEY (image_variant_id) REFERENCES image_variants(id);

-- ============================================================
-- 49. badges (배지 정의)
-- ============================================================
CREATE TYPE badge_type AS ENUM ('AUTO', 'MANUAL');
CREATE TYPE badge_rarity AS ENUM ('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY');

CREATE TABLE badges (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200) NOT NULL,
    icon_url VARCHAR(500) NOT NULL,
    type badge_type NOT NULL,
    condition JSON NULL,
    rarity badge_rarity NOT NULL DEFAULT 'COMMON',
    holder_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_badges_type ON badges (type);

-- ============================================================
-- 50. user_badges (사용자 배지)
-- ============================================================
CREATE TYPE badge_granted_by AS ENUM ('SYSTEM', 'ADMIN');

CREATE TABLE user_badges (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    badge_id INT NOT NULL REFERENCES badges(id),
    granted_by badge_granted_by NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_badges UNIQUE (user_id, badge_id)
);

CREATE INDEX idx_user_badges_user ON user_badges (user_id);

-- ============================================================
-- 51. pc_builds (PC 견적)
-- ============================================================
CREATE TYPE build_purpose AS ENUM ('GAMING', 'OFFICE', 'DESIGN', 'DEVELOPMENT', 'STREAMING');

CREATE TABLE pc_builds (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    purpose build_purpose NOT NULL,
    budget INT NULL,
    total_price INT NOT NULL DEFAULT 0,
    share_code VARCHAR(20) UNIQUE NULL,
    is_public BOOLEAN NOT NULL DEFAULT false,
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_pc_builds_user ON pc_builds (user_id);
CREATE INDEX idx_pc_builds_public ON pc_builds (is_public, like_count DESC);

-- ============================================================
-- 52. build_parts (견적 부품)
-- ============================================================
CREATE TYPE part_type AS ENUM ('CPU', 'MOTHERBOARD', 'RAM', 'GPU', 'SSD', 'HDD', 'PSU', 'CASE', 'COOLER', 'MONITOR');

CREATE TABLE build_parts (
    id SERIAL PRIMARY KEY,
    build_id INT NOT NULL REFERENCES pc_builds(id),
    product_id INT NOT NULL REFERENCES products(id),
    seller_id INT NULL REFERENCES sellers(id),
    part_type part_type NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price_at_add INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_build_parts UNIQUE (build_id, part_type, product_id)
);

CREATE INDEX idx_build_parts_build ON build_parts (build_id);

-- ============================================================
-- 53. compatibility_rules (호환성 규칙)
-- ============================================================
CREATE TYPE compat_rule_type AS ENUM ('SOCKET', 'RAM_TYPE', 'FORM_FACTOR', 'POWER', 'SLOT');
CREATE TYPE compat_match_type AS ENUM ('EXACT', 'CONTAINS', 'RANGE');
CREATE TYPE compat_severity AS ENUM ('ERROR', 'WARNING', 'INFO');

CREATE TABLE compatibility_rules (
    id SERIAL PRIMARY KEY,
    rule_type compat_rule_type NOT NULL,
    part_type_a VARCHAR(20) NOT NULL,
    spec_key_a VARCHAR(50) NOT NULL,
    part_type_b VARCHAR(20) NOT NULL,
    spec_key_b VARCHAR(50) NOT NULL,
    match_type compat_match_type NOT NULL,
    error_message VARCHAR(200) NOT NULL,
    severity compat_severity NOT NULL DEFAULT 'ERROR',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_compatibility_rules_type ON compatibility_rules (rule_type);

-- ============================================================
-- 54. email_verifications (이메일 인증코드)
-- ============================================================
CREATE TYPE verification_type AS ENUM ('SIGNUP', 'PASSWORD_RESET');

CREATE TABLE email_verifications (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    type verification_type NOT NULL,
    code VARCHAR(6) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    is_used BOOLEAN NOT NULL DEFAULT false,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_email_verifications_user_type ON email_verifications (user_id, type);
CREATE INDEX idx_email_verifications_code ON email_verifications (user_id, code, type);
CREATE INDEX idx_email_verifications_expires ON email_verifications (expires_at);

-- ============================================================
-- 55. user_social_accounts (소셜 계정 연동)
-- ============================================================
CREATE TABLE user_social_accounts (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    provider VARCHAR(20) NOT NULL,
    social_id VARCHAR(255) NOT NULL,
    social_email VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_social_provider_id UNIQUE (provider, social_id)
);

CREATE INDEX idx_social_user ON user_social_accounts (user_id);

-- ============================================================
-- 56. friendships (친구/팔로우 관계)
-- ============================================================
CREATE TYPE friendship_status AS ENUM ('PENDING', 'ACCEPTED', 'BLOCKED');

CREATE TABLE friendships (
    id SERIAL PRIMARY KEY,
    requester_id INT NOT NULL REFERENCES users(id),
    addressee_id INT NOT NULL REFERENCES users(id),
    status friendship_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_friendships_pair UNIQUE (requester_id, addressee_id)
);

CREATE INDEX idx_friendships_addressee ON friendships (addressee_id);
CREATE INDEX idx_friendships_status ON friendships (status);

-- ============================================================
-- 57. short_forms (숏폼 영상)
-- ============================================================
CREATE TYPE short_form_status AS ENUM ('PROCESSING', 'ACTIVE', 'DELETED');

CREATE TABLE short_forms (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    title VARCHAR(100) NOT NULL,
    video_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500) NULL,
    duration INT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    status short_form_status NOT NULL DEFAULT 'PROCESSING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_short_forms_user ON short_forms (user_id);
CREATE INDEX idx_short_forms_popular ON short_forms (like_count, view_count);
CREATE INDEX idx_short_forms_created ON short_forms (created_at);

-- ============================================================
-- 58. short_form_products (숏폼-상품 태깅)
-- ============================================================
CREATE TABLE short_form_products (
    id SERIAL PRIMARY KEY,
    short_form_id INT NOT NULL REFERENCES short_forms(id),
    product_id INT NOT NULL REFERENCES products(id),
    display_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_sfp_shortform ON short_form_products (short_form_id);

-- ============================================================
-- 59. short_form_likes (숏폼 좋아요)
-- ============================================================
CREATE TABLE short_form_likes (
    id SERIAL PRIMARY KEY,
    short_form_id INT NOT NULL REFERENCES short_forms(id),
    user_id INT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_sf_likes UNIQUE (short_form_id, user_id)
);

-- ============================================================
-- 60. attachments (통합 첨부파일)
-- ============================================================
CREATE TYPE file_type AS ENUM ('IMAGE', 'VIDEO', 'AUDIO', 'DOCUMENT');

CREATE TABLE attachments (
    id SERIAL PRIMARY KEY,
    owner_id INT NOT NULL,
    owner_type VARCHAR(30) NOT NULL,
    file_type file_type NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(50) NOT NULL,
    file_size INT NOT NULL,
    metadata JSONB NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attachments_owner ON attachments (owner_type, owner_id);
CREATE INDEX idx_attachments_type ON attachments (file_type);

-- ============================================================
-- 61. news_categories (뉴스 카테고리)
-- ============================================================
CREATE TABLE news_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    slug VARCHAR(30) UNIQUE NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 62. news_articles (뉴스 게시글)
-- ============================================================
CREATE TABLE news_articles (
    id SERIAL PRIMARY KEY,
    category_id INT NOT NULL REFERENCES news_categories(id),
    author_id INT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    thumbnail_url VARCHAR(500) NULL,
    view_count INT NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_news_category ON news_articles (category_id);
CREATE INDEX idx_news_created ON news_articles (created_at);

-- ============================================================
-- 63. news_related_products (뉴스-상품 매핑)
-- ============================================================
CREATE TABLE news_related_products (
    id SERIAL PRIMARY KEY,
    news_id INT NOT NULL REFERENCES news_articles(id),
    product_id INT NOT NULL REFERENCES products(id),
    display_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_nrp_news ON news_related_products (news_id);

-- ============================================================
-- 64. product_mappings (크롤링 상품 매핑)
-- ============================================================
CREATE TYPE mapping_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

CREATE TABLE product_mappings (
    id SERIAL PRIMARY KEY,
    crawled_product_name VARCHAR(300) NOT NULL,
    extracted_model VARCHAR(100) NULL,
    seller_id INT NOT NULL REFERENCES sellers(id),
    product_id INT NULL REFERENCES products(id),
    status mapping_status NOT NULL DEFAULT 'PENDING',
    confidence DECIMAL(3,2) NULL,
    reviewed_by INT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mappings_status ON product_mappings (status);
CREATE INDEX idx_mappings_seller ON product_mappings (seller_id);
CREATE INDEX idx_mappings_product ON product_mappings (product_id);

-- ============================================================
-- 65. fraud_alerts (이상 가격 알림)
-- ============================================================
CREATE TYPE fraud_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

CREATE TABLE fraud_alerts (
    id SERIAL PRIMARY KEY,
    price_entry_id INT NOT NULL REFERENCES price_entries(id),
    product_id INT NOT NULL REFERENCES products(id),
    seller_id INT NOT NULL REFERENCES sellers(id),
    detected_price INT NOT NULL,
    average_price INT NOT NULL,
    deviation_percent DECIMAL(5,2) NOT NULL,
    status fraud_status NOT NULL DEFAULT 'PENDING',
    reviewed_by INT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fraud_status ON fraud_alerts (status);
CREATE INDEX idx_fraud_product ON fraud_alerts (product_id);

-- ============================================================
-- 66. review_images (리뷰 이미지)
-- ============================================================
CREATE TABLE review_images (
    id SERIAL PRIMARY KEY,
    review_id INT NOT NULL REFERENCES reviews(id),
    image_url VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_review_images_review ON review_images (review_id);

-- ============================================================
-- 67. review_tags (리뷰 태그)
-- ============================================================
CREATE TABLE review_tags (
    id SERIAL PRIMARY KEY,
    review_id INT NOT NULL REFERENCES reviews(id),
    tag VARCHAR(30) NOT NULL
);

CREATE INDEX idx_review_tags_review ON review_tags (review_id);
CREATE INDEX idx_review_tags_tag ON review_tags (tag);

-- ============================================================
-- 68. used_prices (중고 시세)
-- ============================================================
CREATE TABLE used_prices (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(id),
    average_price INT NOT NULL,
    min_price INT NOT NULL,
    max_price INT NOT NULL,
    sample_count INT NOT NULL,
    source VARCHAR(50) NOT NULL,
    collected_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_used_prices_product ON used_prices (product_id, collected_at);

-- ============================================================
-- 69. car_models (자동차 모델)
-- ============================================================
CREATE TYPE car_type AS ENUM ('SEDAN', 'SUV', 'HATCHBACK', 'TRUCK', 'VAN', 'EV');

CREATE TABLE car_models (
    id SERIAL PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    type car_type NOT NULL,
    year INT NOT NULL,
    base_price INT NOT NULL,
    image_url VARCHAR(500) NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_car_models_brand ON car_models (brand);
CREATE INDEX idx_car_models_type ON car_models (type);

-- ============================================================
-- 70. lease_offers (렌트/리스 조건)
-- ============================================================
CREATE TYPE lease_type AS ENUM ('RENT', 'LEASE', 'FINANCE');

CREATE TABLE lease_offers (
    id SERIAL PRIMARY KEY,
    car_model_id INT NOT NULL REFERENCES car_models(id),
    company VARCHAR(50) NOT NULL,
    type lease_type NOT NULL,
    monthly_payment INT NOT NULL,
    deposit INT NOT NULL DEFAULT 0,
    contract_months INT NOT NULL,
    annual_mileage INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lease_offers_model ON lease_offers (car_model_id);

-- ============================================================
-- 71. auctions (역경매)
-- ============================================================
CREATE TYPE auction_status AS ENUM ('OPEN', 'CLOSED', 'CANCELLED', 'COMPLETED');

CREATE TABLE auctions (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    category_id INT NOT NULL REFERENCES categories(id),
    budget INT NULL,
    status auction_status NOT NULL DEFAULT 'OPEN',
    bid_count INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auctions_user ON auctions (user_id);
CREATE INDEX idx_auctions_status ON auctions (status);
CREATE INDEX idx_auctions_expires ON auctions (expires_at);

-- ============================================================
-- 72. bids (역경매 입찰)
-- ============================================================
CREATE TABLE bids (
    id SERIAL PRIMARY KEY,
    auction_id INT NOT NULL REFERENCES auctions(id),
    seller_id INT NOT NULL REFERENCES sellers(id),
    price INT NOT NULL,
    description TEXT NULL,
    delivery_days INT NOT NULL,
    is_selected BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bids_auction ON bids (auction_id);
CREATE INDEX idx_bids_seller ON bids (seller_id);

-- ============================================================
-- 73. admin_settings (관리자 시스템 설정)
-- ============================================================
CREATE TABLE admin_settings (
    id SERIAL PRIMARY KEY,
    setting_key VARCHAR(50) UNIQUE NOT NULL,
    setting_value JSONB NOT NULL,
    description VARCHAR(200) NULL,
    updated_by INT NULL REFERENCES users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 완료: 73개 테이블 생성 완료
-- ============================================================
