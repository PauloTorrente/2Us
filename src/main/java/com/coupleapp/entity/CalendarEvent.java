package com.coupleapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Stores all calendar entries for a couple: anniversaries, birthdays,
// menstrual cycle tracking, vacations, and any custom important dates.
@Entity
@Table(name = "calendar_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Label shown on the calendar (e.g. "Ana's birthday", "Vacation - Floripa")
    @Column(nullable = false, length = 150)
    private String title;

    // Optional longer description or notes
    @Column(length = 500)
    private String description;

    // The couple this event belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    // Who created this event — null means it was system-generated (e.g. anniversary)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    // The date this event occurs (or starts, for multi-day events)
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    // End date for multi-day events like vacations. Null = single-day event.
    @Column(name = "end_date")
    private LocalDate endDate;

    // What kind of event — used for calendar color-coding and notification logic
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventType eventType;

    // Whether this event repeats every year (e.g. birthdays, anniversary)
    @Column(name = "recurring_yearly")
    @Builder.Default
    private Boolean recurringYearly = false;

    // How many days before to send a reminder. Null = no reminder.
    @Column(name = "reminder_days_before")
    private Integer reminderDaysBefore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum EventType {
        ANNIVERSARY,      // The couple's relationship start date
        BIRTHDAY,         // Either partner's birthday
        MENSTRUAL_CYCLE,  // Tracked for health awareness — only visible to both if the user allows
        VACATION,         // Multi-day trip or time off
        APPOINTMENT,      // Doctor, dentist, etc.
        CUSTOM            // Any other date the couple wants to remember
    }
}
