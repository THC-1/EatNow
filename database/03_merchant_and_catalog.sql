USE eatnow;

-- Merchant, category, tag and dish module

CREATE TABLE IF NOT EXISTS category (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  name VARCHAR(64) NOT NULL COMMENT 'Category name',
  type VARCHAR(32) NOT NULL DEFAULT 'DISH' COMMENT 'DISH/POST/OTHER',
  sort_order INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_category_name_type (name, type),
  KEY idx_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Category';

CREATE TABLE IF NOT EXISTS tag (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  name VARCHAR(64) NOT NULL COMMENT 'Tag name',
  type VARCHAR(32) NOT NULL COMMENT 'TASTE/FEATURE/SCENE/etc.',
  sort_order INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tag_name_type (name, type),
  KEY idx_tag_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tag';

CREATE TABLE IF NOT EXISTS merchant (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Merchant user ID',
  stall_id BIGINT UNSIGNED NOT NULL COMMENT 'Bound stall ID',
  name VARCHAR(128) NOT NULL COMMENT 'Merchant name',
  description VARCHAR(500) DEFAULT NULL COMMENT 'Merchant description',
  logo_url VARCHAR(255) DEFAULT NULL COMMENT 'Logo URL',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT 'Contact phone',
  business_hours VARCHAR(64) DEFAULT NULL COMMENT 'Business hours',
  apply_status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/OPEN/CLOSED/DISABLED',
  reject_reason VARCHAR(255) DEFAULT NULL COMMENT 'Reject reason',
  favorite_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Merchant favorite count',
  review_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Merchant review count',
  average_score DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT 'Merchant average score',
  approved_by BIGINT UNSIGNED DEFAULT NULL COMMENT 'Admin user ID',
  approved_at DATETIME DEFAULT NULL COMMENT 'Approve time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_merchant_user_id (user_id),
  UNIQUE KEY uk_merchant_stall_id (stall_id),
  KEY idx_merchant_apply_status (apply_status),
  KEY idx_merchant_status (status),
  CONSTRAINT fk_merchant_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_merchant_stall
    FOREIGN KEY (stall_id) REFERENCES stall (id),
  CONSTRAINT fk_merchant_approved_by
    FOREIGN KEY (approved_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Merchant profile and application data';

CREATE TABLE IF NOT EXISTS dish (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  merchant_id BIGINT UNSIGNED NOT NULL COMMENT 'Merchant ID',
  canteen_id BIGINT UNSIGNED NOT NULL COMMENT 'Business area ID',
  stall_id BIGINT UNSIGNED NOT NULL COMMENT 'Stall ID',
  category_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Category ID',
  name VARCHAR(128) NOT NULL COMMENT 'Dish name',
  description VARCHAR(500) DEFAULT NULL COMMENT 'Dish description',
  price DECIMAL(10,2) NOT NULL COMMENT 'Dish price',
  cover_image_url VARCHAR(255) DEFAULT NULL COMMENT 'Cover image URL',
  average_score DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT 'Overall average score',
  taste_score DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT 'Taste average score',
  portion_score DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT 'Portion average score',
  value_score DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT 'Value average score',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ON_SALE/SOLD_OUT/OFF_SHELF',
  is_join_lottery TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether join lottery',
  view_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'View count',
  favorite_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Favorite count',
  review_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Review count',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dish_merchant_name (merchant_id, name),
  KEY idx_dish_canteen (canteen_id),
  KEY idx_dish_stall (stall_id),
  KEY idx_dish_category (category_id),
  KEY idx_dish_status_score (status, average_score),
  CONSTRAINT fk_dish_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchant (id),
  CONSTRAINT fk_dish_canteen
    FOREIGN KEY (canteen_id) REFERENCES canteen (id),
  CONSTRAINT fk_dish_stall
    FOREIGN KEY (stall_id) REFERENCES stall (id),
  CONSTRAINT fk_dish_category
    FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dish';

CREATE TABLE IF NOT EXISTS dish_image (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT 'Dish ID',
  image_url VARCHAR(255) NOT NULL COMMENT 'Image URL',
  sort_order INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  is_cover TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether cover image',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  PRIMARY KEY (id),
  KEY idx_dish_image_dish_id (dish_id),
  CONSTRAINT fk_dish_image_dish
    FOREIGN KEY (dish_id) REFERENCES dish (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dish images';

CREATE TABLE IF NOT EXISTS dish_tag (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT 'Dish ID',
  tag_id BIGINT UNSIGNED NOT NULL COMMENT 'Tag ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dish_tag (dish_id, tag_id),
  KEY idx_dish_tag_tag_id (tag_id),
  CONSTRAINT fk_dish_tag_dish
    FOREIGN KEY (dish_id) REFERENCES dish (id),
  CONSTRAINT fk_dish_tag_tag
    FOREIGN KEY (tag_id) REFERENCES tag (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dish and tag relation';

CREATE TABLE IF NOT EXISTS merchant_recommendation (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  merchant_id BIGINT UNSIGNED NOT NULL COMMENT 'Merchant ID',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT 'Dish ID',
  title VARCHAR(128) NOT NULL COMMENT 'Recommendation title',
  recommend_reason VARCHAR(500) DEFAULT NULL COMMENT 'Recommendation reason',
  recommend_type VARCHAR(32) NOT NULL COMMENT 'TODAY/NEW/SPECIAL/VALUE/SIGNATURE',
  start_time DATETIME NOT NULL COMMENT 'Start time',
  end_time DATETIME NOT NULL COMMENT 'End time',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/EXPIRED/CANCELLED',
  is_top TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether pinned',
  click_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Click count',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  KEY idx_merchant_recommendation_merchant (merchant_id),
  KEY idx_merchant_recommendation_dish (dish_id),
  KEY idx_merchant_recommendation_type_status (recommend_type, status),
  CONSTRAINT fk_merchant_recommendation_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchant (id),
  CONSTRAINT fk_merchant_recommendation_dish
    FOREIGN KEY (dish_id) REFERENCES dish (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Merchant recommendation';
