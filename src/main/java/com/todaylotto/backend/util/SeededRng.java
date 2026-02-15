package com.todaylotto.backend.util;

import java.util.List;
import java.util.SplittableRandom;
import java.util.UUID;

/**
 * Deterministic RNG derived from sessionSeed (UUID).
 * - A request is consistent within the same sessionSeed
 * - Every new sessionSeed produces different selection/phrasing
 */
public class SeededRng {
  private final SplittableRandom random;

  public SeededRng(String sessionSeed) {
    long seed;
    try {
      UUID uuid = UUID.fromString(sessionSeed);
      seed = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
    } catch (IllegalArgumentException e) {
      seed = sessionSeed.hashCode();
    }
    this.random = new SplittableRandom(seed);
  }

  public int nextInt(int boundExclusive) {
    return random.nextInt(boundExclusive);
  }

  public int nextInt(int originInclusive, int boundExclusive) {
    return random.nextInt(originInclusive, boundExclusive);
  }

  public long nextLong(long boundExclusive) {
    return random.nextLong(boundExclusive);
  }

  public int sign() {
    return random.nextBoolean() ? 1 : -1;
  }

  public <T> void shuffle(List<T> list) {
    for (int i = list.size() - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      T tmp = list.get(i);
      list.set(i, list.get(j));
      list.set(j, tmp);
    }
  }

  public <T> T pickOne(List<T> list) {
    if (list == null || list.isEmpty()) return null;
    return list.get(random.nextInt(list.size()));
  }
}
