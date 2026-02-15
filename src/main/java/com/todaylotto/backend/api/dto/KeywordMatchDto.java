package com.todaylotto.backend.api.dto;

public record KeywordMatchDto(
    String keyword,
    String ruleDescription,
    Integer scoreDelta,
    String tagApplied
) {}
