package com.coupleapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// A financial goal the couple is saving toward: house, car, vacation, etc.
// Goals have a target amount and a deadline, allowing progress tracking.
@Entity
@Table(name = "finance_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // What they're saving for (e.g. "Trip to Japan", "New car")
    @Column(nullable = false, length = 150)
    private String title;

    // More detail about the goal if needed
    @Column(length = 500)
    private String description;

    // Total amount they need to reach the goal
    @Column(name = "target_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    // How much they've saved so far toward this goal
    @Column(name = "current_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;

    // When they want to achieve this goal by
    @Column(name = "target_date")
    private LocalDate targetDate;

    // Time horizon — used to categorize goals in the UI
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GoalHorizon horizon;

    // Whether this goal has been fully funded
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private GoalStatus status = GoalStatus.IN_PROGRESS;

    // The couple working toward this goal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum GoalHorizon {
        SHORT,  // Under 1 year (e.g. vacation)
        MEDIUM, // 1-3 years (e.g. new car)
        LONG    // 3+ years (e.g. house down payment)
    }

    public enum GoalStatus {
        IN_PROGRESS, // Actively saving
        ACHIEVED,    // Target amount reached
        ABANDONED    // Goal cancelled
    }
}
