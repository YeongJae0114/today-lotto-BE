package com.todaylotto.backend.service;

import com.todaylotto.backend.api.dto.AnswerDto;
import com.todaylotto.backend.api.dto.BreakdownDto;
import com.todaylotto.backend.api.dto.KeywordMatchDto;
import com.todaylotto.backend.api.dto.PostScoreRequest;
import com.todaylotto.backend.api.dto.PostScoreResponse;
import com.todaylotto.backend.api.dto.ResultCardDto;
import com.todaylotto.backend.api.dto.SignalsDto;
import com.todaylotto.backend.api.dto.StrategyCardDto;
import com.todaylotto.backend.domain.Axis;
import com.todaylotto.backend.domain.Question;
import com.todaylotto.backend.domain.Tone;
import com.todaylotto.backend.repo.QuestionRepository;
import com.todaylotto.backend.util.SeededRng;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ScoringService {
  private static final BigDecimal HALF = new BigDecimal("0.5");
  private static final BigDecimal ONE  = BigDecimal.ONE;

  private final QuestionRepository questionRepository;
  private final KeywordService keywordService;
  private final LongformService longformService;
  private final StrategyService strategyService;
  private final MessageService messageService;

  public ScoringService(
      QuestionRepository questionRepository,
      KeywordService keywordService,
      LongformService longformService,
      StrategyService strategyService,
      MessageService messageService
  ) {
    this.questionRepository = questionRepository;
    this.keywordService = keywordService;
    this.longformService = longformService;
    this.strategyService = strategyService;
    this.messageService = messageService;
  }

  public PostScoreResponse score(PostScoreRequest req) {
    LocalDate birth;
    try {
      birth = LocalDate.parse(req.birthDate());
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("birthDate must be YYYY-MM-DD");
    }

    SeededRng rng = new SeededRng(req.sessionSeed());

    // 1) state vector starts at 50
    EnumMap<Axis, Integer> axes = new EnumMap<>(Axis.class);
    for (Axis a : Axis.values()) axes.put(a, 50);

    applyBirthMonthAdjust(birth.getMonthValue(), axes);

    // 2) apply question deltas + tags
    Set<String> tags = new HashSet<>();

    for (AnswerDto a : req.answers()) {
      Question q = questionRepository.findById(a.questionId())
          .orElseThrow(() -> new IllegalArgumentException("Unknown questionId=" + a.questionId()));

      int deltaBase = (a.value() - 3) * 4; // -8,-4,0,+4,+8
      applyQuestionDelta(q, deltaBase, axes);

      if (a.value() >= 4 && q.getTagOnHigh() != null && !q.getTagOnHigh().isBlank()) tags.add(q.getTagOnHigh());
      if (a.value() <= 2 && q.getTagOnLow() != null && !q.getTagOnLow().isBlank()) tags.add(q.getTagOnLow());
    }

    addDerivedTags(axes, tags);

    // 3) keyword analysis
    KeywordService.KeywordResult keyword = keywordService.analyze(req.extraText(), rng);
    tags.addAll(keyword.tags());

    // 4) base score
    double base = 50
        + 0.24 * (axes.get(Axis.OPTIMISM) - 50)
        + 0.18 * (axes.get(Axis.STABILITY) - 50)
        + 0.18 * (axes.get(Axis.RISK) - 50)
        + 0.16 * (axes.get(Axis.FIN_EASE) - 50)
        + 0.12 * (axes.get(Axis.ENERGY) - 50)
        - 0.22 * (axes.get(Axis.IMPULSIVITY) - 50);

    // 5) interactions (exactly 4)
    List<String> interactions = new ArrayList<>();
    int interactionDelta = applyInteractions(axes, interactions);

    int score = clamp(Math.round((float) base) + interactionDelta + keyword.scoreDelta(), 0, 100);

    // 6) session noise -3..+3
    score = clamp(score + rng.nextInt(-3, 4), 0, 100);

    // 7) warning decision (may enforce DONT_BUY_TODAY tag)
    WarningLevel warningLevel = decideWarning(score, axes, tags);
    boolean warningMode = warningLevel != WarningLevel.NONE;
    if (warningMode) tags.add("DONT_BUY_TODAY");

    // 8) tone (sessionSeed based)
    Tone tone = pickTone(rng);

    // 9) strategy deck
    List<StrategyCardDto> strategyCards = strategyService.buildDeck(score, tone, axes, tags, warningMode, rng);

    // 10) result cards
    List<ResultCardDto> cards = messageService.pickResultCards(score, tone, axes, tags, warningMode, rng);

    // 11) longform report
    LongformService.LongformResult longform = longformService.generate(score, tone, axes, tags, warningMode, rng);

    // 12) signals/breakdown
    List<String> positive = buildPositiveSignals(score, axes, tags);
    List<String> caution = buildCautionSignals(score, axes, tags, warningLevel);

    Map<String, Integer> axisDetails = new LinkedHashMap<>();
    for (Axis a : Axis.values()) axisDetails.put(a.name(), axes.get(a));

    SignalsDto signals = new SignalsDto(
        positive,
        caution,
        keyword.matches().stream().map(KeywordMatchDto::keyword).toList(),
        interactions,
        axisDetails
    );

    BreakdownDto breakdown = new BreakdownDto(positive, caution, keyword.matches());

    String grade = gradeFor(score);
    String conclusion = conclusionFor(score, warningLevel, tone, rng);
    String shareText = shareTextFor(score, grade, conclusion, warningLevel, keyword.matches(), rng);

    Map<String, Integer> stateVector = new LinkedHashMap<>();
    for (Axis a : Axis.values()) stateVector.put(a.name(), axes.get(a));

    return new PostScoreResponse(
        conclusion,
        score,
        grade,
        longform.tone().name(),
        stateVector,
        tags.stream().sorted().toList(),
        signals,
        strategyCards,
        cards,
        longform.markdownText(),
        breakdown,
        shareText
    );
  }

  private void applyBirthMonthAdjust(int month, EnumMap<Axis, Integer> axes) {
    if (month >= 1 && month <= 3) axes.put(Axis.STABILITY, clamp(axes.get(Axis.STABILITY) + 5, 0, 100));
    if (month >= 4 && month <= 6) axes.put(Axis.OPTIMISM, clamp(axes.get(Axis.OPTIMISM) + 5, 0, 100));
    if (month >= 7 && month <= 9) axes.put(Axis.RISK, clamp(axes.get(Axis.RISK) + 5, 0, 100));
    if (month >= 10 && month <= 12) axes.put(Axis.IMPULSIVITY, clamp(axes.get(Axis.IMPULSIVITY) - 5, 0, 100));
  }

  private void applyQuestionDelta(Question q, int deltaBase, EnumMap<Axis, Integer> axes) {
    Axis primary = q.getPrimaryAxis();
    Axis secondary = q.getSecondaryAxis();

    BigDecimal strength = (q.getStrength() == null) ? ONE : q.getStrength();
    int polarity = (q.getPolarity() == null) ? 1 : q.getPolarity();

    BigDecimal base = BigDecimal.valueOf(deltaBase).multiply(strength).multiply(BigDecimal.valueOf(polarity));
    int pDelta = base.setScale(0, java.math.RoundingMode.HALF_UP).intValueExact();
    axes.put(primary, clamp(axes.get(primary) + pDelta, 0, 100));

    if (secondary != null) {
      BigDecimal sec = BigDecimal.valueOf(deltaBase)
              .multiply(strength)
              .multiply(HALF)
              .multiply(BigDecimal.valueOf(polarity));
      int sDelta = sec.setScale(0, java.math.RoundingMode.HALF_UP).intValueExact();
      axes.put(secondary, clamp(axes.get(secondary) + sDelta, 0, 100));
    }
  }

  private void addDerivedTags(EnumMap<Axis, Integer> axes, Set<String> tags) {
    if (axes.get(Axis.STABILITY) <= 35) tags.add("STABILITY_LOW");
    if (axes.get(Axis.STABILITY) >= 70) tags.add("STABILITY_HIGH");
    if (axes.get(Axis.IMPULSIVITY) >= 70) tags.add("IMPULSIVITY_HIGH");
    if (axes.get(Axis.IMPULSIVITY) <= 40) tags.add("IMPULSIVITY_LOW");
    if (axes.get(Axis.FIN_EASE) <= 40) tags.add("MONEY_TIGHT");
    if (axes.get(Axis.FIN_EASE) >= 70) tags.add("MONEY_EASY");
    if (axes.get(Axis.OPTIMISM) >= 75) tags.add("OPTIMISM_HIGH");
    if (axes.get(Axis.ENERGY) >= 70) tags.add("ENERGY_HIGH_TAG");
    if (axes.get(Axis.ENERGY) <= 35) tags.add("ENERGY_LOW_TAG");
  }

  private int applyInteractions(EnumMap<Axis, Integer> axes, List<String> interactions) {
    int delta = 0;

    // IMPULSIVITY≥70 AND FIN_EASE≤40 → -6
    if (axes.get(Axis.IMPULSIVITY) >= 70 && axes.get(Axis.FIN_EASE) <= 40) {
      delta -= 6;
      interactions.add("충동↑ + 여유↓ → -6");
    }
    // STABILITY≥70 AND IMPULSIVITY≤40 → +4
    if (axes.get(Axis.STABILITY) >= 70 && axes.get(Axis.IMPULSIVITY) <= 40) {
      delta += 4;
      interactions.add("평정↑ + 충동↓ → +4");
    }
    // OPTIMISM≥75 AND ENERGY≥70 → +3
    if (axes.get(Axis.OPTIMISM) >= 75 && axes.get(Axis.ENERGY) >= 70) {
      delta += 3;
      interactions.add("기분↑ + 컨디션↑ → +3");
    }
    // STABILITY≤35 AND IMPULSIVITY≥65 → -5
    if (axes.get(Axis.STABILITY) <= 35 && axes.get(Axis.IMPULSIVITY) >= 65) {
      delta -= 5;
      interactions.add("불안정 + 충동↑ → -5");
    }

    return delta;
  }

  private WarningLevel decideWarning(int score, EnumMap<Axis, Integer> axes, Set<String> tags) {
    boolean boosted =
        (axes.get(Axis.IMPULSIVITY) >= 70 && axes.get(Axis.FIN_EASE) <= 40)
            || axes.get(Axis.STABILITY) <= 35
            || tags.contains("STRESS_HIGH")
            || tags.contains("MONEY_TIGHT");

    if (score <= 25) return WarningLevel.STRONG;
    if (score <= 40) return boosted ? WarningLevel.STRONG : WarningLevel.NORMAL;
    if (boosted && score <= 55) return WarningLevel.NORMAL;
    return WarningLevel.NONE;
  }

  private Tone pickTone(SeededRng rng) {
    int n = rng.nextInt(4);
    return switch (n) {
      case 0 -> Tone.FUNNY;
      case 1 -> Tone.WARM;
      case 2 -> Tone.DRY;
      default -> Tone.COOL;
    };
  }

  private String gradeFor(int score) {
    if (score <= 25) return "🚫 오늘은 쉬는 게 이득(강한 말림)";
    if (score <= 45) return "🤔 굳이? 한 번 더 생각!(말림)";
    if (score <= 65) return "🙂 재미로 한 장 정도";
    if (score <= 85) return "😎 오늘 분위기 괜찮은데?";
    return "🎉 오늘은 가볍게 도전 데이";
  }

  private String conclusionFor(int score, WarningLevel warning, Tone tone, SeededRng rng) {
    if (warning == WarningLevel.STRONG) {
      return switch (tone) {
        case FUNNY -> "오늘은 로또 대신 ‘PASS’가 이기는 날! 지갑에게 하루 휴가를 주세요.";
        case WARM -> "오늘은 쉬어가는 게 좋아요. 재미는 좋지만, 책임 소비가 먼저예요.";
        case DRY -> "오늘은 구매 비추천. 충동 구매 위험이 감지되었습니다.";
        case COOL -> "오늘은 PASS가 더 멋져요. 룰을 지키는 게 간지.";
        default -> "오늘은 PASS 권장.";
      };
    }
    if (warning == WarningLevel.NORMAL) {
      return switch (tone) {
        case FUNNY -> "살 수는 있는데… 오늘은 ‘한도’부터 정하고 들어가자!";
        case WARM -> "가볍게 즐기되, 오늘은 한 장 룰 정도로만 가요.";
        case DRY -> "구매는 가능하나 제한 권장. 과몰입 방지 룰을 적용하세요.";
        case COOL -> "원하면 한 장. 대신 멈춤 규칙은 필수.";
        default -> "가볍게만 권장.";
      };
    }

    if (score <= 65) {
      return switch (tone) {
        case FUNNY -> "오늘은 ‘재미로 한 장’이 딱! 과금은 금지, 웃음은 허용.";
        case WARM -> "오늘은 가볍게 한 장 정도가 기분 전환에 좋아요.";
        case DRY -> "중립 구간. 소액·소량 원칙을 권장합니다.";
        case COOL -> "한 장이면 충분. 간결하게 가자.";
        default -> "재미로 한 장.";
      };
    }

    if (score <= 85) {
      return switch (tone) {
        case FUNNY -> "오? 오늘 분위기 괜찮은데? 그래도 ‘정해진 한도’ 안에서만!";
        case WARM -> "오늘은 기분이 좋아요. 다만 한도는 꼭 지켜요.";
        case DRY -> "지표 양호. 단, 과대 해석 금지.";
        case COOL -> "오늘은 무드 괜찮음. 룰만 지키면 완벽.";
        default -> "오늘 분위기 괜찮음.";
      };
    }

    return switch (tone) {
      case FUNNY -> "오늘은 ‘가볍게 도전 데이’! 하지만 지갑이 울면 즉시 종료!";
      case WARM -> "좋은 흐름이에요. 그래도 책임 소비는 항상 우선이에요.";
      case DRY -> "점수 상위 구간. 엔터테인먼트로만 접근하세요.";
      case COOL -> "오늘은 도전해도 됨. 대신 멈춤 규칙부터 박자.";
      default -> "오늘은 도전 데이.";
    };
  }

  private String shareTextFor(int score, String grade, String conclusion, WarningLevel warning, List<KeywordMatchDto> keywordMatches, SeededRng rng) {
    StringBuilder sb = new StringBuilder();
    sb.append("[오늘 로또 살까?]\n");
    sb.append("점수: ").append(score).append(" / 100\n");
    sb.append("등급: ").append(grade).append("\n");
    sb.append("결론: ").append(conclusion).append("\n");

    if (warning != WarningLevel.NONE) sb.append("권장: PASS 또는 0~1장 (책임 소비)\n");
    else sb.append("주의: 당첨 예측/보장 아님. 재미로만!\n");

    if (!keywordMatches.isEmpty()) {
      KeywordMatchDto one = rng.pickOne(keywordMatches);
      if (one != null && one.keyword() != null && !one.keyword().isBlank()) {
        sb.append("감지 키워드: ").append(one.keyword()).append("\n");
      }
    }

    sb.append("\n※ 이 앱은 재미용이며 무리한 구매를 권하지 않습니다.");
    return sb.toString();
  }

  private List<String> buildPositiveSignals(int score, EnumMap<Axis, Integer> axes, Set<String> tags) {
    List<String> list = new ArrayList<>();
    if (axes.get(Axis.STABILITY) >= 65) list.add("평정이 안정적");
    if (axes.get(Axis.OPTIMISM) >= 65) list.add("기분이 낙관적");
    if (axes.get(Axis.ENERGY) >= 65) list.add("컨디션이 좋은 편");
    if (axes.get(Axis.FIN_EASE) >= 65) list.add("재정 여유 신호");
    if (score >= 66) list.add("전체 점수 상향 구간");
    if (tags.contains("LUCKY_VIBE")) list.add("행운 무드 태그");
    return list;
  }

  private List<String> buildCautionSignals(int score, EnumMap<Axis, Integer> axes, Set<String> tags, WarningLevel warning) {
    List<String> list = new ArrayList<>();
    if (axes.get(Axis.IMPULSIVITY) >= 65) list.add("충동성이 높음");
    if (axes.get(Axis.STABILITY) <= 40) list.add("안정감이 낮음");
    if (axes.get(Axis.FIN_EASE) <= 45) list.add("재정 여유가 타이트");
    if (axes.get(Axis.ENERGY) <= 40) list.add("컨디션 저하");
    if (warning != WarningLevel.NONE) list.add("말림 모드(책임 소비 강화)");
    if (tags.contains("STRESS_HIGH")) list.add("스트레스 키워드 감지");
    return list;
  }

  private int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }

  private enum WarningLevel {
    NONE,
    NORMAL,
    STRONG
  }
}
