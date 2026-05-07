package com.coupleapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Records every time the couple marks a shared activity as done.
// These logs are the raw data for the performance score dashboard.
@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which activity was completed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private SharedActivity activity;

    // Which partner logged the completion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logged_by_user_id", nullable = false)
    private User loggedBy;

    // Optional note about this session (e.g. "Ran 5km!", "Finished module 3")
    @Column(length = 300)
    private String note;

    // When the activity was completed — defaults to now but can be set manually
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
