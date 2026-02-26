-- ============================================================
-- NestShop 통합 샘플 데이터 (PostgreSQL)
-- 목적: 주요 기능 API를 바로 테스트할 수 있는 최소-충분 데이터 세트
-- 비밀번호(공통): Password1!
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- 0) 기존 데이터 초기화 (테이블이 없으면 자동 스킵)
-- ------------------------------------------------------------
DO $$
DECLARE
  t text;
BEGIN
  FOREACH t IN ARRAY ARRAY[
    'admin_settings','bids','auctions','lease_offers','car_models','used_prices','review_tags','review_images',
    'fraud_alerts','product_mappings','news_related_products','news_articles','news_categories','attachments',
    'short_form_likes','short_form_products','short_forms','friendships','user_social_accounts','email_verifications',
    'compatibility_rules','build_parts','pc_builds','user_badges','badges','image_variants','exchange_rates',
    'translations','seller_reviews','seller_trust_metrics','push_notifications','push_subscriptions','crawler_logs',
    'crawler_jobs','search_synonyms','search_logs','deal_products','deals','recommendations','chat_messages',
    'chat_rooms','search_history','view_history','notices','faqs','ticket_replies','support_tickets','inquiries',
    'post_likes','comments','posts','boards','point_transactions','wishlists','reviews','payments','order_items',
    'orders','addresses','cart_items','price_predictions','price_alerts','price_history','price_entries','sellers',
    'spec_scores','product_specs','spec_definitions','product_images','product_options','products','categories','users'
  ]
  LOOP
    BEGIN
      EXECUTE format('TRUNCATE TABLE %I RESTART IDENTITY CASCADE', t);
    EXCEPTION WHEN undefined_table THEN
      CONTINUE;
    END;
  END LOOP;
END $$;

-- ------------------------------------------------------------
-- 1) 사용자
-- ------------------------------------------------------------
INSERT INTO users (
  id,email,password,name,phone,role,status,email_verified,email_verified_at,nickname,bio,profile_image_url,
  search_history_enabled,point,preferred_locale,preferred_currency,refresh_token
) VALUES
(1,'admin@nestshop.com','$2b$10$3PNYJnWpmZ8Bb3rGqodoxusFvdGAMCPrwZgOdnnoOs.TYbM8s1TUG','관리자','01012340001','ADMIN','ACTIVE',true,NOW(),'admin01','시스템 관리자',NULL,true,100000,'ko','KRW',NULL),
(2,'seller1@nestshop.com','$2b$10$3PNYJnWpmZ8Bb3rGqodoxusFvdGAMCPrwZgOdnnoOs.TYbM8s1TUG','셀러원','01012340002','SELLER','ACTIVE',true,NOW(),'seller01','공식 판매자',NULL,true,12000,'ko','KRW',NULL),
(3,'seller2@nestshop.com','$2b$10$3PNYJnWpmZ8Bb3rGqodoxusFvdGAMCPrwZgOdnnoOs.TYbM8s1TUG','셀러투','01012340003','SELLER','ACTIVE',true,NOW(),'seller02','전문 판매자',NULL,true,15000,'ko','KRW',NULL),
(4,'user1@nestshop.com','$2b$10$3PNYJnWpmZ8Bb3rGqodoxusFvdGAMCPrwZgOdnnoOs.TYbM8s1TUG','홍길동','01012345678','USER','ACTIVE',true,NOW(),'hong01','게이밍 유저',NULL,true,53000,'ko','KRW',NULL),
(5,'user2@nestshop.com','$2b$10$3PNYJnWpmZ8Bb3rGqodoxusFvdGAMCPrwZgOdnnoOs.TYbM8s1TUG','김영희','01023456789','USER','ACTIVE',true,NOW(),'kim02','사무용 유저',NULL,true,27000,'ko','KRW',NULL),
(6,'user3@nestshop.com','$2b$10$3PNYJnWpmZ8Bb3rGqodoxusFvdGAMCPrwZgOdnnoOs.TYbM8s1TUG','이철수','01034567890','USER','INACTIVE',false,NULL,'lee03','신규 가입자',NULL,true,0,'ko','KRW',NULL);

