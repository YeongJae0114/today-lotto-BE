package com.todaylotto.backend.api.dto;

import java.util.List;

public record BreakdownDto(
    List<String> positiveSignals,
    List<String> cautionSignals,
    List<KeywordMatchDto> keywordMatches
) {}
