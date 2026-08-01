package com.coupleapp.dto;

import com.coupleapp.entity.AvailabilityWindow.AvailabilityType;
import com.coupleapp.entity.CalendarEvent.EventType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CalendarDTOs {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateEventRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 150)
        private String title;

        @Size(max = 500)
        private String description;

        @NotNull(message = "Event date is required")
        private LocalDate eventDate;

        private LocalDate endDate;  // For multi-day events like vacations

        @NotNull(message = "Event type is required")
        private EventType eventType;

        private Boolean recurringYearly = false;
        private Integer reminderDaysBefore; // Null = no reminder
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CalendarEventResponse {
        private Long id;
        private String title;
        private String description;
        private LocalDate eventDate;
        private LocalDate endDate;
        private EventType eventType;
        private Boolean recurringYearly;
        private Integer reminderDaysBefore;
        private LocalDateTime createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateAvailabilityWindowRequest {
        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        @NotNull(message = "Type is required")
        private AvailabilityType type;

        @Size(max = 200)
        private String label;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class AvailabilityWindowResponse {
        private Long id;
        private String userName;
        private LocalDate startDate;
        private LocalDate endDate;
        private AvailabilityType type;
        private String label;
        private LocalDateTime createdAt;
    }

    // A date range where both partners are free at the same time — the whole point of
    // mapping availability windows in the first place.
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class AvailabilityOverlapResponse {
        private LocalDate startDate;
        private LocalDate endDate;
        private String partnerAName;
        private String partnerBName;
        private AvailabilityType partnerAType;
        private AvailabilityType partnerBType;
    }
}