-- ------------------------------------------------------------
-- 2) 카테고리 / 상품 / 옵션 / 이미지
-- ------------------------------------------------------------
INSERT INTO categories (id,name,parent_id,sort_order) VALUES
(1,'컴퓨터',NULL,1),
(2,'노트북',1,1),
(3,'데스크탑',1,2),
(4,'자동차',NULL,2),
(5,'전기차',4,1);

INSERT INTO products (
  id,name,description,price,discount_price,stock,status,category_id,thumbnail_url,
  lowest_price,seller_count,view_count,review_count,average_rating,sales_count,popularity_score,version
) VALUES
(1,'게이밍 노트북 A15','RTX 탑재 게이밍 노트북',1890000,1790000,30,'ON_SALE',2,'https://img.example.com/p1-thumb.jpg',1720000,2,320,2,4.5,28,95.5,1),
(2,'사무용 노트북 Slim','가벼운 사무용 노트북',990000,NULL,50,'ON_SALE',2,'https://img.example.com/p2-thumb.jpg',940000,2,210,1,4.0,18,71.2,1),
(3,'미니 데스크탑 Pro','개발용 데스크탑',1450000,1390000,16,'ON_SALE',3,'https://img.example.com/p3-thumb.jpg',1360000,2,180,1,5.0,9,66.7,1),
(4,'그래픽카드 RTX 5070','고성능 GPU',980000,NULL,12,'ON_SALE',3,'https://img.example.com/p4-thumb.jpg',955000,2,140,0,0.0,4,58.2,1),
(5,'전기차 Model E','중형 전기차',56000000,54500000,8,'ON_SALE',5,'https://img.example.com/p5-thumb.jpg',54000000,1,95,0,0.0,2,44.1,1),
(6,'SUV Model X','대형 SUV',62000000,NULL,5,'ON_SALE',4,'https://img.example.com/p6-thumb.jpg',61500000,1,75,0,0.0,1,39.3,1);

INSERT INTO product_options (id,product_id,name,"values") VALUES
(1,1,'RAM','["16GB","32GB"]'),
(2,1,'SSD','["512GB","1TB"]'),
(3,2,'색상','["실버","스페이스그레이"]'),
(4,5,'트림','["스탠다드","롱레인지"]');

INSERT INTO product_images (id,product_id,url,is_main,sort_order,image_variant_id) VALUES
(1,1,'https://img.example.com/p1-1.jpg',true,1,NULL),
(2,1,'https://img.example.com/p1-2.jpg',false,2,NULL),
(3,2,'https://img.example.com/p2-1.jpg',true,1,NULL),
(4,5,'https://img.example.com/p5-1.jpg',true,1,NULL);

-- ------------------------------------------------------------
-- 3) 스펙
-- ------------------------------------------------------------
INSERT INTO spec_definitions (id,category_id,name,type,options,unit,is_comparable,data_type,sort_order) VALUES
(1,2,'CPU','TEXT',NULL,NULL,true,'STRING',1),
(2,2,'RAM','NUMBER',NULL,'GB',true,'NUMBER',2),
(3,2,'SSD','NUMBER',NULL,'GB',true,'NUMBER',3),
(4,3,'GPU','TEXT',NULL,NULL,true,'STRING',1),
(5,5,'주행거리','NUMBER',NULL,'km',true,'NUMBER',1);

INSERT INTO product_specs (id,product_id,spec_definition_id,value,numeric_value) VALUES
(1,1,1,'Intel i7-14700H',NULL),
(2,1,2,'16',16),
(3,1,3,'1024',1024),
(4,2,1,'Intel i5-13420H',NULL),
(5,2,2,'16',16),
(6,3,4,'RTX 4060',NULL),
(7,5,5,'520',520);

INSERT INTO spec_scores (id,spec_definition_id,value,score,benchmark_source) VALUES
(1,2,'8',55,'internal'),
(2,2,'16',82,'internal'),
(3,2,'32',95,'internal'),
(4,3,'512',70,'internal'),
(5,3,'1024',90,'internal');

