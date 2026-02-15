package com.todaylotto.backend.service;

import com.todaylotto.backend.api.dto.ResultCardDto;
import com.todaylotto.backend.domain.Axis;
import com.todaylotto.backend.domain.MessageCategory;
import com.todaylotto.backend.domain.MessagePool;
import com.todaylotto.backend.domain.Tone;
import com.todaylotto.backend.repo.MessagePoolRepository;
import com.todaylotto.backend.util.ConditionContext;
import com.todaylotto.backend.util.ConditionEvaluator;
import com.todaylotto.backend.util.SeededRng;
import com.todaylotto.backend.util.TagUtils;
import com.todaylotto.backend.util.WeightedPicker;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageService {

  private final MessagePoolRepository messageRepo;

  private final ConditionEvaluator conditionEvaluator;

  public MessageService(MessagePoolRepository messageRepo, ConditionEvaluator conditionEvaluator) {
    this.messageRepo = messageRepo;
    this.conditionEvaluator = conditionEvaluator;
  }

  public List<ResultCardDto> pickResultCards(
      int score,
      Tone tone,
      Map<Axis, Integer> axes,
      Set<String> tags,
      boolean warningMode,
      SeededRng rng
  ) {
    List<ResultCardDto> out = new ArrayList<>();

    out.addAll(pickByCategory(MessageCategory.INSIGHT, 3, score, tone, axes, tags, rng));

    if (warningMode) {
      out.addAll(pickByCategory(MessageCategory.WARNING, 1, score, tone, axes, tags, rng));
      out.addAll(pickByCategory(MessageCategory.ALTERNATIVE, 1, score, tone, axes, tags, rng));
    }

    return out;
  }

  private List<ResultCardDto> pickByCategory(
      MessageCategory category,
      int count,
      int score,
      Tone tone,
      Map<Axis, Integer> axes,
      Set<String> tags,
      SeededRng rng
  ) {
    List<Tone> tones = List.of(tone, Tone.ANY);
    List<MessagePool> raw = messageRepo.findByCategoryAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqualAndToneIn(
        category, score, score, tones);

    ConditionContext ctx = new ConditionContext(score, axes, tags);

    List<MessagePool> candidates = new ArrayList<>();
    for (MessagePool m : raw) {
      if (!TagUtils.containsAll(tags, m.getRequiredTags())) continue;
      if (TagUtils.containsAny(tags, m.getBlockedTags())) continue;
      if (!conditionEvaluator.matches(m.getConditionsJson(), ctx)) continue;
      candidates.add(m);
    }

    candidates.sort(Comparator.comparing(MessagePool::getPriority).reversed());

    List<ResultCardDto> picked = new ArrayList<>();
    Set<Long> used = new HashSet<>();

    for (int i = 0; i < count; i++) {
      MessagePool chosen = pickOneDedup(candidates, used, rng);
      if (chosen == null) break;
      used.add(chosen.getId());

      String title = switch (category) {
        case INSIGHT -> "한 줄 코멘트";
        case WARNING -> "말림/경고";
        case ALTERNATIVE -> "대체 행동";
        case CONCLUSION -> "결론";
      };

      picked.add(new ResultCardDto(category.name(), title, chosen.getText()));
    }

    return picked;
  }

  private MessagePool pickOneDedup(List<MessagePool> candidates, Set<Long> used, SeededRng rng) {
    List<MessagePool> available = new ArrayList<>();
    for (MessagePool c : candidates) if (!used.contains(c.getId())) available.add(c);
    if (available.isEmpty()) return null;

    int window = Math.min(40, available.size());
    List<MessagePool> windowed = available.subList(0, window);

    return WeightedPicker.pickWeighted(windowed, m -> Math.max(1, m.getWeight() + m.getPriority()), rng);
  }
}
