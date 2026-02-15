package com.todaylotto.backend.api.dto;

import java.util.List;

public record GetQuestionsResponse(String sessionSeed, List<ChoiceDto> choices, List<QuestionDto> questions) {}
