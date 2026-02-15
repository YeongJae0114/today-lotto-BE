package com.todaylotto.backend.repo;


import com.todaylotto.backend.domain.KeywordRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRuleRepository extends JpaRepository<KeywordRule, Long> {
  List<KeywordRule> findByKeyword_Id(Long keywordId);
}
