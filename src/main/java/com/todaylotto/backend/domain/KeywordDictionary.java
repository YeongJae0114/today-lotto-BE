package com.todaylotto.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "keyword_dictionary")
public class KeywordDictionary {
  @Id
  private Long id;

  @Column(name = "keyword", nullable = false, length = 80)
  private String keyword;

  @Column(name = "normalized", nullable = false, length = 80)
  private String normalized;

  @Column(name = "tag", length = 60)
  private String tag;

  protected KeywordDictionary() {}

  public Long getId() { return id; }
  public String getKeyword() { return keyword; }
  public String getNormalized() { return normalized; }
  public String getTag() { return tag; }
}
