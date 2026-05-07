package com.coupleapp.service.impl;

import com.coupleapp.dto.CalendarDTOs.*;
import com.coupleapp.entity.CalendarEvent;
import com.coupleapp.entity.User;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarEventRepository eventRepository;

    // Returns events for a couple within a date range (used to build the monthly calendar view)
    public List<CalendarEventResponse> getEvents(User user, LocalDate start, LocalDate end) {
        return eventRepository.findByCoupleAndDateRange(user.getCouple().getId(), start, end)
                .stream().map(this::mapToResponse).toList();
    }

    // Returns events coming up within the next N days — for the home screen reminder widget
    public List<CalendarEventResponse> getUpcomingEvents(User user, int daysAhead) {
        LocalDate today = LocalDate.now();
        return eventRepository.findUpcomingEvents(user.getCouple().getId(), today, today.plusDays(daysAhead))
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public CalendarEventResponse createEvent(User user, CreateEventRequest request) {
        CalendarEvent event = CalendarEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .couple(user.getCouple())
                .createdBy(user)
                .eventDate(request.getEventDate())
                .endDate(request.getEndDate())
                .eventType(request.getEventType())
                .recurringYearly(request.getRecurringYearly() != null ? request.getRecurringYearly() : false)
                .reminderDaysBefore(request.getReminderDaysBefore())
                .build();

        return mapToResponse(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(User user, Long eventId) {
        CalendarEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        if (!event.getCouple().getId().equals(user.getCouple().getId())) {
            throw new ForbiddenException("This event does not belong to your couple");
        }
        eventRepository.delete(event);
    }

    private CalendarEventResponse mapToResponse(CalendarEvent e) {
        return new CalendarEventResponse(e.getId(), e.getTitle(), e.getDescription(),
                e.getEventDate(), e.getEndDate(), e.getEventType(),
                e.getRecurringYearly(), e.getReminderDaysBefore(), e.getCreatedAt());
    }
}
