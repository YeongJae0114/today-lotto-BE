package com.todaylotto.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "choice")
public class Choice {
  @Id
  @Column(name = "id")
  private Integer id;

  @Column(name = "value", nullable = false)
  private Integer value;

  @Column(name = "label", nullable = false, length = 50)
  private String label;

  protected Choice() {}

  public Integer getId() { return id; }
  public Integer getValue() { return value; }
  public String getLabel() { return label; }
}
