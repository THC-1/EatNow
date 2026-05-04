USE eatnow;

-- User, auth and permission module

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  role_code VARCHAR(32) NOT NULL COMMENT 'Role code: STUDENT/MERCHANT/ADMIN',
  role_name VARCHAR(64) NOT NULL COMMENT 'Role display name',
  description VARCHAR(255) DEFAULT NULL COMMENT 'Role description',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System roles';

INSERT INTO sys_role (role_code, role_name, description)
VALUES
  ('STUDENT', 'Student', 'Mini program student user'),
  ('MERCHANT', 'Merchant', 'Campus merchant or stall operator'),
  ('ADMIN', 'Admin', 'Platform console administrator')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  description = VALUES(description),
  updated_at = CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  username VARCHAR(64) DEFAULT NULL COMMENT 'Account username for admin and Android password login',
  password_hash VARCHAR(255) DEFAULT NULL COMMENT 'Encrypted password for account login',
  wechat_openid VARCHAR(128) DEFAULT NULL COMMENT 'Wechat mini-program openid for student/merchant login',
  nickname VARCHAR(64) NOT NULL COMMENT 'Nickname',
  avatar_url VARCHAR(255) DEFAULT NULL COMMENT 'Avatar URL',
  phone VARCHAR(20) DEFAULT NULL COMMENT 'Phone number',
  gender VARCHAR(16) DEFAULT NULL COMMENT 'Gender',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
  last_login_at DATETIME DEFAULT NULL COMMENT 'Last login time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username),
  UNIQUE KEY uk_sys_user_wechat_openid (wechat_openid),
  UNIQUE KEY uk_sys_user_phone (phone),
  KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Unified user table for students, merchants and admins';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  role_id BIGINT UNSIGNED NOT NULL COMMENT 'Role ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_role (user_id, role_id),
  KEY idx_sys_user_role_role_id (role_id),
  CONSTRAINT fk_sys_user_role_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_sys_user_role_role
    FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User and role relation';

CREATE TABLE IF NOT EXISTS student_profile (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Student user ID',
  campus_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Campus ID',
  student_no VARCHAR(32) DEFAULT NULL COMMENT 'Student number',
  grade VARCHAR(32) DEFAULT NULL COMMENT 'Grade',
  major VARCHAR(64) DEFAULT NULL COMMENT 'Major',
  bio VARCHAR(255) DEFAULT NULL COMMENT 'Student bio',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_profile_user_id (user_id),
  UNIQUE KEY uk_student_profile_student_no (student_no),
  KEY idx_student_profile_campus_id (campus_id),
  CONSTRAINT fk_student_profile_user
    FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student profile';
