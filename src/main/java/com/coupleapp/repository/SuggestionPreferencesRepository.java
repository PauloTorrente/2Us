package com.coupleapp.repository;

import com.coupleapp.entity.SuggestionPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuggestionPreferencesRepository extends JpaRepository<SuggestionPreferences, Long> {

    // Find preferences by couple ID — each couple has exactly one preferences record
    Optional<SuggestionPreferences> findByCoupleId(Long coupleId);

    // Check if preferences exist for a couple
    boolean existsByCoupleId(Long coupleId);
}
