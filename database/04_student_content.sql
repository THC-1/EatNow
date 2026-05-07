USE eatnow;

-- Student post/share module

CREATE TABLE IF NOT EXISTS student_post (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Student user ID',
  canteen_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Business area ID',
  stall_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Stall ID',
  category_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Category ID',
  title VARCHAR(128) NOT NULL COMMENT 'Post title',
  dish_name VARCHAR(128) NOT NULL COMMENT 'Shared dish/food name',
  shop_name VARCHAR(128) NOT NULL COMMENT 'User entered shop/stall name',
  content TEXT NOT NULL COMMENT 'Post content',
  price DECIMAL(10,2) DEFAULT NULL COMMENT 'Dish price snapshot',
  score DECIMAL(3,2) DEFAULT NULL COMMENT 'Dish score snapshot',
  is_join_lottery TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether join lottery pool',
  cover_image_url VARCHAR(255) DEFAULT NULL COMMENT 'Cover image URL',
  status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED' COMMENT 'PUBLISHED/HIDDEN/DELETED',
  view_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'View count',
  like_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Like count',
  favorite_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Favorite count',
  comment_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Comment count',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  KEY idx_student_post_user_id (user_id),
  KEY idx_student_post_canteen_id (canteen_id),
  KEY idx_student_post_stall_id (stall_id),
  KEY idx_student_post_category_id (category_id),
  KEY idx_student_post_status (status),
  CONSTRAINT fk_student_post_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_student_post_canteen
    FOREIGN KEY (canteen_id) REFERENCES canteen (id),
  CONSTRAINT fk_student_post_stall
    FOREIGN KEY (stall_id) REFERENCES stall (id),
  CONSTRAINT fk_student_post_category
    FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student post/share';

CREATE TABLE IF NOT EXISTS student_post_image (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  post_id BIGINT UNSIGNED NOT NULL COMMENT 'Post ID',
  image_url VARCHAR(255) NOT NULL COMMENT 'Image URL',
  sort_order INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  PRIMARY KEY (id),
  KEY idx_student_post_image_post_id (post_id),
  CONSTRAINT fk_student_post_image_post
    FOREIGN KEY (post_id) REFERENCES student_post (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student post images';

CREATE TABLE IF NOT EXISTS student_post_tag (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  post_id BIGINT UNSIGNED NOT NULL COMMENT 'Post ID',
  tag_id BIGINT UNSIGNED NOT NULL COMMENT 'Tag ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_post_tag (post_id, tag_id),
  KEY idx_student_post_tag_tag_id (tag_id),
  CONSTRAINT fk_student_post_tag_post
    FOREIGN KEY (post_id) REFERENCES student_post (id),
  CONSTRAINT fk_student_post_tag_tag
    FOREIGN KEY (tag_id) REFERENCES tag (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student post and tag relation';

CREATE TABLE IF NOT EXISTS student_post_like (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  post_id BIGINT UNSIGNED NOT NULL COMMENT 'Post ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_post_like (post_id, user_id),
  KEY idx_student_post_like_user_id (user_id),
  CONSTRAINT fk_student_post_like_post
    FOREIGN KEY (post_id) REFERENCES student_post (id),
  CONSTRAINT fk_student_post_like_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student post likes';
