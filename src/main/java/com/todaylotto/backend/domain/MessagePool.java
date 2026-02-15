package com.todaylotto.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "message_pool")
public class MessagePool {
  @Id
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 20)
  private MessageCategory category;

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

  @Column(name = "text", nullable = false, length = 800)
  private String text;

  @Column(name = "weight", nullable = false)
  private Integer weight;

  @Column(name = "priority", nullable = false)
  private Integer priority;

  protected MessagePool() {}

  public Long getId() { return id; }
  public MessageCategory getCategory() { return category; }
  public Tone getTone() { return tone; }
  public Integer getMinScore() { return minScore; }
  public Integer getMaxScore() { return maxScore; }
  public String getRequiredTags() { return requiredTags; }
  public String getBlockedTags() { return blockedTags; }
  public String getConditionsJson() { return conditionsJson; }
  public String getText() { return text; }
  public Integer getWeight() { return weight; }
  public Integer getPriority() { return priority; }
}
