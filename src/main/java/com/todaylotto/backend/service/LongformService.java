package com.todaylotto.backend.service;

import com.todaylotto.backend.domain.Axis;
import com.todaylotto.backend.domain.LongformBlock;
import com.todaylotto.backend.domain.LongformSection;
import com.todaylotto.backend.domain.PhrasePool;
import com.todaylotto.backend.domain.StyleProfile;
import com.todaylotto.backend.domain.Tone;
import com.todaylotto.backend.repo.LongformBlockRepository;
import com.todaylotto.backend.repo.PhrasePoolRepository;
import com.todaylotto.backend.repo.StyleProfileRepository;
import com.todaylotto.backend.util.ConditionContext;
import com.todaylotto.backend.util.ConditionEvaluator;
import com.todaylotto.backend.util.SeededRng;
import com.todaylotto.backend.util.TagUtils;
import com.todaylotto.backend.util.TemplateRenderer;
import com.todaylotto.backend.util.WeightedPicker;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LongformService {

  private final LongformBlockRepository blockRepo;
  private final PhrasePoolRepository phraseRepo;
  private final StyleProfileRepository styleRepo;
  private final ConditionEvaluator conditionEvaluator;

  public LongformService(LongformBlockRepository blockRepo, PhrasePoolRepository phraseRepo, StyleProfileRepository styleRepo, ConditionEvaluator conditionEvaluator) {
    this.blockRepo = blockRepo;
    this.phraseRepo = phraseRepo;
    this.styleRepo = styleRepo;
    this.conditionEvaluator = conditionEvaluator;
  }

  public LongformResult generate(
      int score,
      Tone tone,
      Map<Axis, Integer> axes,
      Set<String> tags,
      boolean warningMode,
      SeededRng rng
  ) {
    StyleProfile profile = styleRepo.findByTone(tone).orElse(null);
    String heading = profile == null ? "##" : profile.getHeadingStyle();
    int emojiRate = profile == null ? 15 : profile.getEmojiRate();

    List<LongformSection> sections = decideSections(warningMode, rng);
    ConditionContext ctx = new ConditionContext(score, axes, tags);

    StringBuilder md = new StringBuilder();
    for (LongformSection section : sections) {
      md.append(heading).append(" ").append(titleFor(section, tone)).append("\n");

      String paragraph = pickAndRenderBlock(section, score, tone, ctx, tags, rng);
      if (paragraph.isBlank()) paragraph = fallbackText(section, tone);

      if (rng.nextInt(100) < emojiRate) paragraph = paragraph + emojiForTone(tone, rng);

      md.append(paragraph).append("\n\n");
    }

    return new LongformResult(tone, md.toString().trim());
  }

  private List<LongformSection> decideSections(boolean warningMode, SeededRng rng) {
    if (warningMode) {
      List<LongformSection> base = new ArrayList<>(List.of(
          LongformSection.OPENING,
          LongformSection.ANALYSIS,
          LongformSection.CAUTION,
          LongformSection.TIP,
          LongformSection.CONCLUSION
      ));
      if (rng.nextInt(100) < 55) base.add(3, LongformSection.STRATEGY);
      return base;
    }

    List<LongformSection> base = new ArrayList<>(List.of(
        LongformSection.OPENING,
        LongformSection.ANALYSIS,
        LongformSection.TIP,
        LongformSection.STRATEGY,
        LongformSection.CONCLUSION
    ));
    if (rng.nextInt(100) < 35) base.add(4, LongformSection.FUN);
    return base;
  }

  private String pickAndRenderBlock(
      LongformSection section,
      int score,
      Tone tone,
      ConditionContext ctx,
      Set<String> tags,
      SeededRng rng
  ) {
    List<Tone> tones = List.of(tone, Tone.ANY);
    List<LongformBlock> raw = blockRepo.findBySectionAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqualAndToneIn(section, score, score, tones);

    List<LongformBlock> candidates = new ArrayList<>();
    for (LongformBlock b : raw) {
      if (!TagUtils.containsAll(tags, b.getRequiredTags())) continue;
      if (TagUtils.containsAny(tags, b.getBlockedTags())) continue;
      if (!conditionEvaluator.matches(b.getConditionsJson(), ctx)) continue;
      candidates.add(b);
    }

    if (candidates.isEmpty()) return "";
    candidates.sort(Comparator.comparing(LongformBlock::getPriority).reversed());

    int window = Math.min(60, candidates.size());
    LongformBlock chosen = WeightedPicker.pickWeighted(
        candidates.subList(0, window),
        b -> Math.max(1, b.getWeight() + b.getPriority()),
        rng
    );
    if (chosen == null) return "";

    String template = chosen.getTextTemplate();
    Set<String> slots = TemplateRenderer.extractSlots(template);

    Map<String, String> slotValues = new HashMap<>();
    for (String slot : slots) slotValues.put(slot, pickPhrase(slot, tone, rng));

    return TemplateRenderer.render(template, slotValues);
  }

  private String pickPhrase(String slotKey, Tone tone, SeededRng rng) {
    List<Tone> tones = List.of(tone, Tone.ANY);
    List<PhrasePool> phrases = phraseRepo.findBySlotKeyAndToneIn(slotKey, tones);
    if (phrases == null || phrases.isEmpty()) return "";
    PhrasePool chosen = WeightedPicker.pickWeighted(phrases, p -> Math.max(1, p.getWeight()), rng);
    return chosen == null ? "" : chosen.getText();
  }

  private String titleFor(LongformSection section, Tone tone) {
    return switch (section) {
      case OPENING -> tone == Tone.DRY ? "요약" : "오늘의 오프닝";
      case ANALYSIS -> "지표 분석";
      case TIP -> "가벼운 팁";
      case CAUTION -> "과몰입 방지";
      case STRATEGY -> "오늘의 전략";
      case CONCLUSION -> "결론";
      case FUN -> "재미 요소";
    };
  }

  private String emojiForTone(Tone tone, SeededRng rng) {
    return switch (tone) {
      case FUNNY -> rng.pickOne(List.of(" 😆", " 🎲", " 🍀", " 🤹"));
      case WARM -> rng.pickOne(List.of(" 🙂", " ☕", " 🌿", " ✨"));
      case DRY -> rng.pickOne(List.of(" 📌", " 🧾", " ✅", " ⏱️"));
      case COOL -> rng.pickOne(List.of(" 😎", " 🧊", " 🔥", " 🛰️"));
      default -> "";
    };
  }

  private String fallbackText(LongformSection section, Tone tone) {
    return switch (section) {
      case OPENING -> "오늘 리포트는 재미를 위한 참고용이에요. 당첨을 예측하는 기능은 없습니다.";
      case ANALYSIS -> "응답 패턴상 현재 컨디션과 소비 리듬이 함께 움직이는 날로 보여요. 무리하지 않는 선에서만 접근해 주세요.";
      case TIP -> "구매 전 30초만 멈춰서 ‘오늘 목표’와 ‘한도’를 정해보면 만족도가 확 올라갑니다.";
      case CAUTION -> "점수가 낮게 나왔다면 ‘사지 말기’가 더 멋진 선택일 수 있어요. 재미는 재미로만!";
      case STRATEGY -> "오늘은 단순한 룰(장수/한도/멈춤)을 정하는 게 핵심 전략입니다.";
      case CONCLUSION -> tone == Tone.DRY ? "결론: 책임 소비를 최우선으로 판단하세요." : "오늘의 결론은 ‘가볍게, 책임 있게’입니다.";
      case FUN -> "재미 미션: 편의점에서 가장 작은 행운의 간식을 하나 골라보세요.";
    };
  }

  public record LongformResult(Tone tone, String markdownText) {}
}
