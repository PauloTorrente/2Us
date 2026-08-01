package com.coupleapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// A block of time one partner is free/off work — day off, vacation, flexible schedule window.
// Matching these across partners is what lets the app find dates both are actually free.
@Entity
@Table(name = "availability_windows")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Whose availability this is
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Denormalized so overlap queries don't need to join through User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AvailabilityType type;

    // Optional free-text note (e.g. "Banco de horas", "Ferias contrato CLT")
    @Column(length = 200)
    private String label;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum AvailabilityType {
        DAY_OFF,   // A single/regular day off from work
        VACATION,  // Multi-day time off
        FLEXIBLE   // Open/negotiable schedule window
    }
}
