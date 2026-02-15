package com.todaylotto.backend.repo;

import com.todaylotto.backend.domain.ScoreBand;
import com.todaylotto.backend.domain.StrategyRuleMap;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;



public interface StrategyRuleMapRepository extends JpaRepository<StrategyRuleMap, Long> {
  List<StrategyRuleMap> findByScoreBand(ScoreBand scoreBand);
}
