package com.coupleapp.repository;

import com.coupleapp.entity.SharedActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedActivityRepository extends JpaRepository<SharedActivity, Long> {

    // Active activities for a couple — shown in the main activities screen
    List<SharedActivity> findByCoupleIdAndIsActiveTrueOrderByCompletionCountDesc(Long coupleId);

    // All activities including archived — for history/analytics
    List<SharedActivity> findByCoupleIdOrderByCompletionCountDesc(Long coupleId);
}
