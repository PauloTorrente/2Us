package com.coupleapp.repository;

import com.coupleapp.entity.FinanceGoal;
import com.coupleapp.entity.FinanceGoal.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceGoalRepository extends JpaRepository<FinanceGoal, Long> {

    // All goals for a couple — used to show the goals dashboard
    List<FinanceGoal> findByCoupleIdOrderByCreatedAtDesc(Long coupleId);

    // Active goals only — used in the home screen summary widget
    List<FinanceGoal> findByCoupleIdAndStatus(Long coupleId, GoalStatus status);
}
