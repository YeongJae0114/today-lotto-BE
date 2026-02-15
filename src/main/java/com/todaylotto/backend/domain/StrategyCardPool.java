package com.todaylotto.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "strategy_card_pool")
public class StrategyCardPool {
  @Id
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "card_type", nullable = false, length = 20)
  private StrategyCardType cardType;

  @Enumerated(EnumType.STRING)
  @Column(name = "tone", nullable = false, length = 20)
  private Tone tone;

  @Column(name = "min_score", nullable = false)
  private Integer minScore;

  @Column(name = "max_score", nullable = false)
  private Integer maxScore;

  @Column(name = "required_tags", length = 400)
  private String requiredTags;

  @Column(name = "blocked_tags", length = 400)
  private String blockedTags;

  @Column(name = "conditions_json", columnDefinition = "json")
  private String conditionsJson;

  @Column(name = "title_template", nullable = false, length = 255)
  private String titleTemplate;

  @Column(name = "body_template", nullable = false, length = 800)
  private String bodyTemplate;

  @Column(name = "footer_template", nullable = false, length = 255)
  private String footerTemplate;

  @Column(name = "weight", nullable = false)
  private Integer weight;

  @Column(name = "priority", nullable = false)
  private Integer priority;

  @Column(name = "tags", length = 400)
  private String tags;

  protected StrategyCardPool() {}

  public Long getId() { return id; }
  public StrategyCardType getCardType() { return cardType; }
  public Tone getTone() { return tone; }
  public Integer getMinScore() { return minScore; }
  public Integer getMaxScore() { return maxScore; }
  public String getRequiredTags() { return requiredTags; }
  public String getBlockedTags() { return blockedTags; }
  public String getConditionsJson() { return conditionsJson; }
  public String getTitleTemplate() { return titleTemplate; }
  public String getBodyTemplate() { return bodyTemplate; }
  public String getFooterTemplate() { return footerTemplate; }
  public Integer getWeight() { return weight; }
  public Integer getPriority() { return priority; }
  public String getTags() { return tags; }
}
