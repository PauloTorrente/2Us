package com.coupleapp.repository;

import com.coupleapp.entity.CalendarEvent;
import com.coupleapp.entity.CalendarEvent.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    // All events for a couple in a date range — used to build the monthly calendar view
    @Query("SELECT e FROM CalendarEvent e WHERE e.couple.id = :coupleId AND e.eventDate BETWEEN :start AND :end ORDER BY e.eventDate ASC")
    List<CalendarEvent> findByCoupleAndDateRange(@Param("coupleId") Long coupleId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // Events of a specific type — e.g. fetch only menstrual cycle entries
    List<CalendarEvent> findByCoupleIdAndEventType(Long coupleId, EventType eventType);

    // Recurring yearly events (anniversaries, birthdays) — used to generate annual reminders
    @Query("SELECT e FROM CalendarEvent e WHERE e.couple.id = :coupleId AND e.recurringYearly = true")
    List<CalendarEvent> findRecurringEvents(@Param("coupleId") Long coupleId);

    // Events coming up within a given number of days — used to trigger reminder notifications
    @Query("SELECT e FROM CalendarEvent e WHERE e.couple.id = :coupleId AND e.eventDate BETWEEN :today AND :until ORDER BY e.eventDate ASC")
    List<CalendarEvent> findUpcomingEvents(@Param("coupleId") Long coupleId, @Param("today") LocalDate today, @Param("until") LocalDate until);

    // Every event (across all couples) that has a reminder configured — scanned daily by ReminderScheduler
    @Query("SELECT e FROM CalendarEvent e WHERE e.reminderDaysBefore IS NOT NULL")
    List<CalendarEvent> findAllWithReminderEnabled();
}
