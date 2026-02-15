package com.todaylotto.backend.service;


import com.todaylotto.backend.api.dto.StrategyCardDto;
import com.todaylotto.backend.domain.Axis;
import com.todaylotto.backend.domain.ScoreBand;
import com.todaylotto.backend.domain.StrategyCardPool;
import com.todaylotto.backend.domain.StrategyCardType;
import com.todaylotto.backend.domain.StrategyRuleMap;
import com.todaylotto.backend.domain.StrategySlotPool;
import com.todaylotto.backend.domain.Tone;
import com.todaylotto.backend.repo.StrategyCardPoolRepository;
import com.todaylotto.backend.repo.StrategyRuleMapRepository;
import com.todaylotto.backend.repo.StrategySlotPoolRepository;
import com.todaylotto.backend.util.ConditionContext;
import com.todaylotto.backend.util.ConditionEvaluator;
import com.todaylotto.backend.util.SeededRng;
import com.todaylotto.backend.util.TagUtils;
import com.todaylotto.backend.util.TemplateRenderer;
import com.todaylotto.backend.util.WeightedPicker;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StrategyService {

  private final StrategyCardPoolRepository cardRepo;
  private final StrategySlotPoolRepository slotRepo;
  private final StrategyRuleMapRepository ruleRepo;
  private final ConditionEvaluator conditionEvaluator;

  public StrategyService(StrategyCardPoolRepository cardRepo, StrategySlotPoolRepository slotRepo, StrategyRuleMapRepository ruleRepo, ConditionEvaluator conditionEvaluator) {
    this.cardRepo = cardRepo;
    this.slotRepo = slotRepo;
    this.ruleRepo = ruleRepo;
    this.conditionEvaluator = conditionEvaluator;
  }

  public List<StrategyCardDto> buildDeck(
      int score,
      Tone tone,
      Map<Axis, Integer> axes,
      Set<String> tags,
      boolean warningMode,
      SeededRng rng
  ) {
    ScoreBand band = scoreBand(score, warningMode);

    List<StrategyRuleMap> rules = ruleRepo.findByScoreBand(band);
    if (rules == null || rules.isEmpty()) throw new IllegalStateException("No strategy_rule_map for band=" + band);

    StrategyRuleMap rule = rng.pickOne(rules);

    List<StrategyCardType> mandatory = parseTypes(rule.getMandatoryTypes());
    List<StrategyCardType> optional = parseTypes(rule.getOptionalTypes());
    int maxCards = Math.max(2, Math.min(4, rule.getMaxCards()));

    ConditionContext ctx = new ConditionContext(score, axes, tags);

    List<StrategyCardDto> picked = new ArrayList<>();
    Set<Long> usedIds = new HashSet<>();
    Set<String> usedTypes = new HashSet<>();
    Set<String> usedTagKeys = new HashSet<>();

    for (StrategyCardType t : mandatory) {
      StrategyCardPool c = pickCard(t, score, tone, tags, ctx, usedIds, usedTypes, usedTagKeys, rng);
      if (c != null) {
        picked.add(renderCard(c, tone, rng));
        usedIds.add(c.getId());
        usedTypes.add(t.name());
      }
    }

    if (warningMode && picked.stream().noneMatch(c -> c.cardType().equals(StrategyCardType.BUY_INTENSITY.name()))) {
      StrategyCardPool forced = pickCard(StrategyCardType.BUY_INTENSITY, score, tone, tags, ctx, usedIds, usedTypes, usedTagKeys, rng);
      if (forced != null) {
        picked.add(0, renderCard(forced, tone, rng));
        usedIds.add(forced.getId());
        usedTypes.add(StrategyCardType.BUY_INTENSITY.name());
      }
    }

    while (picked.size() < maxCards && !optional.isEmpty()) {
      StrategyCardType t = rng.pickOne(optional);
      if (t == null) break;
      if (usedTypes.contains(t.name()) && picked.size() >= 2) {
        optional.remove(t);
        continue;
      }
      StrategyCardPool c = pickCard(t, score, tone, tags, ctx, usedIds, usedTypes, usedTagKeys, rng);
      if (c == null) {
        optional.remove(t);
        continue;
      }
      picked.add(renderCard(c, tone, rng));
      usedIds.add(c.getId());
      usedTypes.add(t.name());
    }

    if (picked.size() > 4) picked = picked.subList(0, 4);
    if (picked.size() < 2) {
      StrategyCardPool a = pickCard(StrategyCardType.SAFETY, score, tone, tags, ctx, usedIds, usedTypes, usedTagKeys, rng);
      StrategyCardPool b = pickCard(StrategyCardType.RULE, score, tone, tags, ctx, usedIds, usedTypes, usedTagKeys, rng);
      if (a != null) picked.add(renderCard(a, tone, rng));
      if (b != null) picked.add(renderCard(b, tone, rng));
    }
    return picked;
  }

  private ScoreBand scoreBand(int score, boolean warningMode) {
    if (warningMode) return ScoreBand.LOW;
    if (score <= 45) return ScoreBand.LOW;
    if (score <= 70) return ScoreBand.MID;
    return ScoreBand.HIGH;
  }

  private List<StrategyCardType> parseTypes(String csv) {
    if (csv == null || csv.isBlank()) return new ArrayList<>();
    List<StrategyCardType> out = new ArrayList<>();
    for (String s : csv.split(",")) {
      String t = s.trim();
      if (!t.isEmpty()) out.add(StrategyCardType.valueOf(t));
    }
    return out;
  }

  private StrategyCardPool pickCard(
      StrategyCardType type,
      int score,
      Tone tone,
      Set<String> tags,
      ConditionContext ctx,
      Set<Long> usedIds,
      Set<String> usedTypes,
      Set<String> usedTagKeys,
      SeededRng rng
  ) {
    List<Tone> tones = List.of(tone, Tone.ANY);
    List<StrategyCardPool> raw = cardRepo.findByCardTypeAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqualAndToneIn(type, score, score, tones);

    List<StrategyCardPool> candidates = new ArrayList<>();
    for (StrategyCardPool c : raw) {
      if (usedIds.contains(c.getId())) continue;
      if (!TagUtils.containsAll(tags, c.getRequiredTags())) continue;
      if (TagUtils.containsAny(tags, c.getBlockedTags())) continue;
      if (!conditionEvaluator.matches(c.getConditionsJson(), ctx)) continue;
      candidates.add(c);
    }
    if (candidates.isEmpty()) return null;

    candidates.sort(Comparator.comparing(StrategyCardPool::getPriority).reversed());
    int window = Math.min(120, candidates.size());

    StrategyCardPool chosen = WeightedPicker.pickWeighted(
        candidates.subList(0, window),
        c -> Math.max(1, c.getWeight() + c.getPriority()),
        rng
    );

    if (chosen != null && chosen.getTags() != null) {
      String key = type.name() + "|" + chosen.getTags();
      if (usedTagKeys.contains(key)) return null;
      usedTagKeys.add(key);
    }
    return chosen;
  }

  private StrategyCardDto renderCard(StrategyCardPool card, Tone tone, SeededRng rng) {
    Map<String, String> slots = new HashMap<>();
    String merged = card.getTitleTemplate() + "\n" + card.getBodyTemplate() + "\n" + card.getFooterTemplate();
    Set<String> keys = TemplateRenderer.extractSlots(merged);
    for (String k : keys) slots.put(k, pickSlot(k, tone, rng));

    String title = TemplateRenderer.render(card.getTitleTemplate(), slots);
    String body = TemplateRenderer.render(card.getBodyTemplate(), slots);
    String footer = TemplateRenderer.render(card.getFooterTemplate(), slots);

    return new StrategyCardDto(
        card.getCardType().name(),
        title,
        body,
        footer,
        new ArrayList<>(TagUtils.parseCsv(card.getTags()))
    );
  }

  private String pickSlot(String slotKey, Tone tone, SeededRng rng) {
    List<Tone> tones = List.of(tone, Tone.ANY);
    List<StrategySlotPool> options = slotRepo.findBySlotKeyAndToneIn(slotKey, tones);
    if (options == null || options.isEmpty()) return "";
    StrategySlotPool chosen = WeightedPicker.pickWeighted(options, o -> Math.max(1, o.getWeight()), rng);
    return chosen == null ? "" : chosen.getText();
  }
}
