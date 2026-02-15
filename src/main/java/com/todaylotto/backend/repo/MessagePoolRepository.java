package com.todaylotto.backend.repo;


import com.todaylotto.backend.domain.MessageCategory;
import com.todaylotto.backend.domain.MessagePool;
import com.todaylotto.backend.domain.Tone;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessagePoolRepository extends JpaRepository<MessagePool, Long> {
  List<MessagePool> findByCategoryAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqualAndToneIn(
      MessageCategory category,
      Integer minScore,
      Integer maxScore,
      List<Tone> tones
  );
}
