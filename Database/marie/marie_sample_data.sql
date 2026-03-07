-- Project Bible Shop
-- MariaDB baseline sample data aligned to the MySQL/PostgreSQL reference dataset.

USE pbdb;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE user_badges;
TRUNCATE TABLE badges;
TRUNCATE TABLE news_products;
TRUNCATE TABLE news;
TRUNCATE TABLE media_assets;
TRUNCATE TABLE image_variants;
TRUNCATE TABLE image_assets;
TRUNCATE TABLE trust_score_histories;
TRUNCATE TABLE fraud_flags;
TRUNCATE TABLE search_index_outbox;
TRUNCATE TABLE product_query_views;
TRUNCATE TABLE compare_items;
TRUNCATE TABLE crawler_runs;
TRUNCATE TABLE crawler_jobs;
TRUNCATE TABLE push_preferences;
TRUNCATE TABLE push_subscriptions;
TRUNCATE TABLE chat_messages;
TRUNCATE TABLE chat_room_members;
TRUNCATE TABLE chat_rooms;
TRUNCATE TABLE reviews;
TRUNCATE TABLE payments;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE addresses;
TRUNCATE TABLE cart_items;
TRUNCATE TABLE wishlist_items;
TRUNCATE TABLE deals;
TRUNCATE TABLE price_entries;
TRUNCATE TABLE product_specs;
TRUNCATE TABLE products;
TRUNCATE TABLE sellers;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;
TRUNCATE TABLE system_settings;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (id, email, password, name, nickname, role, status, phone, bio) VALUES
  (1, 'admin@pbshop.com', '$2y$12$demo-hash-admin', 'PBShop Admin', 'admin', 'ADMIN', 'ACTIVE', '010-1111-1111', 'platform admin'),
  (2, 'user1@pbshop.com', '$2y$12$demo-hash-user1', 'Kim Buyer', 'buyer1', 'USER', 'ACTIVE', '010-2222-2222', 'shopper profile'),
  (3, 'user2@pbshop.com', '$2y$12$demo-hash-user2', 'Lee Seller', 'sellerfan', 'USER', 'ACTIVE', '010-3333-3333', 'reviewer profile');

INSERT INTO categories (id, parent_id, name, slug, depth, sort_order, is_visible) VALUES
  (1, NULL, '컴퓨터/노트북/조립PC', 'computer', 0, 1, 1),
  (2, 1, '노트북', 'notebook', 1, 1, 1),
  (3, 1, '데스크탑', 'desktop', 1, 2, 1),
  (4, NULL, '가전/TV', 'appliance-tv', 0, 2, 1),
  (5, 4, 'TV', 'tv', 1, 1, 1);

INSERT INTO sellers (id, name, code, status, rating, contact_email, homepage_url) VALUES
  (1, 'PB Mall', 'pbmall', 'ACTIVE', 4.80, 'cs@pbmall.com', 'https://pbmall.example.com'),
  (2, 'Nest Price', 'nestprice', 'ACTIVE', 4.50, 'help@nestprice.com', 'https://nestprice.example.com');

INSERT INTO products (id, category_id, name, slug, description, brand, status, thumbnail_url, rating_avg, review_count) VALUES
  (1, 2, '게이밍 노트북 A15', 'gaming-notebook-a15', 'RTX 기반 게이밍 노트북', 'PBGear', 'ACTIVE', 'https://picsum.photos/seed/pb-a15/320/240', 4.60, 12),
  (2, 3, '슬림 데스크탑 Mini', 'slim-desktop-mini', '사무용 미니 데스크탑', 'PBOffice', 'ACTIVE', 'https://picsum.photos/seed/pb-mini/320/240', 4.20, 4),
  (3, 5, 'OLED TV 55', 'oled-tv-55', '55인치 OLED 스마트 TV', 'ViewMax', 'ACTIVE', 'https://picsum.photos/seed/pb-tv/320/240', 4.90, 21);

INSERT INTO product_specs (product_id, spec_key, spec_value, sort_order) VALUES
  (1, 'CPU', 'Ryzen 7', 1),
  (1, 'GPU', 'RTX 4060', 2),
  (1, 'RAM', '16GB', 3),
  (2, 'CPU', 'Intel i5', 1),
  (2, 'RAM', '16GB', 2),
  (3, '패널', 'OLED', 1),
  (3, '해상도', '4K', 2);

INSERT INTO price_entries (id, product_id, seller_id, price, shipping_fee, is_card_discount, is_cash_discount, stock_status) VALUES
  (1, 1, 1, 1499000, 0, 1, 0, 'IN_STOCK'),
  (2, 1, 2, 1520000, 2500, 0, 1, 'IN_STOCK'),
  (3, 2, 1, 799000, 0, 1, 1, 'IN_STOCK'),
  (4, 3, 2, 1890000, 0, 0, 1, 'IN_STOCK');

INSERT INTO deals (id, product_id, title, description, discount_rate, start_at, end_at, is_active) VALUES
  (1, 1, '오늘의 특가', '게이밍 노트북 한정 특가', 12.50, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);

