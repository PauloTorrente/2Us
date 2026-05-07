package com.coupleapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "couples")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Couple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 6)
    private String inviteCode;

    @Column(name = "couple_name")
    private String coupleName;

    @Column(name = "relationship_start_date")
    private LocalDate relationshipStartDate;

    @Column(name = "exact_date_known")
    private Boolean exactDateKnown;

    @Column(name = "anniversary_date")
    private LocalDate anniversaryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoupleStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = CoupleStatus.PENDING;
        }
        if (exactDateKnown == null) {
            exactDateKnown = true;
        }
    }
}