-- ------------------------------------------------------------
-- 4) 판매처 / 가격 / 가격이력 / 알림 / 예측
-- ------------------------------------------------------------
INSERT INTO sellers (id,name,url,logo_url,trust_score,trust_grade,description,is_active) VALUES
(1,'공식몰','https://official.example.com','https://img.example.com/s1-logo.png',92,'A','제조사 공식 판매처',true),
(2,'테크마켓','https://techmarket.example.com','https://img.example.com/s2-logo.png',84,'B','IT 전문 판매처',true),
(3,'카월드','https://carworld.example.com','https://img.example.com/s3-logo.png',79,'B','자동차 전문 판매처',true);

INSERT INTO price_entries (
  id,product_id,seller_id,price,shipping_cost,shipping_info,product_url,shipping_fee,shipping_type,
  click_count,is_available,crawled_at
) VALUES
(1,1,1,1720000,0,'무료배송','https://official.example.com/p/1',0,'FREE',120,true,NOW()),
(2,1,2,1750000,3000,'기본배송','https://techmarket.example.com/p/1',3000,'PAID',98,true,NOW()),
(3,2,1,940000,0,'무료배송','https://official.example.com/p/2',0,'FREE',67,true,NOW()),
(4,2,2,955000,0,'조건부무료','https://techmarket.example.com/p/2',0,'CONDITIONAL',51,true,NOW()),
(5,3,1,1360000,0,'무료배송','https://official.example.com/p/3',0,'FREE',46,true,NOW()),
(6,4,2,955000,0,'무료배송','https://techmarket.example.com/p/4',0,'FREE',35,true,NOW()),
(7,5,3,54000000,0,'직배송','https://carworld.example.com/p/5',0,'FREE',12,true,NOW()),
(8,6,3,61500000,0,'직배송','https://carworld.example.com/p/6',0,'FREE',8,true,NOW());

INSERT INTO price_history (id,product_id,date,lowest_price,average_price,highest_price) VALUES
(1,1,CURRENT_DATE - INTERVAL '2 day',1760000,1785000,1810000),
(2,1,CURRENT_DATE - INTERVAL '1 day',1740000,1760000,1790000),
(3,1,CURRENT_DATE,1720000,1735000,1753000),
(4,2,CURRENT_DATE - INTERVAL '1 day',950000,960000,975000),
(5,2,CURRENT_DATE,940000,947500,955000);

INSERT INTO price_alerts (id,user_id,product_id,target_price,is_triggered,triggered_at,is_active) VALUES
(1,4,1,1700000,false,NULL,true),
(2,5,2,930000,false,NULL,true);

INSERT INTO price_predictions (
  id,product_id,prediction_date,predicted_price,confidence,trend,trend_strength,moving_avg_7d,moving_avg_30d,
  recommendation,seasonality_note,calculated_at
) VALUES
(1,1,CURRENT_DATE + INTERVAL '7 day',1690000,0.82,'FALLING',0.61,1740000,1785000,'BUY_SOON','신학기 시즌 할인 가능성',NOW()),
(2,2,CURRENT_DATE + INTERVAL '7 day',945000,0.74,'STABLE',0.22,948000,952000,'WAIT','가격 안정 구간',NOW());

-- ------------------------------------------------------------
-- 5) 장바구니 / 배송지 / 주문 / 결제 / 리뷰 / 위시리스트 / 포인트
-- ------------------------------------------------------------
INSERT INTO cart_items (id,user_id,product_id,seller_id,selected_options,quantity) VALUES
(1,4,1,1,'RAM:16GB,SSD:1TB',1),
(2,4,2,2,'색상:실버',1),
(3,5,3,1,NULL,1);

INSERT INTO addresses (id,user_id,label,recipient_name,phone,zip_code,address,address_detail,is_default) VALUES
(1,4,'집','홍길동','01012345678','06236','서울시 강남구 테헤란로 123','101동 1001호',true),
(2,4,'회사','홍길동','01012345678','04782','서울시 성동구 왕십리로 222','20층',false),
(3,5,'집','김영희','01023456789','48058','부산시 해운대구 센텀중앙로 77','1203호',true);

