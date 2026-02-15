package com.todaylotto.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "style_profile")
public class StyleProfile {
  @Id
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "tone", nullable = false, length = 20)
  private Tone tone;

  @Column(name = "emoji_rate", nullable = false)
  private Integer emojiRate;

  @Column(name = "heading_style", nullable = false, length = 30)
  private String headingStyle;

  protected StyleProfile() {}

  public Long getId() { return id; }
  public Tone getTone() { return tone; }
  public Integer getEmojiRate() { return emojiRate; }
  public String getHeadingStyle() { return headingStyle; }
}
