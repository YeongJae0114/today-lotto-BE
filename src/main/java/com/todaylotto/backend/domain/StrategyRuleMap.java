package com.todaylotto.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "strategy_rule_map")
public class StrategyRuleMap {
  @Id
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "score_band", nullable = false, length = 10)
  private ScoreBand scoreBand;

  @Column(name = "mandatory_types", nullable = false, length = 300)
  private String mandatoryTypes;

  @Column(name = "optional_types", nullable = false, length = 300)
  private String optionalTypes;

  @Column(name = "max_cards", nullable = false)
  private Integer maxCards;

  @Column(name = "dedupe_key_rules", nullable = false, length = 200)
  private String dedupeKeyRules;

  protected StrategyRuleMap() {}

  public Long getId() { return id; }
  public ScoreBand getScoreBand() { return scoreBand; }
  public String getMandatoryTypes() { return mandatoryTypes; }
  public String getOptionalTypes() { return optionalTypes; }
  public Integer getMaxCards() { return maxCards; }
  public String getDedupeKeyRules() { return dedupeKeyRules; }
}
