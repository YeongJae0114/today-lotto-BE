package com.todaylotto.backend.api.dto;

import java.util.List;

public record StrategyCardDto(
    String cardType,
    String title,
    String body,
    String footer,
    List<String> tags
) {}
