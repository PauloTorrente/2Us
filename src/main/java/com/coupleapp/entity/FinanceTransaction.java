package com.coupleapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Records every financial transaction for a couple.
// Used to build spending reports, category breakdowns, and goal progress.
@Entity
@Table(name = "finance_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Short label (e.g. "Grocery run", "Netflix subscription")
    @Column(nullable = false, length = 150)
    private String description;

    // Always positive. Direction (income vs expense) is determined by the "type" field.
    // BigDecimal is used for money — never use float/double for currency (precision errors)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // INCOME or EXPENSE — drives how this affects the couple's balance
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    // Spending category for reports and tips (e.g. FOOD, TRANSPORT, LEISURE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinanceCategory category;

    // The couple this transaction belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    // Which partner recorded this transaction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_user_id", nullable = false)
    private User recordedBy;

    // The date the transaction actually occurred (may differ from entry date)
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    // Optional note (e.g. "Birthday dinner for mom")
    @Column(length = 300)
    private String note;

    // Whether this transaction repeats monthly (e.g. rent, subscriptions)
    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        INCOME,   // Money coming in (salary, freelance, etc.)
        EXPENSE   // Money going out
    }

    public enum FinanceCategory {
        HOUSING,      // Rent, mortgage, condo fees
        FOOD,         // Groceries and restaurants
        TRANSPORT,    // Fuel, Uber, public transit
        HEALTH,       // Medical, pharmacy, gym
        LEISURE,      // Bars, cinemas, events
        SUBSCRIPTIONS,// Netflix, Spotify, iCloud
        CLOTHING,     // Clothes and shoes
        EDUCATION,    // Courses, books
        SAVINGS,      // Money put aside
        OTHER         // Anything that doesn't fit above
    }
}