INSERT INTO orders (
  id,order_number,user_id,status,total_amount,point_used,final_amount,recipient_name,recipient_phone,zip_code,address,address_detail,memo,version
) VALUES
(1,'ORD-20260225-A001',4,'DELIVERED',2660000,20000,2640000,'홍길동','01012345678','06236','서울시 강남구 테헤란로 123','101동 1001호','문 앞에 놓아주세요',1),
(2,'ORD-20260225-A002',5,'PAYMENT_CONFIRMED',1360000,0,1360000,'김영희','01023456789','48058','부산시 해운대구 센텀중앙로 77','1203호','빠른 배송 요청',1);

INSERT INTO order_items (
  id,order_id,product_id,seller_id,product_name,seller_name,selected_options,quantity,unit_price,total_price,is_reviewed
) VALUES
(1,1,1,1,'게이밍 노트북 A15','공식몰','RAM:16GB,SSD:1TB',1,1720000,1720000,true),
(2,1,2,2,'사무용 노트북 Slim','테크마켓','색상:실버',1,940000,940000,false),
(3,2,3,1,'미니 데스크탑 Pro','공식몰',NULL,1,1360000,1360000,true);

INSERT INTO payments (id,order_id,method,amount,status,paid_at,refunded_at) VALUES
(1,1,'CARD',2640000,'COMPLETED',NOW() - INTERVAL '1 day',NULL),
(2,2,'BANK_TRANSFER',1360000,'COMPLETED',NOW() - INTERVAL '3 hour',NULL);

INSERT INTO reviews (id,user_id,product_id,order_id,rating,content,is_best) VALUES
(1,4,1,1,5,'성능이 매우 좋고 발열 관리도 괜찮습니다.',true),
(2,5,3,2,4,'개발용으로 충분한 성능입니다.',false);

INSERT INTO wishlists (id,user_id,product_id) VALUES
(1,4,4),
(2,4,5),
(3,5,1);

INSERT INTO point_transactions (id,user_id,type,amount,balance,description,reference_type,reference_id,expires_at) VALUES
(1,4,'EARN',5000,5000,'리뷰 작성 적립','REVIEW',1,NULL),
(2,4,'USE',20000,53000,'주문 시 포인트 사용','ORDER',1,NULL),
(3,5,'ADMIN_GRANT',27000,27000,'운영자 지급','ADMIN',1,NULL);

-- ------------------------------------------------------------
-- 6) 커뮤니티 / 문의 / FAQ / 공지 / 활동 / 채팅
-- ------------------------------------------------------------
INSERT INTO boards (id,name,slug,description,sort_order,is_active) VALUES
(1,'자유게시판','free','자유롭게 이야기하는 공간',1,true),
(2,'구매후기','reviews','구매 경험 공유',2,true);

INSERT INTO posts (id,board_id,user_id,title,content,view_count,like_count,comment_count) VALUES
(1,1,4,'노트북 추천 부탁드립니다','예산 150만원대 노트북 추천 부탁해요.',45,3,2),
(2,2,5,'미니 데스크탑 사용기','조용하고 성능 좋아서 만족합니다.',23,2,1);

INSERT INTO comments (id,post_id,user_id,parent_id,content) VALUES
(1,1,5,NULL,'A15 모델 추천합니다.'),
(2,1,4,1,'감사합니다! 참고할게요.');

INSERT INTO post_likes (id,post_id,user_id) VALUES
(1,1,5);

INSERT INTO inquiries (id,product_id,user_id,title,content,is_secret,answer,answered_by,answered_at) VALUES
(1,1,4,'램 업그레이드 가능 여부','기본 16GB에서 추후 32GB 업그레이드 가능한가요?',false,'네, 공식 서비스센터에서 업그레이드 가능합니다.',1,NOW() - INTERVAL '2 hour');

INSERT INTO support_tickets (id,ticket_number,user_id,category,title,content,status,attachment_urls) VALUES
(1,'TCK-20260225-0001',4,'DELIVERY','배송 지연 문의','주문한 상품 배송이 지연되고 있습니다.','IN_PROGRESS','["https://img.example.com/ticket1.png"]');

