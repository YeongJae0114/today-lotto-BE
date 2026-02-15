package com.todaylotto.backend.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostScoreRequest(
    @NotBlank String birthDate,
    @NotBlank String sessionSeed,
    @NotNull @Size(min = 6, max = 6) List<@Valid AnswerDto> answers,
    String extraText
) {}