INSERT INTO wishlist_items (user_id, product_id) VALUES
  (2, 1),
  (2, 3);

INSERT INTO cart_items (user_id, product_id, quantity, unit_price) VALUES
  (2, 1, 1, 1499000),
  (2, 3, 1, 1890000);

INSERT INTO addresses (id, user_id, recipient_name, phone, zip_code, address1, address2, is_default) VALUES
  (1, 2, 'Kim Buyer', '010-2222-2222', '06236', '서울 강남구 테헤란로 1', '101동 1201호', 1);

INSERT INTO orders (id, user_id, address_id, order_number, status, total_amount, point_used, note) VALUES
  (1, 2, 1, 'ORD-20260306-0001', 'PAID', 1499000, 0, '문 앞 배송');

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
  (1, 1, 1, 1499000);

INSERT INTO payments (order_id, payment_key, provider, method, amount, status, approved_at) VALUES
  (1, 'pay_demo_0001', 'PBPAY', 'CARD', 1499000, 'DONE', NOW());

INSERT INTO reviews (user_id, product_id, rating, title, content) VALUES
  (2, 1, 5, '만족', '성능과 발열 균형이 좋습니다.'),
  (3, 3, 4, '화질 좋음', '거실용으로 충분히 만족스럽습니다.');

INSERT INTO chat_rooms (id, name, created_by, is_private) VALUES
  (1, '주문 문의', 2, 1);

INSERT INTO chat_room_members (room_id, user_id) VALUES
  (1, 2),
  (1, 1);

INSERT INTO chat_messages (room_id, sender_id, message) VALUES
  (1, 2, '배송 일정 문의드립니다.'),
  (1, 1, '내일 출고 예정입니다.');

INSERT INTO push_subscriptions (user_id, endpoint, p256dh_key, auth_key, expiration_time) VALUES
  (2, 'https://push.example.com/subscriptions/1', 'demo-p256dh-key', 'demo-auth-key', NULL);

INSERT INTO push_preferences (user_id, marketing_enabled, order_enabled, chat_enabled) VALUES
  (2, 0, 1, 1);

INSERT INTO crawler_jobs (id, seller_id, name, cron_expression, collect_price, collect_spec, detect_anomaly, is_active) VALUES
  (1, 1, 'PB Mall price crawler', '0 */6 * * *', 1, 1, 1, 1);

INSERT INTO crawler_runs (job_id, status, started_at, finished_at, summary_json) VALUES
  (1, 'SUCCESS', NOW(), NOW(), JSON_OBJECT('updatedPrices', 12, 'updatedSpecs', 3));

INSERT INTO system_settings (setting_key, setting_value, description) VALUES
  ('site.brandName', JSON_OBJECT('value', 'PBShop'), 'public brand name'),
  ('search.defaultSort', JSON_OBJECT('value', 'popular'), 'default product sort');

INSERT INTO fraud_flags (user_id, product_id, flag_type, score, detail_json) VALUES
  (NULL, 1, 'PRICE_ANOMALY', 72.50, JSON_OBJECT('source', 'crawler', 'reason', 'sudden price drop'));

INSERT INTO trust_score_histories (seller_id, score, reason) VALUES
  (1, 92.50, 'stable fulfillment history'),
  (2, 87.10, 'good review quality');

INSERT INTO image_assets (id, owner_type, owner_id, original_url, alt_text, sort_order) VALUES
  (1, 'product', 1, 'https://picsum.photos/seed/pb-a15-original/800/600', '게이밍 노트북 A15', 1);

INSERT INTO image_variants (image_id, type, size, url) VALUES
  (1, 'thumbnail', '320x240', 'https://picsum.photos/seed/pb-a15-thumb/320/240');

INSERT INTO media_assets (owner_type, owner_id, media_type, url, meta_json) VALUES
  ('product', 1, 'video', 'https://media.example.com/products/1/intro.mp4', JSON_OBJECT('durationSec', 45));

INSERT INTO news (id, title, summary, body, source_name, source_url, published_at) VALUES
  (1, '그래픽카드 시장 가격 안정화', '신제품 출시 이후 가격이 안정세를 보였습니다.', '뉴스 본문 예시', 'PB News', 'https://news.example.com/1', NOW());

INSERT INTO news_products (news_id, product_id) VALUES
  (1, 1);

INSERT INTO badges (id, name, code, description) VALUES
  (1, 'Early Adopter', 'EARLY_ADOPTER', '초기 구매 배지');

INSERT INTO user_badges (user_id, badge_id, granted_by_admin_id, reason) VALUES
  (2, 1, 1, '베타 테스트 참여');

INSERT INTO compare_items (user_id, product_id) VALUES
  (2, 1),
  (2, 2);

INSERT INTO product_query_views (product_id, search_keyword, hit_count, last_viewed_at) VALUES
  (1, '게이밍 노트북', 15, NOW()),
  (3, 'oled tv', 8, NOW());

INSERT INTO search_index_outbox (aggregate_type, aggregate_id, event_type, payload_json, status, retry_count) VALUES
  ('product', 1, 'PRODUCT_UPSERT', JSON_OBJECT('productId', 1), 'PENDING', 0);
