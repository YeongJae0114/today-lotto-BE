package com.todaylotto.backend.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class TagUtils {
  private TagUtils() {}

  public static Set<String> parseCsv(String csv) {
    if (csv == null || csv.isBlank()) return Collections.emptySet();
    Set<String> out = new HashSet<>();
    Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .forEach(out::add);
    return out;
  }

  public static boolean containsAll(Set<String> tags, String requiredCsv) {
    Set<String> required = parseCsv(requiredCsv);
    return tags.containsAll(required);
  }

  public static boolean containsAny(Set<String> tags, String blockedCsv) {
    Set<String> blocked = parseCsv(blockedCsv);
    for (String b : blocked) {
      if (tags.contains(b)) return true;
    }
    return false;
  }
}
