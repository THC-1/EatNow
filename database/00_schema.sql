-- EatNow database bootstrap
-- Recommended execution order:
-- 1. 00_schema.sql
-- 2. 01_user_and_auth.sql
-- 3. 02_campus_and_stall.sql
-- 4. 03_merchant_and_catalog.sql
-- 5. 04_student_content.sql
-- 6. 05_interaction_and_feedback.sql
-- 7. 06_lottery_and_recommendation.sql

CREATE DATABASE IF NOT EXISTS eatnow
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE eatnow;
