-- V12__seed_strategy_rule_map.sql
-- 6+ rules: 2 variants per band to increase diversity
INSERT INTO strategy_rule_map (id, score_band, mandatory_types, optional_types, max_cards, dedupe_key_rules) VALUES
  (1, 'LOW',  'BUY_INTENSITY,SAFETY,RULE', 'BUDGET_CAP,DO,FUN_MISSION,TIMING', 4, 'card_type,tags'),
  (2, 'LOW',  'BUY_INTENSITY,RULE',       'SAFETY,BUDGET_CAP,DO,FUN_MISSION', 3, 'card_type,tags'),
  (3, 'MID',  'BUY_INTENSITY,BUDGET_CAP', 'RULE,SAFETY,DO,TIMING,FUN_MISSION', 4, 'card_type,tags'),
  (4, 'MID',  'BUY_INTENSITY,TIMING',     'BUDGET_CAP,RULE,SAFETY,DO,FUN_MISSION', 3, 'card_type,tags'),
  (5, 'HIGH', 'BUY_INTENSITY,TIMING',     'BUDGET_CAP,RULE,DO,FUN_MISSION,SAFETY', 4, 'card_type,tags'),
  (6, 'HIGH', 'BUY_INTENSITY,BUDGET_CAP', 'TIMING,RULE,DO,FUN_MISSION,SAFETY', 3, 'card_type,tags');
