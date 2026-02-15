-- V9__create_strategy_tables.sql
CREATE TABLE IF NOT EXISTS strategy_card_pool (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  card_type VARCHAR(20) NOT NULL,
  tone VARCHAR(20) NOT NULL,
  min_score INT NOT NULL,
  max_score INT NOT NULL,
  required_tags VARCHAR(400) NULL,
  blocked_tags VARCHAR(400) NULL,
  conditions_json JSON NULL,
  title_template VARCHAR(255) NOT NULL,
  body_template VARCHAR(800) NOT NULL,
  footer_template VARCHAR(255) NOT NULL,
  weight INT NOT NULL DEFAULT 1,
  priority INT NOT NULL DEFAULT 0,
  tags VARCHAR(400) NULL,
  INDEX idx_strategy_type(card_type),
  INDEX idx_strategy_score(min_score, max_score),
  INDEX idx_strategy_tone(tone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS strategy_slot_pool (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  slot_key VARCHAR(60) NOT NULL,
  tone VARCHAR(20) NOT NULL,
  text VARCHAR(220) NOT NULL,
  weight INT NOT NULL DEFAULT 1,
  INDEX idx_strslot_key(slot_key),
  INDEX idx_strslot_tone(tone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS strategy_rule_map (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  score_band VARCHAR(10) NOT NULL,
  mandatory_types VARCHAR(300) NOT NULL,
  optional_types VARCHAR(300) NOT NULL,
  max_cards INT NOT NULL,
  dedupe_key_rules VARCHAR(200) NOT NULL,
  INDEX idx_rule_band(score_band)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
