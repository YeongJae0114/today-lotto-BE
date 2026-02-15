package com.todaylotto.backend.api.dto;

import java.util.List;
import java.util.Map;

public record PostScoreResponse(
    String actionConclusion,
    int score,
    String grade,
    String tone,
    Map<String, Integer> stateVector,
    List<String> tags,
    SignalsDto signals,
    List<StrategyCardDto> strategyCards,
    List<ResultCardDto> cards,
    String longformText,
    BreakdownDto breakdown,
    String shareText
) {}
