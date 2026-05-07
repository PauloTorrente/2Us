package com.coupleapp.controller;

import com.coupleapp.dto.CalendarDTOs.*;
import com.coupleapp.entity.User;
import com.coupleapp.service.impl.CalendarService;
import com.coupleapp.util.UserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// Calendar endpoints: important dates, anniversaries, menstrual cycle, vacations.
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "Important dates, anniversaries, menstrual cycle, vacations")
public class CalendarController {

    private final CalendarService calendarService;
    private final UserResolver userResolver;

    // GET /api/calendar?start=2025-01-01&end=2025-01-31 — events in a date range
    @GetMapping
    @Operation(summary = "Get calendar events in a date range")
    public ResponseEntity<List<CalendarEventResponse>> getEvents(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(calendarService.getEvents(user, start, end));
    }

    // GET /api/calendar/upcoming?days=7 — events in the next N days (for home screen widget)
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming events in the next N days")
    public ResponseEntity<List<CalendarEventResponse>> getUpcoming(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "7") int days) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.ok(calendarService.getUpcomingEvents(user, days));
    }

    // POST /api/calendar — create a new event (anniversary, birthday, vacation, etc.)
    @PostMapping
    @Operation(summary = "Create a new calendar event")
    public ResponseEntity<CalendarEventResponse> createEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateEventRequest request) {

        User user = userResolver.resolveWithCouple(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarService.createEvent(user, request));
    }

    // DELETE /api/calendar/{eventId}
    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete a calendar event")
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long eventId) {

        User user = userResolver.resolveWithCouple(userDetails);
        calendarService.deleteEvent(user, eventId);
        return ResponseEntity.noContent().build();
    }
}
