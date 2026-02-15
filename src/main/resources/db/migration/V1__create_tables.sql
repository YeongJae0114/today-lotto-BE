-- V1__create_tables.sql
-- Core tables (excluding strategy tables)
SET NAMES utf8mb4;
SET time_zone = '+09:00';

CREATE TABLE IF NOT EXISTS choice (
  id INT PRIMARY KEY,
  value INT NOT NULL,
  label VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  bucket VARCHAR(20) NOT NULL,
  text VARCHAR(300) NOT NULL,
  primary_axis VARCHAR(20) NOT NULL,
  secondary_axis VARCHAR(20) NULL,
  strength DECIMAL(3,2) NOT NULL,
  polarity INT NOT NULL,
  tag_on_high VARCHAR(60) NULL,
  tag_on_low VARCHAR(60) NULL,
  weight INT NOT NULL DEFAULT 1,
  INDEX idx_question_bucket(bucket),
  INDEX idx_question_primary(primary_axis)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS keyword_dictionary (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  keyword VARCHAR(80) NOT NULL,
  normalized VARCHAR(80) NOT NULL,
  tag VARCHAR(60) NULL,
  UNIQUE KEY uk_keyword_normalized(normalized)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS keyword_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  keyword_id BIGINT NOT NULL,
  score_delta INT NOT NULL,
  tag VARCHAR(60) NULL,
  description VARCHAR(200) NULL,
  weight INT NOT NULL DEFAULT 1,
  CONSTRAINT fk_keyword_rule_keyword FOREIGN KEY (keyword_id) REFERENCES keyword_dictionary(id),
  INDEX idx_keyword_rule_keyword(keyword_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS message_pool (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category VARCHAR(20) NOT NULL,
  tone VARCHAR(20) NOT NULL,
  min_score INT NOT NULL,
  max_score INT NOT NULL,
  required_tags VARCHAR(400) NULL,
  blocked_tags VARCHAR(400) NULL,
  conditions_json JSON NULL,
  text VARCHAR(800) NOT NULL,
  weight INT NOT NULL DEFAULT 1,
  priority INT NOT NULL DEFAULT 0,
  INDEX idx_message_cat(category),
  INDEX idx_message_score(min_score, max_score),
  INDEX idx_message_tone(tone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS longform_block (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  section VARCHAR(20) NOT NULL,
  tone VARCHAR(20) NOT NULL,
  min_score INT NOT NULL,
  max_score INT NOT NULL,
  required_tags VARCHAR(400) NULL,
  blocked_tags VARCHAR(400) NULL,
  conditions_json JSON NULL,
  text_template TEXT NOT NULL,
  weight INT NOT NULL DEFAULT 1,
  priority INT NOT NULL DEFAULT 0,
  INDEX idx_longform_section(section),
  INDEX idx_longform_score(min_score, max_score),
  INDEX idx_longform_tone(tone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS phrase_pool (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  slot_key VARCHAR(60) NOT NULL,
  tone VARCHAR(20) NOT NULL,
  text VARCHAR(220) NOT NULL,
  weight INT NOT NULL DEFAULT 1,
  INDEX idx_phrase_slot(slot_key),
  INDEX idx_phrase_tone(tone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS style_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tone VARCHAR(20) NOT NULL,
  emoji_rate INT NOT NULL,
  heading_style VARCHAR(30) NOT NULL,
  UNIQUE KEY uk_style_tone(tone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS horoscope_cache (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cache_key VARCHAR(120) NOT NULL,
  created_at DATETIME NOT NULL,
  response_json JSON NOT NULL,
  UNIQUE KEY uk_cache_key(cache_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
