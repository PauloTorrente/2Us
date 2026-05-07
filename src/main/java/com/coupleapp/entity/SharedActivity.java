package com.coupleapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// Represents a recurring activity the couple does together (e.g. gym, online course, cooking).
// Each activity has a completion log that feeds the performance score dashboard.
@Entity
@Table(name = "shared_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name of the activity (e.g. "Morning run", "Watch cooking course")
    @Column(nullable = false, length = 100)
    private String name;

    // Optional description or goal for this activity
    @Column(length = 300)
    private String description;

    // The couple who tracks this activity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    // How often this activity is expected (used to calculate compliance score)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityFrequency frequency;

    // Category for grouping in the score dashboard
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityCategory category;

    // Whether this activity is still active or has been archived
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Total times this activity has been completed — incremented on each log entry
    @Column(name = "completion_count")
    @Builder.Default
    private Integer completionCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ActivityFrequency {
        DAILY,
        WEEKLY,
        BIWEEKLY,
        MONTHLY
    }

    public enum ActivityCategory {
        FITNESS,   // Gym, running, yoga
        LEARNING,  // Courses, books, podcasts together
        LEISURE,   // Movies, games, walks
        HEALTH,    // Meditation, therapy, doctor visits
        OTHER
    }
}
