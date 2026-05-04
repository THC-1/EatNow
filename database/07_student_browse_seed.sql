USE eatnow;

-- Student browse module demo data
-- Run after 00-06 schema scripts

INSERT INTO school (name, code, address, description, status)
VALUES ('Demo University', 'DEMO_UNIV', '1 Demo Road', 'EatNow demo school', 'ACTIVE')
ON DUPLICATE KEY UPDATE
  address = VALUES(address),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO campus (school_id, name, address, description, status)
SELECT s.id, 'Main Campus', 'Demo University Town', 'Campus for student browse demo', 'ACTIVE'
FROM school s
WHERE s.code = 'DEMO_UNIV'
ON DUPLICATE KEY UPDATE
  address = VALUES(address),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO canteen (campus_id, name, type, location, description, opening_hours, contact_phone, average_score, status, sort_order)
SELECT c.id, 'First Canteen', 'CANTEEN', 'East Zone', 'Popular student canteen', '07:00-21:00', '010-10000001', 4.60, 'OPEN', 1
FROM campus c
WHERE c.name = 'Main Campus'
ON DUPLICATE KEY UPDATE
  location = VALUES(location),
  description = VALUES(description),
  opening_hours = VALUES(opening_hours),
  contact_phone = VALUES(contact_phone),
  average_score = VALUES(average_score),
  status = VALUES(status),
  sort_order = VALUES(sort_order),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO canteen (campus_id, name, type, location, description, opening_hours, contact_phone, average_score, status, sort_order)
SELECT c.id, 'Light Food Hub', 'CAMPUS_SHOP', 'North Library Side', 'Independent campus light food shop', '09:00-20:30', '010-10000002', 4.40, 'OPEN', 2
FROM campus c
WHERE c.name = 'Main Campus'
ON DUPLICATE KEY UPDATE
  location = VALUES(location),
  description = VALUES(description),
  opening_hours = VALUES(opening_hours),
  contact_phone = VALUES(contact_phone),
  average_score = VALUES(average_score),
  status = VALUES(status),
  sort_order = VALUES(sort_order),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO canteen (campus_id, name, type, location, description, opening_hours, contact_phone, average_score, status, sort_order)
SELECT c.id, 'South Gate Food Street', 'PERIPHERY_SHOP', 'Outside South Gate', 'Campus periphery partner merchants', '10:00-22:00', '010-10000003', 4.30, 'OPEN', 3
FROM campus c
WHERE c.name = 'Main Campus'
ON DUPLICATE KEY UPDATE
  location = VALUES(location),
  description = VALUES(description),
  opening_hours = VALUES(opening_hours),
  contact_phone = VALUES(contact_phone),
  average_score = VALUES(average_score),
  status = VALUES(status),
  sort_order = VALUES(sort_order),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO stall (canteen_id, name, location, description, average_score, status, sort_order)
SELECT c.id, 'Sichuan Wok', 'First Floor A1', 'Rice bowls and stir fry dishes', 4.70, 'OPEN', 1
FROM canteen c
WHERE c.name = 'First Canteen'
ON DUPLICATE KEY UPDATE
  location = VALUES(location),
  description = VALUES(description),
  average_score = VALUES(average_score),
  status = VALUES(status),
  sort_order = VALUES(sort_order),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO stall (canteen_id, name, location, description, average_score, status, sort_order)
SELECT c.id, 'Energy Bowl', 'Shop 01', 'Light food and healthy combos', 4.45, 'OPEN', 1
FROM canteen c
WHERE c.name = 'Light Food Hub'
ON DUPLICATE KEY UPDATE
  location = VALUES(location),
  description = VALUES(description),
  average_score = VALUES(average_score),
  status = VALUES(status),
  sort_order = VALUES(sort_order),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO stall (canteen_id, name, location, description, average_score, status, sort_order)
