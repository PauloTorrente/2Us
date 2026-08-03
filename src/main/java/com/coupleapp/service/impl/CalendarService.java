package com.coupleapp.service.impl;

import com.coupleapp.dto.CalendarDTOs.*;
import com.coupleapp.entity.AvailabilityWindow;
import com.coupleapp.entity.CalendarEvent;
import com.coupleapp.entity.User;
import com.coupleapp.exception.ForbiddenException;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.AvailabilityWindowRepository;
import com.coupleapp.repository.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarEventRepository eventRepository;
    private final AvailabilityWindowRepository availabilityRepository;

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
        User createdBy = e.getCreatedBy();
        return new CalendarEventResponse(e.getId(), e.getTitle(), e.getDescription(),
                e.getEventDate(), e.getEndDate(), e.getEventType(),
                e.getRecurringYearly(), e.getReminderDaysBefore(), e.getCreatedAt(),
                createdBy != null ? createdBy.getId() : null,
                createdBy != null ? createdBy.getName() : null);
    }

    // --- Availability windows (days off, vacations, flexible schedules) ---

    @Transactional
    public AvailabilityWindowResponse createAvailabilityWindow(User user, CreateAvailabilityWindowRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ForbiddenException("endDate cannot be before startDate");
        }

        AvailabilityWindow window = AvailabilityWindow.builder()
                .user(user)
                .couple(user.getCouple())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(request.getType())
                .label(request.getLabel())
                .build();

        return mapToAvailabilityResponse(availabilityRepository.save(window));
    }

    public List<AvailabilityWindowResponse> getAvailabilityWindows(User user) {
        return availabilityRepository.findByCoupleIdOrderByStartDateAsc(user.getCouple().getId())
                .stream().map(this::mapToAvailabilityResponse).toList();
    }

    @Transactional
    public void deleteAvailabilityWindow(User user, Long windowId) {
        AvailabilityWindow window = availabilityRepository.findById(windowId)
                .orElseThrow(() -> new NotFoundException("Availability window not found: " + windowId));
        if (!window.getCouple().getId().equals(user.getCouple().getId())) {
            throw new ForbiddenException("This availability window does not belong to your couple");
        }
        availabilityRepository.delete(window);
    }

    // Finds date ranges where both partners are free at the same time by intersecting
    // every window from partner A against every window from partner B.
    public List<AvailabilityOverlapResponse> getAvailabilityOverlaps(User user) {
        Long coupleId = user.getCouple().getId();
        List<AvailabilityWindow> windows = availabilityRepository.findByCoupleIdOrderByStartDateAsc(coupleId);

        List<AvailabilityWindow> partnerAWindows = new ArrayList<>();
        List<AvailabilityWindow> partnerBWindows = new ArrayList<>();
        Long firstOwnerId = null;

        for (AvailabilityWindow w : windows) {
            Long ownerId = w.getUser().getId();
            if (firstOwnerId == null) {
                firstOwnerId = ownerId;
            }
            if (ownerId.equals(firstOwnerId)) {
                partnerAWindows.add(w);
            } else {
                partnerBWindows.add(w);
            }
        }

        List<AvailabilityOverlapResponse> overlaps = new ArrayList<>();
        for (AvailabilityWindow a : partnerAWindows) {
            for (AvailabilityWindow b : partnerBWindows) {
                LocalDate overlapStart = a.getStartDate().isAfter(b.getStartDate()) ? a.getStartDate() : b.getStartDate();
                LocalDate overlapEnd = a.getEndDate().isBefore(b.getEndDate()) ? a.getEndDate() : b.getEndDate();

                if (!overlapStart.isAfter(overlapEnd)) {
                    overlaps.add(new AvailabilityOverlapResponse(
                            overlapStart, overlapEnd,
                            a.getUser().getName(), b.getUser().getName(),
                            a.getType(), b.getType()
                    ));
                }
            }
        }

        return overlaps;
    }

    private AvailabilityWindowResponse mapToAvailabilityResponse(AvailabilityWindow w) {
        return new AvailabilityWindowResponse(
                w.getId(), w.getUser().getName(), w.getStartDate(), w.getEndDate(),
                w.getType(), w.getLabel(), w.getCreatedAt()
        );
    }
}
