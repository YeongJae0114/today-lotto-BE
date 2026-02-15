package com.todaylotto.backend.repo;

import com.todaylotto.backend.domain.StrategyCardPool;
import com.todaylotto.backend.domain.StrategyCardType;
import com.todaylotto.backend.domain.Tone;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StrategyCardPoolRepository extends JpaRepository<StrategyCardPool, Long> {
  List<StrategyCardPool> findByCardTypeAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqualAndToneIn(
      StrategyCardType cardType,
      Integer minScore,
      Integer maxScore,
      List<Tone> tones
  );
}