SELECT c.id, 'BBQ Rice House', 'Block 08', 'Independent periphery store', 4.35, 'OPEN', 1
FROM canteen c
WHERE c.name = 'South Gate Food Street'
ON DUPLICATE KEY UPDATE
  location = VALUES(location),
  description = VALUES(description),
  average_score = VALUES(average_score),
  status = VALUES(status),
  sort_order = VALUES(sort_order),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_user (wechat_openid, nickname, status)
VALUES ('demo-merchant-sichuan', 'Merchant Sichuan Wok', 'ACTIVE')
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_user (wechat_openid, nickname, status)
VALUES ('demo-merchant-lightfood', 'Merchant Energy Bowl', 'ACTIVE')
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_user (wechat_openid, nickname, status)
VALUES ('demo-merchant-bbq', 'Merchant BBQ Rice House', 'ACTIVE')
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
INNER JOIN sys_role r ON r.role_code = 'MERCHANT'
WHERE u.wechat_openid = 'demo-merchant-sichuan'
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
INNER JOIN sys_role r ON r.role_code = 'MERCHANT'
WHERE u.wechat_openid = 'demo-merchant-lightfood'
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
INNER JOIN sys_role r ON r.role_code = 'MERCHANT'
WHERE u.wechat_openid = 'demo-merchant-bbq'
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id);