INSERT INTO ticket_replies (id,ticket_id,user_id,content,is_admin) VALUES
(1,1,4,'확인 부탁드립니다.',false),
(2,1,1,'현재 물류센터 출고 대기 중이며 내일 출고 예정입니다.',true);

INSERT INTO faqs (id,category,question,answer,sort_order,is_active) VALUES
(1,'ORDER','주문 취소는 어떻게 하나요?','마이페이지 > 주문내역에서 취소 가능합니다.',1,true),
(2,'PAYMENT','환불은 언제 처리되나요?','결제수단에 따라 1~5영업일 소요됩니다.',2,true);

INSERT INTO notices (id,title,content,is_pinned,view_count) VALUES
(1,'[점검 안내] 2/28 새벽 시스템 점검','2/28 02:00~04:00 서비스 점검 예정입니다.',true,102),
(2,'배송 정책 변경 안내','무료배송 기준이 일부 변경됩니다.',false,58);

INSERT INTO view_history (id,user_id,product_id,viewed_at) VALUES
(1,4,1,NOW() - INTERVAL '30 minute'),
(2,4,4,NOW() - INTERVAL '10 minute'),
(3,5,3,NOW() - INTERVAL '1 hour');

INSERT INTO search_history (id,user_id,keyword,searched_at) VALUES
(1,4,'게이밍 노트북',NOW() - INTERVAL '20 minute'),
(2,4,'RTX 5070',NOW() - INTERVAL '15 minute'),
(3,5,'미니 데스크탑',NOW() - INTERVAL '50 minute');

INSERT INTO chat_rooms (id,user_id,admin_id,status,last_message_at,closed_at) VALUES
(1,4,1,'ACTIVE',NOW() - INTERVAL '5 minute',NULL);

INSERT INTO chat_messages (id,room_id,sender_id,content,is_read,created_at) VALUES
(1,1,4,'배송 관련 문의드립니다.',true,NOW() - INTERVAL '6 minute'),
(2,1,1,'확인 후 안내드리겠습니다.',false,NOW() - INTERVAL '5 minute');

-- ------------------------------------------------------------
-- 7) 추천 / 특가 / 검색 / 크롤러 / 푸시
-- ------------------------------------------------------------
INSERT INTO recommendations (id,product_id,type,sort_order,start_date,end_date) VALUES
(1,1,'TODAY',1,CURRENT_DATE - 1,CURRENT_DATE + 7),
(2,3,'EDITOR_PICK',1,CURRENT_DATE - 3,CURRENT_DATE + 10),
(3,5,'NEW_ARRIVAL',1,CURRENT_DATE - 1,CURRENT_DATE + 14);

INSERT INTO deals (id,title,type,description,discount_rate,banner_url,start_date,end_date,is_active) VALUES
(1,'노트북 특가전','SPECIAL','인기 노트북 한정 특가',8,'https://img.example.com/deal1.jpg',NOW() - INTERVAL '1 day',NOW() + INTERVAL '5 day',true);

INSERT INTO deal_products (id,deal_id,product_id,deal_price,stock,sold_count) VALUES
(1,1,1,1690000,20,5),
(2,1,2,920000,30,9);

INSERT INTO search_logs (id,user_id,keyword,result_count,category_id,filters,response_time_ms,searched_at) VALUES
(1,4,'게이밍 노트북',12,2,'{"sort":"popularity"}',120,NOW() - INTERVAL '30 minute'),
(2,4,'RTX 5070',4,3,'{"minPrice":900000}',98,NOW() - INTERVAL '22 minute'),
(3,5,'미니 데스크탑',6,3,'{"sort":"newest"}',110,NOW() - INTERVAL '1 hour');

INSERT INTO search_synonyms (id,word,synonyms,is_active) VALUES
(1,'그래픽카드','["GPU","비디오카드"]',true),
(2,'노트북','["랩탑","Laptop"]',true);

