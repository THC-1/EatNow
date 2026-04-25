USE eatnow;

-- Lottery and recommendation support module

CREATE TABLE IF NOT EXISTS user_preference (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Student user ID',
  default_canteen_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Default business area ID',
  default_canteen_type VARCHAR(32) DEFAULT NULL COMMENT 'CANTEEN/CAMPUS_SHOP/PERIPHERY_SHOP',
  min_price DECIMAL(10,2) DEFAULT NULL COMMENT 'Preferred min price',
  max_price DECIMAL(10,2) DEFAULT NULL COMMENT 'Preferred max price',
  taste_preference VARCHAR(255) DEFAULT NULL COMMENT 'Preferred tags, comma separated',
  avoid_tags VARCHAR(255) DEFAULT NULL COMMENT 'Avoided tags, comma separated',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_preference_user_id (user_id),
  KEY idx_user_preference_canteen_id (default_canteen_id),
  CONSTRAINT fk_user_preference_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_user_preference_canteen
    FOREIGN KEY (default_canteen_id) REFERENCES canteen (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User preference for lottery and recommendations';

CREATE TABLE IF NOT EXISTS lottery_rule (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  rule_name VARCHAR(128) NOT NULL COMMENT 'Rule name',
  draw_mode VARCHAR(16) NOT NULL COMMENT 'RANDOM/CONDITION/FAVORITE',
  student_ratio DECIMAL(5,2) DEFAULT NULL COMMENT 'Student post ratio',
  merchant_ratio DECIMAL(5,2) DEFAULT NULL COMMENT 'Merchant dish ratio',
  min_score DECIMAL(3,2) DEFAULT NULL COMMENT 'Minimum score threshold',
  max_price DECIMAL(10,2) DEFAULT NULL COMMENT 'Maximum price threshold',
  is_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether default rule',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
  remark VARCHAR(255) DEFAULT NULL COMMENT 'Remark',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_lottery_rule_name (rule_name),
  KEY idx_lottery_rule_mode_status (draw_mode, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lottery rules';

INSERT INTO lottery_rule (
  rule_name, draw_mode, student_ratio, merchant_ratio, min_score, max_price, is_default, status, remark
)
VALUES
  ('default-random-draw', 'RANDOM', 0.00, 100.00, NULL, NULL, 1, 'ACTIVE', 'Default full-range draw rule'),
  ('default-condition-draw', 'CONDITION', 0.00, 100.00, 4.00, 30.00, 0, 'ACTIVE', 'Default conditional draw rule'),
  ('default-favorite-draw', 'FAVORITE', 0.00, 100.00, NULL, NULL, 0, 'ACTIVE', 'Default favorite draw rule')
ON DUPLICATE KEY UPDATE
  draw_mode = VALUES(draw_mode),
  student_ratio = VALUES(student_ratio),
  merchant_ratio = VALUES(merchant_ratio),
  min_score = VALUES(min_score),
  max_price = VALUES(max_price),
  is_default = VALUES(is_default),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_at = CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS lottery_pool (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  source_type VARCHAR(16) NOT NULL COMMENT 'DISH/POST',
  source_id BIGINT UNSIGNED NOT NULL COMMENT 'Source ID',
  merchant_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Merchant ID',
  canteen_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Business area ID',
  stall_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Stall ID',
  category_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Category ID',
  title VARCHAR(128) NOT NULL COMMENT 'Display title',
  price DECIMAL(10,2) DEFAULT NULL COMMENT 'Price snapshot',
  score DECIMAL(3,2) DEFAULT NULL COMMENT 'Score snapshot',
  cover_image_url VARCHAR(255) DEFAULT NULL COMMENT 'Cover image URL',
  recommend_reason VARCHAR(255) DEFAULT NULL COMMENT 'Recommendation reason',
  weight INT NOT NULL DEFAULT 100 COMMENT 'Lottery weight',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_lottery_pool_source (source_type, source_id),
  KEY idx_lottery_pool_status_weight (status, weight),
  KEY idx_lottery_pool_canteen_id (canteen_id),
  KEY idx_lottery_pool_category_id (category_id),
  CONSTRAINT fk_lottery_pool_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchant (id),
  CONSTRAINT fk_lottery_pool_canteen
    FOREIGN KEY (canteen_id) REFERENCES canteen (id),
  CONSTRAINT fk_lottery_pool_stall
    FOREIGN KEY (stall_id) REFERENCES stall (id),
  CONSTRAINT fk_lottery_pool_category
    FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Unified lottery pool';

CREATE TABLE IF NOT EXISTS lottery_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Student user ID',
  rule_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Lottery rule ID',
  draw_mode VARCHAR(16) NOT NULL COMMENT 'RANDOM/CONDITION/FAVORITE',
  source_type VARCHAR(16) NOT NULL COMMENT 'DISH/POST',
  source_id BIGINT UNSIGNED NOT NULL COMMENT 'Lottery result source ID',
  snapshot_title VARCHAR(128) NOT NULL COMMENT 'Result title snapshot',
  snapshot_price DECIMAL(10,2) DEFAULT NULL COMMENT 'Result price snapshot',
  snapshot_score DECIMAL(3,2) DEFAULT NULL COMMENT 'Result score snapshot',
  condition_json JSON DEFAULT NULL COMMENT 'Draw condition payload',
  result_action VARCHAR(16) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/ACCEPT/SKIP/FAVORITE',
  action_at DATETIME DEFAULT NULL COMMENT 'Action time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  KEY idx_lottery_record_user_created_at (user_id, created_at),
  KEY idx_lottery_record_rule_id (rule_id),
  KEY idx_lottery_record_source (source_type, source_id),
  CONSTRAINT fk_lottery_record_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_lottery_record_rule
    FOREIGN KEY (rule_id) REFERENCES lottery_rule (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lottery records';

CREATE TABLE IF NOT EXISTS lottery_candidate (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  record_id BIGINT UNSIGNED NOT NULL COMMENT 'Lottery record ID',
  source_type VARCHAR(16) NOT NULL COMMENT 'DISH/POST',
  source_id BIGINT UNSIGNED NOT NULL COMMENT 'Candidate source ID',
  title VARCHAR(128) NOT NULL COMMENT 'Candidate title',
  price DECIMAL(10,2) DEFAULT NULL COMMENT 'Candidate price snapshot',
  score DECIMAL(3,2) DEFAULT NULL COMMENT 'Candidate score snapshot',
  sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
  is_selected TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether selected result',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  PRIMARY KEY (id),
  KEY idx_lottery_candidate_record_id (record_id),
  CONSTRAINT fk_lottery_candidate_record
    FOREIGN KEY (record_id) REFERENCES lottery_record (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lottery candidate list';