INSERT INTO category (name, type, sort_order, status)
VALUES ('Rice Bowl', 'DISH', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO category (name, type, sort_order, status)
VALUES ('Light Food', 'DISH', 2, 'ACTIVE')
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO category (name, type, sort_order, status)
VALUES ('BBQ Combo', 'DISH', 3, 'ACTIVE')
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO tag (name, type, sort_order, status)
VALUES ('Mild Spicy', 'TASTE', 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO tag (name, type, sort_order, status)
VALUES ('Filling', 'FEATURE', 2, 'ACTIVE')
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO tag (name, type, sort_order, status)
VALUES ('Low Fat', 'FEATURE', 3, 'ACTIVE')
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO tag (name, type, sort_order, status)
VALUES ('Signature', 'FEATURE', 4, 'ACTIVE')
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO merchant (
  user_id, stall_id, name, description, logo_url, contact_phone, business_hours,
  apply_status, status, favorite_count, review_count, average_score, approved_at
)
SELECT
  u.id,
  s.id,
  'Sichuan Wok',
  'Canteen stall focused on rice bowls and spicy combos',
  'https://example.com/logo/sichuan.png',
  '13800000001',
  '10:00-20:30',
  'APPROVED',
  'OPEN',
  126,
  58,
  4.70,
  '2026-04-01 10:00:00'
FROM sys_user u
INNER JOIN stall s ON s.name = 'Sichuan Wok'
WHERE u.wechat_openid = 'demo-merchant-sichuan'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  logo_url = VALUES(logo_url),
  contact_phone = VALUES(contact_phone),
  business_hours = VALUES(business_hours),
  apply_status = VALUES(apply_status),
  status = VALUES(status),
  favorite_count = VALUES(favorite_count),
  review_count = VALUES(review_count),
  average_score = VALUES(average_score),
  approved_at = VALUES(approved_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO merchant (
  user_id, stall_id, name, description, logo_url, contact_phone, business_hours,
  apply_status, status, favorite_count, review_count, average_score, approved_at
)
SELECT
  u.id,
  s.id,
  'Energy Bowl',
  'Independent campus light food shop',
  'https://example.com/logo/lightfood.png',
  '13800000002',
  '09:00-20:00',
  'APPROVED',
  'OPEN',
  88,
  36,
  4.45,
  '2026-04-01 10:00:00'
FROM sys_user u
INNER JOIN stall s ON s.name = 'Energy Bowl'
WHERE u.wechat_openid = 'demo-merchant-lightfood'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  logo_url = VALUES(logo_url),
  contact_phone = VALUES(contact_phone),
  business_hours = VALUES(business_hours),
  apply_status = VALUES(apply_status),
  status = VALUES(status),
  favorite_count = VALUES(favorite_count),
  review_count = VALUES(review_count),
  average_score = VALUES(average_score),
  approved_at = VALUES(approved_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO merchant (
  user_id, stall_id, name, description, logo_url, contact_phone, business_hours,
  apply_status, status, favorite_count, review_count, average_score, approved_at
)
SELECT
  u.id,
  s.id,
  'BBQ Rice House',
  'Periphery merchant focused on BBQ rice combos',
  'https://example.com/logo/bbq.png',
  '13800000003',
  '10:00-22:00',
  'APPROVED',
  'OPEN',
  64,
  24,
  4.35,
  '2026-04-01 10:00:00'
FROM sys_user u
INNER JOIN stall s ON s.name = 'BBQ Rice House'
WHERE u.wechat_openid = 'demo-merchant-bbq'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  logo_url = VALUES(logo_url),
  contact_phone = VALUES(contact_phone),
  business_hours = VALUES(business_hours),
  apply_status = VALUES(apply_status),
  status = VALUES(status),
  favorite_count = VALUES(favorite_count),
  review_count = VALUES(review_count),
  average_score = VALUES(average_score),
  approved_at = VALUES(approved_at),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO dish (
  merchant_id, canteen_id, stall_id, category_id, name, description, price, cover_image_url,
  average_score, taste_score, portion_score, value_score, status, is_join_lottery,
  view_count, favorite_count, review_count
)
SELECT
  m.id, c.id, s.id, cg.id,
  'Black Pepper Chicken Rice',
  'Fresh fried chicken with black pepper sauce and vegetables',
  16.00,
  'https://example.com/dish/chicken-rice-cover.png',
  4.80, 4.90, 4.70, 4.60,
  'ON_SALE', 1,
  560, 132, 68
FROM merchant m
INNER JOIN stall s ON s.id = m.stall_id
INNER JOIN canteen c ON c.id = s.canteen_id
INNER JOIN category cg ON cg.name = 'Rice Bowl' AND cg.type = 'DISH'
WHERE m.name = 'Sichuan Wok'
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  price = VALUES(price),
  cover_image_url = VALUES(cover_image_url),
  average_score = VALUES(average_score),
  taste_score = VALUES(taste_score),
  portion_score = VALUES(portion_score),
  value_score = VALUES(value_score),
  status = VALUES(status),
  is_join_lottery = VALUES(is_join_lottery),
  view_count = VALUES(view_count),
  favorite_count = VALUES(favorite_count),
  review_count = VALUES(review_count),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO dish (
  merchant_id, canteen_id, stall_id, category_id, name, description, price, cover_image_url,
  average_score, taste_score, portion_score, value_score, status, is_join_lottery,
  view_count, favorite_count, review_count
)
SELECT
  m.id, c.id, s.id, cg.id,
  'Spicy Chicken Leg Rice',
  'Mild spicy chicken leg with egg and side vegetables',
  14.00,
  'https://example.com/dish/spicy-leg-rice-cover.png',
  4.55, 4.60, 4.50, 4.55,
  'SOLD_OUT', 1,
  410, 95, 42
FROM merchant m
INNER JOIN stall s ON s.id = m.stall_id
INNER JOIN canteen c ON c.id = s.canteen_id
INNER JOIN category cg ON cg.name = 'Rice Bowl' AND cg.type = 'DISH'
WHERE m.name = 'Sichuan Wok'
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  price = VALUES(price),
  cover_image_url = VALUES(cover_image_url),
  average_score = VALUES(average_score),
  taste_score = VALUES(taste_score),
  portion_score = VALUES(portion_score),
  value_score = VALUES(value_score),
  status = VALUES(status),
  is_join_lottery = VALUES(is_join_lottery),
  view_count = VALUES(view_count),
  favorite_count = VALUES(favorite_count),
  review_count = VALUES(review_count),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO dish (
  merchant_id, canteen_id, stall_id, category_id, name, description, price, cover_image_url,
  average_score, taste_score, portion_score, value_score, status, is_join_lottery,
  view_count, favorite_count, review_count
)
SELECT
  m.id, c.id, s.id, cg.id,
  'Beef Energy Bowl',
  'Brown rice, beef, broccoli and soft boiled egg',
  22.00,
  'https://example.com/dish/beef-bowl-cover.png',
  4.50, 4.55, 4.30, 4.40,
  'ON_SALE', 1,
  280, 66, 28
FROM merchant m
INNER JOIN stall s ON s.id = m.stall_id
INNER JOIN canteen c ON c.id = s.canteen_id
INNER JOIN category cg ON cg.name = 'Light Food' AND cg.type = 'DISH'
WHERE m.name = 'Energy Bowl'
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  price = VALUES(price),
  cover_image_url = VALUES(cover_image_url),
  average_score = VALUES(average_score),
  taste_score = VALUES(taste_score),
  portion_score = VALUES(portion_score),
  value_score = VALUES(value_score),
  status = VALUES(status),
  is_join_lottery = VALUES(is_join_lottery),
  view_count = VALUES(view_count),
  favorite_count = VALUES(favorite_count),
  review_count = VALUES(review_count),
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO dish (
  merchant_id, canteen_id, stall_id, category_id, name, description, price, cover_image_url,
  average_score, taste_score, portion_score, value_score, status, is_join_lottery,
  view_count, favorite_count, review_count
)
SELECT
  m.id, c.id, s.id, cg.id,
  'Double BBQ Rice',
  'Pork belly and chicken combo with large portion',
  20.00,
  'https://example.com/dish/bbq-mix-cover.png',
  4.35, 4.40, 4.50, 4.20,
  'ON_SALE', 1,
  320, 58, 21
FROM merchant m
INNER JOIN stall s ON s.id = m.stall_id
INNER JOIN canteen c ON c.id = s.canteen_id
INNER JOIN category cg ON cg.name = 'BBQ Combo' AND cg.type = 'DISH'
WHERE m.name = 'BBQ Rice House'
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  price = VALUES(price),
  cover_image_url = VALUES(cover_image_url),
  average_score = VALUES(average_score),
  taste_score = VALUES(taste_score),
  portion_score = VALUES(portion_score),
  value_score = VALUES(value_score),
  status = VALUES(status),
  is_join_lottery = VALUES(is_join_lottery),
  view_count = VALUES(view_count),
  favorite_count = VALUES(favorite_count),
  review_count = VALUES(review_count),
  updated_at = CURRENT_TIMESTAMP;

DELETE di
FROM dish_image di
INNER JOIN dish d ON d.id = di.dish_id
WHERE d.name IN (
  'Black Pepper Chicken Rice',
  'Spicy Chicken Leg Rice',
  'Beef Energy Bowl',
  'Double BBQ Rice'
);

INSERT INTO dish_image (dish_id, image_url, sort_order, is_cover)
SELECT d.id, 'https://example.com/dish/chicken-rice-cover.png', 1, 1
FROM dish d
INNER JOIN merchant m ON m.id = d.merchant_id
WHERE d.name = 'Black Pepper Chicken Rice' AND m.name = 'Sichuan Wok';

INSERT INTO dish_image (dish_id, image_url, sort_order, is_cover)
SELECT d.id, 'https://example.com/dish/chicken-rice-detail.png', 2, 0
FROM dish d
INNER JOIN merchant m ON m.id = d.merchant_id
WHERE d.name = 'Black Pepper Chicken Rice' AND m.name = 'Sichuan Wok';

INSERT INTO dish_image (dish_id, image_url, sort_order, is_cover)
SELECT d.id, 'https://example.com/dish/spicy-leg-rice-cover.png', 1, 1
FROM dish d
INNER JOIN merchant m ON m.id = d.merchant_id
WHERE d.name = 'Spicy Chicken Leg Rice' AND m.name = 'Sichuan Wok';

INSERT INTO dish_image (dish_id, image_url, sort_order, is_cover)
SELECT d.id, 'https://example.com/dish/beef-bowl-cover.png', 1, 1
FROM dish d
INNER JOIN merchant m ON m.id = d.merchant_id
WHERE d.name = 'Beef Energy Bowl' AND m.name = 'Energy Bowl';

INSERT INTO dish_image (dish_id, image_url, sort_order, is_cover)
SELECT d.id, 'https://example.com/dish/bbq-mix-cover.png', 1, 1
FROM dish d
INNER JOIN merchant m ON m.id = d.merchant_id
WHERE d.name = 'Double BBQ Rice' AND m.name = 'BBQ Rice House';

INSERT INTO dish_tag (dish_id, tag_id)
SELECT d.id, t.id
FROM dish d
INNER JOIN merchant m ON m.id = d.merchant_id
INNER JOIN tag t ON t.name = 'Filling'
WHERE d.name = 'Black Pepper Chicken Rice' AND m.name = 'Sichuan Wok'
ON DUPLICATE KEY UPDATE
  tag_id = VALUES(tag_id);

INSERT INTO dish_tag (dish_id, tag_id)
SELECT d.id, t.id
FROM dish d
INNER JOIN merchant m ON m.id = d.merchant_id
INNER JOIN tag t ON t.name = 'Signature'
WHERE d.name = 'Black Pepper Chicken Rice' AND m.name = 'Sichuan Wok'
ON DUPLICATE KEY UPDATE
  tag_id = VALUES(tag_id);

INSERT INTO dish_tag (dish_id, tag_id)
SELECT d.id, t.id
FROM dish d
INNER JOIN merchant m ON m.id = d.merchant_id
INNER JOIN tag t ON t.name = 'Mild Spicy'
WHERE d.name = 'Spicy Chicken Leg Rice' AND m.name = 'Sichuan Wok'
ON DUPLICATE KEY UPDATE
  tag_id = VALUES(tag_id);

INSERT INTO dish_tag (dish_id, tag_id)
SELECT d.id, t.id
FROM dish d
INNER JOIN merchant m ON m.id = d.merchant_id
INNER JOIN tag t ON t.name = 'Low Fat'
WHERE d.name = 'Beef Energy Bowl' AND m.name = 'Energy Bowl'
ON DUPLICATE KEY UPDATE
  tag_id = VALUES(tag_id);

INSERT INTO dish_tag (dish_id, tag_id)
SELECT d.id, t.id
FROM dish d
INNER JOIN merchant m ON m.id = d.merchant_id
INNER JOIN tag t ON t.name = 'Filling'
WHERE d.name = 'Double BBQ Rice' AND m.name = 'BBQ Rice House'
ON DUPLICATE KEY UPDATE
  tag_id = VALUES(tag_id);

DELETE mr
FROM merchant_recommendation mr
INNER JOIN dish d ON d.id = mr.dish_id
WHERE d.name IN ('Black Pepper Chicken Rice', 'Beef Energy Bowl');

INSERT INTO merchant_recommendation (
  merchant_id, dish_id, title, recommend_reason, recommend_type, start_time, end_time, status, is_top, click_count
)
SELECT
  m.id,
  d.id,
  'Today Pick: Black Pepper Chicken Rice',
  'Fresh fried chicken, stable quality and good for lunch time.',
  'TODAY',
  '2026-01-01 00:00:00',
  '2027-12-31 23:59:59',
  'ACTIVE',
  1,
  156
FROM merchant m
INNER JOIN dish d ON d.merchant_id = m.id
WHERE m.name = 'Sichuan Wok' AND d.name = 'Black Pepper Chicken Rice';

INSERT INTO merchant_recommendation (
  merchant_id, dish_id, title, recommend_reason, recommend_type, start_time, end_time, status, is_top, click_count
)
SELECT
  m.id,
  d.id,
  'Weekly Pick: Beef Energy Bowl',
  'Balanced combo for a lighter lunch and fitness routine.',
  'VALUE',
  '2026-01-01 00:00:00',
  '2027-12-31 23:59:59',
  'ACTIVE',
  0,
  92
FROM merchant m
INNER JOIN dish d ON d.merchant_id = m.id
WHERE m.name = 'Energy Bowl' AND d.name = 'Beef Energy Bowl';