INSERT INTO crawler_jobs (id,name,seller_id,target_url,category_id,schedule,parser_type,config,status,is_active,last_run_at) VALUES
(1,'공식몰 노트북 크롤러',1,'https://official.example.com/laptops',2,'0 */2 * * *','css','{"retry":2}', 'IDLE', true, NOW() - INTERVAL '3 hour'),
(2,'테크마켓 GPU 크롤러',2,'https://techmarket.example.com/gpu',3,'30 */3 * * *','xpath','{"retry":3}', 'FAILED', true, NOW() - INTERVAL '6 hour');

INSERT INTO crawler_logs (id,job_id,status,started_at,finished_at,duration_ms,items_processed,items_created,items_updated,items_failed,error_message) VALUES
(1,1,'SUCCESS',NOW() - INTERVAL '3 hour',NOW() - INTERVAL '2 hour 58 minute',120000,24,3,16,0,NULL),
(2,2,'FAILED',NOW() - INTERVAL '6 hour',NOW() - INTERVAL '5 hour 59 minute',60000,10,0,2,8,'페이지 파싱 실패');

INSERT INTO push_subscriptions (id,user_id,endpoint,p256dh,auth,device_name,is_active,last_used_at) VALUES
(1,4,'https://push.example.com/sub/abc123','p256dh_key_value','auth_key_value','Chrome-Windows',true,NOW() - INTERVAL '1 day');

INSERT INTO push_notifications (id,user_id,title,body,url,icon_url,type,is_read,sent_at,read_at) VALUES
(1,4,'가격 알림','관심 상품 가격이 하락했습니다.','/products/1',NULL,'PRICE_ALERT',false,NOW() - INTERVAL '5 minute',NULL),
(2,4,'특가 알림','노트북 특가전이 시작되었습니다.','/products',NULL,'DEAL',true,NOW() - INTERVAL '1 day',NOW() - INTERVAL '23 hour');

-- ------------------------------------------------------------
-- 8) 신뢰도 / 다국어 / 이미지 / 배지 / PC견적
-- ------------------------------------------------------------
INSERT INTO seller_trust_metrics (
  id,seller_id,delivery_score,price_accuracy,return_rate,response_time_hours,review_score,order_count,dispute_rate,overall_score,grade,trend,calculated_at
) VALUES
(1,1,92,95,1.20,2.5,4.8,1200,0.3,94,'A','IMPROVING',NOW()),
(2,2,84,88,2.10,4.0,4.4,860,0.8,86,'B','STABLE',NOW()),
(3,3,79,81,2.80,5.2,4.1,430,1.4,80,'B','STABLE',NOW());

INSERT INTO seller_reviews (id,seller_id,user_id,order_id,rating,delivery_rating,content) VALUES
(1,1,4,1,5,5,'배송이 빠르고 포장 상태가 좋았습니다.'),
(2,1,5,2,4,4,'가격 경쟁력이 좋아요.');

INSERT INTO translations (id,locale,namespace,key,value) VALUES
(1,'ko','common','welcome','환영합니다'),
(2,'en','common','welcome','Welcome'),
(3,'ko','product','lowest_price','최저가'),
(4,'en','product','lowest_price','Lowest Price');

INSERT INTO exchange_rates (id,base_currency,target_currency,rate,source,fetched_at) VALUES
(1,'KRW','USD',0.00075,'mock-api',NOW()),
(2,'USD','KRW',1330.250000,'mock-api',NOW());

INSERT INTO image_variants (id,original_url,variant_type,url,format,width,height,file_size,processing_status,category) VALUES
(1,'https://img.example.com/p1-1.jpg','THUMBNAIL','https://img.example.com/p1-1-thumb.webp','WEBP',320,240,45231,'COMPLETED','product'),
(2,'https://img.example.com/p2-1.jpg','MEDIUM','https://img.example.com/p2-1-medium.webp','WEBP',800,600,112345,'COMPLETED','product');

UPDATE product_images SET image_variant_id = 1 WHERE id = 1;
UPDATE product_images SET image_variant_id = 2 WHERE id = 3;

