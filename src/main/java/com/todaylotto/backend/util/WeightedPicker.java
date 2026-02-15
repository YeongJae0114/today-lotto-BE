package com.todaylotto.backend.util;

import java.util.List;
import java.util.function.ToIntFunction;

public final class WeightedPicker {
  private WeightedPicker() {}

  public static <T> T pickWeighted(List<T> items, ToIntFunction<T> weightFn, SeededRng rng) {
    if (items == null || items.isEmpty()) return null;
    long total = 0;
    for (T it : items) {
      int w = Math.max(0, weightFn.applyAsInt(it));
      total += w;
    }
    if (total <= 0) {
      return items.get(rng.nextInt(items.size()));
    }
    long r = rng.nextLong(total);
    long acc = 0;
    for (T it : items) {
      acc += Math.max(0, weightFn.applyAsInt(it));
      if (r < acc) return it;
    }
    return items.get(items.size() - 1);
  }
}
