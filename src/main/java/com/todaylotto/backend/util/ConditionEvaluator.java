package com.todaylotto.backend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylotto.backend.domain.Axis;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ConditionEvaluator {
  private final ObjectMapper objectMapper;

  public ConditionEvaluator(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public boolean matches(String conditionsJson, ConditionContext ctx) {
    if (conditionsJson == null || conditionsJson.isBlank()) return true;
    String trimmed = conditionsJson.trim();
    if (trimmed.equals("{}")) return true;
    try {
      Map<String, Object> root = objectMapper.readValue(trimmed, new TypeReference<>() {});
      return evalNode(root, ctx);
    } catch (Exception e) {
      // fail-open (content problems should not break the app)
      return true;
    }
  }

  @SuppressWarnings("unchecked")
  private boolean evalNode(Map<String, Object> node, ConditionContext ctx) {
    if (node == null || node.isEmpty()) return true;

    if (node.containsKey("all")) {
      List<Object> list = (List<Object>) node.get("all");
      if (list == null) return true;
      for (Object o : list) if (!evalAny(o, ctx)) return false;
      return true;
    }
    if (node.containsKey("any")) {
      List<Object> list = (List<Object>) node.get("any");
      if (list == null || list.isEmpty()) return true;
      for (Object o : list) if (evalAny(o, ctx)) return true;
      return false;
    }

    String type = asString(node.get("type"));
    if (type == null) return true;

    return switch (type) {
      case "axis" -> evalAxis(node, ctx);
      case "score" -> evalScore(node, ctx);
      case "tag" -> evalTag(node, ctx);
      default -> true;
    };
  }

  @SuppressWarnings("unchecked")
  private boolean evalAny(Object o, ConditionContext ctx) {
    if (o instanceof Map<?, ?> m) return evalNode((Map<String, Object>) m, ctx);
    return true;
  }

  private boolean evalAxis(Map<String, Object> node, ConditionContext ctx) {
    String axisStr = asString(node.get("axis"));
    String op = asString(node.get("op"));
    Integer value = asInt(node.get("value"));
    if (axisStr == null || op == null || value == null) return true;
    Axis axis;
    try {
      axis = Axis.valueOf(axisStr);
    } catch (Exception e) {
      return true;
    }
    int actual = ctx.axes().getOrDefault(axis, 50);
    return compare(actual, op, value);
  }

  private boolean evalScore(Map<String, Object> node, ConditionContext ctx) {
    String op = asString(node.get("op"));
    Integer value = asInt(node.get("value"));
    if (op == null || value == null) return true;
    return compare(ctx.score(), op, value);
  }

  private boolean evalTag(Map<String, Object> node, ConditionContext ctx) {
    String op = asString(node.get("op"));
    String value = asString(node.get("value"));
    if (op == null || value == null) return true;
    boolean has = ctx.tags().contains(value);
    return switch (op) {
      case "has" -> has;
      case "not" -> !has;
      default -> true;
    };
  }

  private boolean compare(int actual, String op, int expected) {
    return switch (op) {
      case ">" -> actual > expected;
      case ">=" -> actual >= expected;
      case "<" -> actual < expected;
      case "<=" -> actual <= expected;
      case "==" -> actual == expected;
      case "!=" -> actual != expected;
      default -> true;
    };
  }

  private String asString(Object o) {
    if (o == null) return null;
    return String.valueOf(o);
  }

  private Integer asInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(String.valueOf(o));
    } catch (Exception e) {
      return null;
    }
  }
}
