package com.todaylotto.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "strategy_slot_pool")
public class StrategySlotPool {
  @Id
  private Long id;

  @Column(name = "slot_key", nullable = false, length = 60)
  private String slotKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "tone", nullable = false, length = 20)
  private Tone tone;

  @Column(name = "text", nullable = false, length = 220)
  private String text;

  @Column(name = "weight", nullable = false)
  private Integer weight;

  protected StrategySlotPool() {}

  public Long getId() { return id; }
  public String getSlotKey() { return slotKey; }
  public Tone getTone() { return tone; }
  public String getText() { return text; }
  public Integer getWeight() { return weight; }
}
