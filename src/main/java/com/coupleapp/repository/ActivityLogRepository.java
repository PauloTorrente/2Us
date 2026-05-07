package com.coupleapp.repository;

import com.coupleapp.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // All completions for one activity — for detailed activity history
    List<ActivityLog> findByActivityIdOrderByCompletedAtDesc(Long activityId);

    // Completions in a date range for a couple — feeds the score dashboard
    @Query("SELECT l FROM ActivityLog l WHERE l.activity.couple.id = :coupleId AND l.completedAt BETWEEN :start AND :end ORDER BY l.completedAt DESC")
    List<ActivityLog> findByCoupleAndDateRange(@Param("coupleId") Long coupleId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Completion count per activity in a period — [activityId, count] — for ranking most/least done
    @Query("SELECT l.activity.id, COUNT(l) FROM ActivityLog l WHERE l.activity.couple.id = :coupleId AND l.completedAt BETWEEN :start AND :end GROUP BY l.activity.id ORDER BY COUNT(l) DESC")
    List<Object[]> countCompletionsPerActivity(@Param("coupleId") Long coupleId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
