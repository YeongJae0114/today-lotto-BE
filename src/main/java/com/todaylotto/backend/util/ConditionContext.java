package com.todaylotto.backend.util;


import com.todaylotto.backend.domain.Axis;
import java.util.Map;
import java.util.Set;

public record ConditionContext(int score, Map<Axis, Integer> axes, Set<String> tags) {}
