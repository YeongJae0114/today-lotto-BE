package com.todaylotto.backend.repo;

import com.todaylotto.backend.domain.StyleProfile;
import com.todaylotto.backend.domain.Tone;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StyleProfileRepository extends JpaRepository<StyleProfile, Long> {
  Optional<StyleProfile> findByTone(Tone tone);
}