INSERT INTO badges (id,name,description,icon_url,type,condition,rarity,holder_count,is_active) VALUES
(1,'첫 구매','첫 주문 완료 사용자','https://img.example.com/badge-first.png','AUTO','{"orders":1}','COMMON',2,true),
(2,'리뷰 장인','리뷰 10개 이상 작성','https://img.example.com/badge-review.png','AUTO','{"reviews":10}','RARE',1,true),
(3,'운영자 픽','운영자 수동 지급','https://img.example.com/badge-admin.png','MANUAL',NULL,'EPIC',1,true);

INSERT INTO user_badges (id,user_id,badge_id,granted_by) VALUES
(1,4,1,'SYSTEM'),
(2,4,3,'ADMIN'),
(3,5,1,'SYSTEM');

INSERT INTO pc_builds (id,user_id,name,description,purpose,budget,total_price,share_code,is_public,view_count,like_count,deleted_at) VALUES
(1,4,'FHD 게이밍 견적','가성비 게이밍 빌드','GAMING',2500000,2355000,'PCBUILD001',true,45,7,NULL),
(2,5,'개발용 저소음 견적','도커/IDE 작업용','DEVELOPMENT',1800000,1680000,'PCBUILD002',false,8,1,NULL);

INSERT INTO build_parts (id,build_id,product_id,seller_id,part_type,quantity,price_at_add) VALUES
(1,1,1,1,'CPU',1,420000),
(2,1,4,2,'GPU',1,955000),
(3,1,3,1,'SSD',1,180000),
(4,2,3,1,'CPU',1,380000);

INSERT INTO compatibility_rules (
  id,rule_type,part_type_a,spec_key_a,part_type_b,spec_key_b,match_type,error_message,severity,is_active
) VALUES
(1,'SOCKET','CPU','socket','MOTHERBOARD','socket','EXACT','CPU와 메인보드 소켓이 호환되지 않습니다.','ERROR',true),
(2,'POWER','GPU','power','PSU','watt','RANGE','GPU 권장 전력 대비 PSU 용량이 부족합니다.','WARNING',true);

-- ------------------------------------------------------------
-- 9) 인증/소셜/친구/숏폼/미디어/뉴스/매핑/사기탐지
-- ------------------------------------------------------------
INSERT INTO email_verifications (id,user_id,type,code,attempt_count,is_used,expires_at) VALUES
(1,6,'SIGNUP','123456',0,false,NOW() + INTERVAL '10 minute'),
(2,4,'PASSWORD_RESET','654321',1,false,NOW() + INTERVAL '5 minute');

INSERT INTO user_social_accounts (id,user_id,provider,social_id,social_email) VALUES
(1,4,'google','google_10001','user1@nestshop.com'),
(2,5,'kakao','kakao_20002',NULL);

INSERT INTO friendships (id,requester_id,addressee_id,status) VALUES
(1,4,5,'ACCEPTED'),
(2,5,6,'PENDING');

INSERT INTO short_forms (id,user_id,title,video_url,thumbnail_url,duration,view_count,like_count,comment_count,status,deleted_at) VALUES
(1,4,'게이밍 노트북 언박싱','https://cdn.example.com/short/1.mp4','https://cdn.example.com/short/1.jpg',45,1200,95,12,'ACTIVE',NULL),
(2,5,'데스크탑 조립 팁','https://cdn.example.com/short/2.mp4','https://cdn.example.com/short/2.jpg',60,860,64,7,'ACTIVE',NULL);

INSERT INTO short_form_products (id,short_form_id,product_id,display_order) VALUES
(1,1,1,1),
(2,2,3,1);

INSERT INTO short_form_likes (id,short_form_id,user_id) VALUES
(1,1,5),
(2,2,4);

INSERT INTO attachments (id,owner_id,owner_type,file_type,original_name,storage_path,mime_type,file_size,metadata) VALUES
(1,1,'support_ticket','IMAGE','ticket1.png','/uploads/ticket1.png','image/png',324455,'{"width":1200,"height":900}'),
(2,1,'short_form','VIDEO','short1.mp4','/uploads/short1.mp4','video/mp4',12204455,'{"duration":45}');

