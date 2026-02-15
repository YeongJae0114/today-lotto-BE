package com.todaylotto.backend.repo;


import com.todaylotto.backend.domain.Question;
import com.todaylotto.backend.domain.QuestionBucket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
  List<Question> findByBucket(QuestionBucket bucket);
}
