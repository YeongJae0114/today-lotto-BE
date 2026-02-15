package com.todaylotto.backend.repo;


import com.todaylotto.backend.domain.StrategySlotPool;
import com.todaylotto.backend.domain.Tone;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StrategySlotPoolRepository extends JpaRepository<StrategySlotPool, Long> {
  List<StrategySlotPool> findBySlotKeyAndToneIn(String slotKey, List<Tone> tones);
}
