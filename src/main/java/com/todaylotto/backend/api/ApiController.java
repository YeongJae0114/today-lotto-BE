package com.todaylotto.backend.api;


import com.todaylotto.backend.api.dto.ChoiceDto;
import com.todaylotto.backend.api.dto.GetQuestionsResponse;
import com.todaylotto.backend.api.dto.PostScoreRequest;
import com.todaylotto.backend.api.dto.PostScoreResponse;
import com.todaylotto.backend.api.dto.QuestionDto;
import com.todaylotto.backend.repo.ChoiceRepository;
import com.todaylotto.backend.service.QuestionService;
import com.todaylotto.backend.service.ScoringService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
  private final QuestionService questionService;
  private final ChoiceRepository choiceRepository;
  private final ScoringService scoringService;

  public ApiController(QuestionService questionService, ChoiceRepository choiceRepository, ScoringService scoringService) {
    this.questionService = questionService;
    this.choiceRepository = choiceRepository;
    this.scoringService = scoringService;
  }

  @GetMapping("/questions")
  public GetQuestionsResponse getQuestions() {
    QuestionService.QuestionSelection selection = questionService.generateQuestionSet();

    List<QuestionDto> questions = selection.questions().stream()
        .map(q -> new QuestionDto(q.getId(), q.getBucket().name(), q.getText()))
        .toList();

    List<ChoiceDto> choices = choiceRepository.findAll().stream()
        .sorted((a, b) -> Integer.compare(a.getValue(), b.getValue()))
        .map(c -> new ChoiceDto(c.getValue(), c.getLabel()))
        .toList();

    return new GetQuestionsResponse(selection.sessionSeed(), choices, questions);
  }

  @PostMapping("/score")
  public PostScoreResponse score(@Valid @RequestBody PostScoreRequest req) {
    return scoringService.score(req);
  }

  @GetMapping("/health")
  public String health() {
    return "ok";
  }
}
