package com.todaylotto.backend.repo;


import com.todaylotto.backend.domain.KeywordDictionary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordDictionaryRepository extends JpaRepository<KeywordDictionary, Long> {}
