package com.coupleapp.service.impl;

import com.coupleapp.entity.CalendarEvent;
import com.coupleapp.entity.Notification.NotificationType;
import com.coupleapp.entity.User;
import com.coupleapp.repository.CalendarEventRepository;
import com.coupleapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

// Scans every calendar event with a reminder configured and notifies both partners
// exactly N days before it happens.
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final CalendarEventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Runs daily at 08:00. Idempotent — NotificationService.alreadySentToday() guards against
    // duplicate reminders if the job is re-run or restarted on the same day.
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendDueReminders() {
        LocalDate today = LocalDate.now();
        List<CalendarEvent> eventsWithReminder = eventRepository.findAllWithReminderEnabled();
        int fired = 0;

        for (CalendarEvent event : eventsWithReminder) {
            LocalDate occurrence = occurrenceDueToday(event, today);
            if (occurrence != null) {
                notifyCouple(event, occurrence);
                fired++;
            }
        }

        log.info("ReminderScheduler: checked {} events, fired {} reminders for {}",
                eventsWithReminder.size(), fired, today);
    }

    // Returns the occurrence date if today is exactly reminderDaysBefore days ahead of it.
    // Recurring events are checked against both this year and next, to handle year boundaries.
    private LocalDate occurrenceDueToday(CalendarEvent event, LocalDate today) {
        int reminderDays = event.getReminderDaysBefore();

        if (Boolean.TRUE.equals(event.getRecurringYearly())) {
            LocalDate thisYear = withYearSafe(event.getEventDate(), today.getYear());
            if (thisYear.minusDays(reminderDays).isEqual(today)) return thisYear;

            LocalDate nextYear = withYearSafe(event.getEventDate(), today.getYear() + 1);
            if (nextYear.minusDays(reminderDays).isEqual(today)) return nextYear;

            return null;
        }

        return event.getEventDate().minusDays(reminderDays).isEqual(today) ? event.getEventDate() : null;
    }

    // Re-anchors a month/day to a target year without blowing up on Feb 29 in a non-leap year.
    private LocalDate withYearSafe(LocalDate date, int year) {
        int lastDayOfMonth = YearMonth.of(year, date.getMonthValue()).lengthOfMonth();
        return LocalDate.of(year, date.getMonthValue(), Math.min(date.getDayOfMonth(), lastDayOfMonth));
    }

    private void notifyCouple(CalendarEvent event, LocalDate occurrence) {
        NotificationType type = event.getEventType() == CalendarEvent.EventType.ANNIVERSARY
                ? NotificationType.ANNIVERSARY_REMINDER
                : NotificationType.CALENDAR_REMINDER;

        List<User> partners = userRepository.findByCoupleId(event.getCouple().getId());
        for (User partner : partners) {
            if (notificationService.alreadySentToday(partner.getId(), event.getId(), type)) {
                continue;
            }
            String message = String.format("Lembrete: \"%s\" é daqui a %d dia(s) (%s)",
                    event.getTitle(), event.getReminderDaysBefore(), occurrence);
            notificationService.create(partner, message, type, event.getId());
        }
    }
}
