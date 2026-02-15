package com.todaylotto.backend.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "horoscope_cache")
public class HoroscopeCache {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "cache_key", nullable = false, unique = true, length = 120)
  private String cacheKey;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Lob
  @Column(name = "response_json", columnDefinition = "json", nullable = false)
  private String responseJson;

  protected HoroscopeCache() {}

  public Long getId() { return id; }
  public String getCacheKey() { return cacheKey; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public String getResponseJson() { return responseJson; }
}
