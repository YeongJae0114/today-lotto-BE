package com.todaylotto.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "keyword_rule")
public class KeywordRule {
  @Id
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "keyword_id", nullable = false)
  private KeywordDictionary keyword;

  @Column(name = "score_delta", nullable = false)
  private Integer scoreDelta;

  @Column(name = "tag", length = 60)
  private String tag;

  @Column(name = "description", length = 200)
  private String description;

  @Column(name = "weight", nullable = false)
  private Integer weight;

  protected KeywordRule() {}

  public Long getId() { return id; }
  public KeywordDictionary getKeyword() { return keyword; }
  public Integer getScoreDelta() { return scoreDelta; }
  public String getTag() { return tag; }
  public String getDescription() { return description; }
  public Integer getWeight() { return weight; }
}
