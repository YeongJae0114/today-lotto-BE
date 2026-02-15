package com.todaylotto.backend.repo;


import com.todaylotto.backend.domain.LongformBlock;
import com.todaylotto.backend.domain.LongformSection;
import com.todaylotto.backend.domain.Tone;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LongformBlockRepository extends JpaRepository<LongformBlock, Long> {
  List<LongformBlock> findBySectionAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqualAndToneIn(
      LongformSection section,
      Integer minScore,
      Integer maxScore,
      List<Tone> tones
  );
}
