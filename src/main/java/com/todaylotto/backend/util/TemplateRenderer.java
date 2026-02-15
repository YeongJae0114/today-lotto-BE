package com.todaylotto.backend.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TemplateRenderer {
  private static final Pattern SLOT_PATTERN = Pattern.compile("\\{([A-Z0-9_]+)\\}");

  private TemplateRenderer() {}

  public static Set<String> extractSlots(String template) {
    Set<String> out = new HashSet<>();
    if (template == null) return out;
    Matcher m = SLOT_PATTERN.matcher(template);
    while (m.find()) out.add(m.group(1));
    return out;
  }

  public static String render(String template, Map<String, String> slotValues) {
    if (template == null) return "";
    Matcher m = SLOT_PATTERN.matcher(template);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String key = m.group(1);
      String replacement = slotValues.getOrDefault(key, "");
      m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    m.appendTail(sb);
    return sb.toString();
  }
}
