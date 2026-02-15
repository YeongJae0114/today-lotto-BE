package com.todaylotto.backend.repo;


import com.todaylotto.backend.domain.PhrasePool;
import com.todaylotto.backend.domain.Tone;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhrasePoolRepository extends JpaRepository<PhrasePool, Long> {

  List<PhrasePool> findBySlotKeyAndToneIn(String slotKey, List<Tone> tones);
}
