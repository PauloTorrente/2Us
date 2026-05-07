package com.coupleapp.dto;

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
}
