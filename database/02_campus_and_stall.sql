USE eatnow;

-- Campus, business area and stall module

CREATE TABLE IF NOT EXISTS school (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  name VARCHAR(128) NOT NULL COMMENT 'School name',
  code VARCHAR(32) DEFAULT NULL COMMENT 'School code',
  address VARCHAR(255) DEFAULT NULL COMMENT 'School address',
  description VARCHAR(255) DEFAULT NULL COMMENT 'School description',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_school_name (name),
  UNIQUE KEY uk_school_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='School';

CREATE TABLE IF NOT EXISTS campus (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  school_id BIGINT UNSIGNED NOT NULL COMMENT 'School ID',
  name VARCHAR(128) NOT NULL COMMENT 'Campus name',
  address VARCHAR(255) DEFAULT NULL COMMENT 'Campus address',
  description VARCHAR(255) DEFAULT NULL COMMENT 'Campus description',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_campus_school_name (school_id, name),
  KEY idx_campus_status (status),
  CONSTRAINT fk_campus_school
    FOREIGN KEY (school_id) REFERENCES school (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Campus';

CREATE TABLE IF NOT EXISTS canteen (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  campus_id BIGINT UNSIGNED NOT NULL COMMENT 'Campus ID',
  name VARCHAR(128) NOT NULL COMMENT 'Business area name',
  type VARCHAR(32) NOT NULL COMMENT 'CANTEEN/CAMPUS_SHOP/PERIPHERY_SHOP',
  location VARCHAR(255) DEFAULT NULL COMMENT 'Location',
  description VARCHAR(255) DEFAULT NULL COMMENT 'Description',
  opening_hours VARCHAR(64) DEFAULT NULL COMMENT 'Opening hours',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT 'Contact phone',
  average_score DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT 'Average score',
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/CLOSED/DISABLED',
  sort_order INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_canteen_campus_name (campus_id, name),
  KEY idx_canteen_type_status (type, status),
  CONSTRAINT fk_canteen_campus
    FOREIGN KEY (campus_id) REFERENCES campus (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Business area table, mapped by API as canteen';

CREATE TABLE IF NOT EXISTS stall (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  canteen_id BIGINT UNSIGNED NOT NULL COMMENT 'Business area ID',
  name VARCHAR(128) NOT NULL COMMENT 'Stall/store name',
  location VARCHAR(255) DEFAULT NULL COMMENT 'Stall location',
  description VARCHAR(255) DEFAULT NULL COMMENT 'Description',
  average_score DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT 'Average score',
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/CLOSED/DISABLED',
  sort_order INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_stall_canteen_name (canteen_id, name),
  KEY idx_stall_status (status),
  CONSTRAINT fk_stall_canteen
    FOREIGN KEY (canteen_id) REFERENCES canteen (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Stall/store';
