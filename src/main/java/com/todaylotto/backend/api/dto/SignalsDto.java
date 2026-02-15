package com.todaylotto.backend.api.dto;

import java.util.List;
import java.util.Map;

public record SignalsDto(
    List<String> positiveSignals,
    List<String> cautionSignals,
    List<String> detectedKeywords,
    List<String> appliedInteractions,
    Map<String, Integer> axisDetails
) {}
