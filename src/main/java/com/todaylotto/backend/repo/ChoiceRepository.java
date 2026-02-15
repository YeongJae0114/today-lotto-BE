package com.todaylotto.backend.repo;


import com.todaylotto.backend.domain.Choice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoiceRepository extends JpaRepository<Choice, Integer> {}
