USE eatnow;

-- One-time upgrade for existing databases created before user post publishing APIs.
-- Fresh databases already get these columns from 04_student_content.sql.

UPDATE student_post
SET dish_name = title
WHERE dish_name IS NULL OR dish_name = '';

ALTER TABLE student_post
  MODIFY dish_name VARCHAR(128) NOT NULL COMMENT 'Shared dish/food name',
  ADD COLUMN shop_name VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'User entered shop/stall name' AFTER dish_name,
  ADD COLUMN is_join_lottery TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether join lottery pool' AFTER score;
