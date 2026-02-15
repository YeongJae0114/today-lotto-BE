package com.todaylotto.backend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "question")
public class Question {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "bucket", nullable = false, length = 20)
  private QuestionBucket bucket;

  @Column(name = "text", nullable = false, length = 300)
  private String text;

  @Enumerated(EnumType.STRING)
  @Column(name = "primary_axis", nullable = false, length = 20)
  private Axis primaryAxis;

  @Enumerated(EnumType.STRING)
  @Column(name = "secondary_axis", length = 20)
  private Axis secondaryAxis;

  @Column(name = "strength", nullable = false, precision = 3, scale = 2)
  private BigDecimal strength;

  @Column(name = "polarity", nullable = false)
  private Integer polarity;

  @Column(name = "tag_on_high", length = 60)
  private String tagOnHigh;

  @Column(name = "tag_on_low", length = 60)
  private String tagOnLow;

  @Column(name = "weight", nullable = false)
  private Integer weight;

  protected Question() {}

  public Long getId() { return id; }
  public QuestionBucket getBucket() { return bucket; }
  public String getText() { return text; }
  public Axis getPrimaryAxis() { return primaryAxis; }
  public Axis getSecondaryAxis() { return secondaryAxis; }
  public BigDecimal getStrength() { return strength; }
  public Integer getPolarity() { return polarity; }
  public String getTagOnHigh() { return tagOnHigh; }
  public String getTagOnLow() { return tagOnLow; }
  public Integer getWeight() { return weight; }
}
