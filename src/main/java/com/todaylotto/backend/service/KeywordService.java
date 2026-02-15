package com.todaylotto.backend.service;

import com.todaylotto.backend.api.dto.KeywordMatchDto;
import com.todaylotto.backend.domain.KeywordDictionary;
import com.todaylotto.backend.domain.KeywordRule;
import com.todaylotto.backend.repo.KeywordDictionaryRepository;
import com.todaylotto.backend.repo.KeywordRuleRepository;
import com.todaylotto.backend.util.SeededRng;
import com.todaylotto.backend.util.WeightedPicker;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;

@Service
public class KeywordService {

  private final KeywordDictionaryRepository dictionaryRepository;
  private final KeywordRuleRepository ruleRepository;

  public KeywordService(KeywordDictionaryRepository dictionaryRepository, KeywordRuleRepository ruleRepository) {
    this.dictionaryRepository = dictionaryRepository;
    this.ruleRepository = ruleRepository;
  }

  public KeywordResult analyze(String extraText, SeededRng rng) {
    if (extraText == null || extraText.isBlank()) {
      return new KeywordResult(0, List.of(), Set.of());
    }

    String normalized = normalize(extraText);

    List<KeywordDictionary> dict = dictionaryRepository.findAll();
    List<KeywordMatchDto> matches = new ArrayList<>();
    Set<String> tags = new HashSet<>();
    int sumDelta = 0;

    for (KeywordDictionary entry : dict) {
      String needle = entry.getNormalized();
      if (needle == null || needle.isBlank()) continue;
      if (!normalized.contains(needle)) continue;

      if (entry.getTag() != null && !entry.getTag().isBlank()) tags.add(entry.getTag());

      List<KeywordRule> rules = ruleRepository.findByKeyword_Id(entry.getId());
      if (rules == null || rules.isEmpty()) continue;

      KeywordRule chosen = WeightedPicker.pickWeighted(rules, r -> Math.max(1, r.getWeight()), rng);
      if (chosen == null) continue;

      sumDelta += chosen.getScoreDelta();
      if (chosen.getTag() != null && !chosen.getTag().isBlank()) tags.add(chosen.getTag());

      matches.add(new KeywordMatchDto(
          entry.getKeyword(),
          chosen.getDescription() == null ? "" : chosen.getDescription(),
          chosen.getScoreDelta(),
          chosen.getTag()
      ));
    }

    // clamp total keyword impact to [-5, +5]
    if (sumDelta > 5) sumDelta = 5;
    if (sumDelta < -5) sumDelta = -5;

    // keep up to 10 matches for readability
    if (matches.size() > 10) {
      rng.shuffle(matches);
      matches = matches.subList(0, 10);
    }

    return new KeywordResult(sumDelta, matches, tags);
  }

  private String normalize(String input) {
    String s = input.toLowerCase(Locale.ROOT);
    s = Normalizer.normalize(s, Normalizer.Form.NFKC);
    s = s.replaceAll("[^0-9a-zA-Z가-힣\\s]", " ");
    s = s.replaceAll("\\s+", " ").trim();
    return s;
  }

  public record KeywordResult(int scoreDelta, List<KeywordMatchDto> matches, Set<String> tags) {}
}
