package com.todaylotto.backend.service;

import com.todaylotto.backend.domain.Question;
import com.todaylotto.backend.domain.QuestionBucket;
import com.todaylotto.backend.repo.QuestionRepository;
import com.todaylotto.backend.util.SeededRng;
import java.util.List;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class QuestionService {
  private final QuestionRepository questionRepository;

  public QuestionService(QuestionRepository questionRepository) {
    this.questionRepository = questionRepository;
  }

  public QuestionSelection generateQuestionSet() {
    String sessionSeed = UUID.randomUUID().toString();
    SeededRng rng = new SeededRng(sessionSeed);

    List<Question> optimism = new ArrayList<>(questionRepository.findByBucket(QuestionBucket.OPTIMISM));
    List<Question> stability = new ArrayList<>(questionRepository.findByBucket(QuestionBucket.STABILITY));
    List<Question> impulsivity = new ArrayList<>(questionRepository.findByBucket(QuestionBucket.IMPULSIVITY));
    List<Question> risk = new ArrayList<>(questionRepository.findByBucket(QuestionBucket.RISK));
    List<Question> mix = new ArrayList<>(questionRepository.findByBucket(QuestionBucket.MIX));

    rng.shuffle(optimism);
    rng.shuffle(stability);
    rng.shuffle(impulsivity);
    rng.shuffle(risk);
    rng.shuffle(mix);

    List<Question> selected = new ArrayList<>();
    selected.add(pickOneOrThrow(optimism, "OPTIMISM"));
    selected.add(pickOneOrThrow(stability, "STABILITY"));
    selected.add(pickOneOrThrow(impulsivity, "IMPULSIVITY"));
    selected.add(pickOneOrThrow(risk, "RISK"));

    if (mix.size() < 2) throw new IllegalStateException("Not enough MIX questions (need >=2).");
    selected.add(mix.get(0));
    selected.add(mix.get(1));

    rng.shuffle(selected);
    return new QuestionSelection(sessionSeed, selected);
  }

  private Question pickOneOrThrow(List<Question> list, String bucket) {
    if (list.isEmpty()) throw new IllegalStateException("Not enough questions for bucket " + bucket);
    return list.get(0);
  }

  public record QuestionSelection(String sessionSeed, List<Question> questions) {}
}
