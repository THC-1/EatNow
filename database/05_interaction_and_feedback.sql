USE eatnow;

-- Favorite, eat-list, review, feedback and activity module

CREATE TABLE IF NOT EXISTS favorite (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Student user ID',
  target_type VARCHAR(16) NOT NULL COMMENT 'DISH/POST',
  target_id BIGINT UNSIGNED NOT NULL COMMENT 'Target ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_favorite_user_target (user_id, target_type, target_id),
  KEY idx_favorite_target (target_type, target_id),
  CONSTRAINT fk_favorite_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Favorite records';

CREATE TABLE IF NOT EXISTS eat_list (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Student user ID',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT 'Dish ID',
  status VARCHAR(16) NOT NULL DEFAULT 'WANT_TO_EAT' COMMENT 'WANT_TO_EAT/EATEN/CANCELLED',
  source_lottery_record_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Source lottery record ID',
  note VARCHAR(255) DEFAULT NULL COMMENT 'Memo',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  eaten_at DATETIME DEFAULT NULL COMMENT 'Eat time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_eat_list_user_dish (user_id, dish_id),
  KEY idx_eat_list_status (status),
  KEY idx_eat_list_lottery_record_id (source_lottery_record_id),
  CONSTRAINT fk_eat_list_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_eat_list_dish
    FOREIGN KEY (dish_id) REFERENCES dish (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Want-to-eat list';

CREATE TABLE IF NOT EXISTS review (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Student user ID',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT 'Dish ID',
  target_type VARCHAR(16) NOT NULL DEFAULT 'DISH' COMMENT 'API target type, current value DISH',
  overall_score DECIMAL(3,2) NOT NULL COMMENT 'Overall score',
  taste_score DECIMAL(3,2) NOT NULL COMMENT 'Taste score',
  portion_score DECIMAL(3,2) NOT NULL COMMENT 'Portion score',
  value_score DECIMAL(3,2) NOT NULL COMMENT 'Value score',
  content TEXT DEFAULT NULL COMMENT 'Review content',
  is_anonymous TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether anonymous',
  like_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Like count',
  status VARCHAR(16) NOT NULL DEFAULT 'VISIBLE' COMMENT 'VISIBLE/HIDDEN/DELETED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_review_user_dish (user_id, dish_id),
  KEY idx_review_dish_created_at (dish_id, created_at),
  KEY idx_review_dish_score (dish_id, overall_score),
  KEY idx_review_status (status),
  CONSTRAINT fk_review_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_review_dish
    FOREIGN KEY (dish_id) REFERENCES dish (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dish reviews';

CREATE TABLE IF NOT EXISTS review_image (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  review_id BIGINT UNSIGNED NOT NULL COMMENT 'Review ID',
  image_url VARCHAR(255) NOT NULL COMMENT 'Image URL',
  sort_order INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  PRIMARY KEY (id),
  KEY idx_review_image_review_id (review_id),
  CONSTRAINT fk_review_image_review
    FOREIGN KEY (review_id) REFERENCES review (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Review images';

CREATE TABLE IF NOT EXISTS review_like (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  review_id BIGINT UNSIGNED NOT NULL COMMENT 'Review ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Liker user ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_review_like (review_id, user_id),
  KEY idx_review_like_user_id (user_id),
  CONSTRAINT fk_review_like_review
    FOREIGN KEY (review_id) REFERENCES review (id),
  CONSTRAINT fk_review_like_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Review likes';

CREATE TABLE IF NOT EXISTS feedback (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Student user ID',
  merchant_id BIGINT UNSIGNED NOT NULL COMMENT 'Merchant ID',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT 'Dish ID',
  target_type VARCHAR(16) NOT NULL DEFAULT 'DISH' COMMENT 'API target type, current value DISH',
  feedback_type VARCHAR(32) NOT NULL COMMENT 'TASTE/PORTION/PRICE/SERVICE/HYGIENE/OTHER',
  content TEXT NOT NULL COMMENT 'Feedback content',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/VIEWED/ACCEPTED/IMPROVED/REJECTED',
  reply_content VARCHAR(500) DEFAULT NULL COMMENT 'Merchant reply content',
  replied_by BIGINT UNSIGNED DEFAULT NULL COMMENT 'Reply user ID',
  replied_at DATETIME DEFAULT NULL COMMENT 'Reply time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  KEY idx_feedback_merchant_status (merchant_id, status),
  KEY idx_feedback_dish_id (dish_id),
  KEY idx_feedback_type (feedback_type),
  CONSTRAINT fk_feedback_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_feedback_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchant (id),
  CONSTRAINT fk_feedback_dish
    FOREIGN KEY (dish_id) REFERENCES dish (id),
  CONSTRAINT fk_feedback_replied_by
    FOREIGN KEY (replied_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Merchant feedback';

CREATE TABLE IF NOT EXISTS merchant_improvement_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  merchant_id BIGINT UNSIGNED NOT NULL COMMENT 'Merchant ID',
  dish_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Dish ID',
  feedback_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Feedback ID',
  title VARCHAR(128) NOT NULL COMMENT 'Improvement title',
  content TEXT NOT NULL COMMENT 'Improvement content',
  before_description VARCHAR(255) DEFAULT NULL COMMENT 'Before improvement description',
  after_description VARCHAR(255) DEFAULT NULL COMMENT 'After improvement description',
  status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED' COMMENT 'DRAFT/PUBLISHED/ARCHIVED',
  is_public TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether visible to students',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  KEY idx_improvement_merchant_id (merchant_id),
  KEY idx_improvement_dish_id (dish_id),
  KEY idx_improvement_feedback_id (feedback_id),
  CONSTRAINT fk_improvement_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchant (id),
  CONSTRAINT fk_improvement_dish
    FOREIGN KEY (dish_id) REFERENCES dish (id),
  CONSTRAINT fk_improvement_feedback
    FOREIGN KEY (feedback_id) REFERENCES feedback (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Merchant improvement records';

CREATE TABLE IF NOT EXISTS user_activity_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  activity_type VARCHAR(32) NOT NULL COMMENT 'LOGIN/VIEW_DISH/FAVORITE/REVIEW/LOTTERY/etc.',
  target_type VARCHAR(32) DEFAULT NULL COMMENT 'Target type',
  target_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Target ID',
  extra_data JSON DEFAULT NULL COMMENT 'Extension payload',
  occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Occurrence time',
  PRIMARY KEY (id),
  KEY idx_user_activity_user_time (user_id, occurred_at),
  KEY idx_user_activity_type_time (activity_type, occurred_at),
  CONSTRAINT fk_user_activity_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User activity log for statistics';