INSERT INTO news_categories (id,name,slug,sort_order,is_active) VALUES
(1,'테크뉴스','tech',1,true),
(2,'자동차뉴스','auto',2,true);

INSERT INTO news_articles (id,category_id,author_id,title,content,thumbnail_url,view_count,is_published,deleted_at) VALUES
(1,1,1,'차세대 GPU 출시 소식','신제품 GPU가 공식 발표되었습니다.','https://img.example.com/news1.jpg',220,true,NULL),
(2,2,1,'전기차 보조금 개편','2026년 전기차 보조금 정책이 변경됩니다.','https://img.example.com/news2.jpg',180,true,NULL);

INSERT INTO news_related_products (id,news_id,product_id,display_order) VALUES
(1,1,4,1),
(2,2,5,1);

INSERT INTO product_mappings (id,crawled_product_name,extracted_model,seller_id,product_id,status,confidence,reviewed_by) VALUES
(1,'A15 Gaming Laptop 2026','A15',2,1,'APPROVED',0.94,1),
(2,'Model E Long Range','Model E',3,5,'PENDING',0.71,NULL);

INSERT INTO fraud_alerts (id,price_entry_id,product_id,seller_id,detected_price,average_price,deviation_percent,status,reviewed_by) VALUES
(1,2,1,2,1753000,1835000,-4.47,'PENDING',NULL),
(2,6,4,2,955000,1030000,-7.28,'APPROVED',1);

INSERT INTO review_images (id,review_id,image_url,display_order) VALUES
(1,1,'https://img.example.com/review1-1.jpg',1),
(2,2,'https://img.example.com/review2-1.jpg',1);

INSERT INTO review_tags (id,review_id,tag) VALUES
(1,1,'가성비'),
(2,1,'성능좋음'),
(3,2,'조용함');

INSERT INTO used_prices (id,product_id,average_price,min_price,max_price,sample_count,source,collected_at) VALUES
(1,1,1280000,1100000,1450000,38,'community-market',NOW() - INTERVAL '1 day'),
(2,3,980000,860000,1120000,24,'community-market',NOW() - INTERVAL '1 day');

INSERT INTO car_models (id,brand,name,type,year,base_price,image_url,is_active) VALUES
(1,'NEST','Model E','EV',2026,56000000,'https://img.example.com/car1.jpg',true),
(2,'NEST','Model X','SUV',2026,62000000,'https://img.example.com/car2.jpg',true);

INSERT INTO lease_offers (id,car_model_id,company,type,monthly_payment,deposit,contract_months,annual_mileage,is_active) VALUES
(1,1,'렌트원','RENT',590000,3000000,48,20000,true),
(2,1,'리스플랜','LEASE',620000,2000000,60,15000,true),
(3,2,'렌트원','RENT',710000,5000000,48,20000,true);

INSERT INTO auctions (id,user_id,title,description,category_id,budget,status,bid_count,expires_at) VALUES
(1,4,'게이밍 PC 견적 요청','RTX급 게이밍 PC 견적을 원합니다.',3,2200000,'OPEN',2,NOW() + INTERVAL '3 day');

INSERT INTO bids (id,auction_id,seller_id,price,description,delivery_days,is_selected) VALUES
(1,1,1,2140000,'정품 부품 구성, 1년 A/S',3,false),
(2,1,2,2090000,'조립 + 케이블 정리 포함',4,true);

INSERT INTO admin_settings (id,setting_key,setting_value,description,updated_by) VALUES
(1,'upload_limits','{"imageMb":10,"videoMb":200}','업로드 제한 설정',1),
(2,'review_policy','{"minOrderDays":1,"maxImages":5}','리뷰 정책',1),
(3,'extensions','{"featureFlags":{"opsDashboard":true,"searchOutbox":true}}','확장 모듈 플래그',1);

COMMIT;

-- ============================================================
-- 샘플 계정
-- admin@nestshop.com / Password1!
-- user1@nestshop.com / Password1!
-- seller1@nestshop.com / Password1!
-- ============================================================
