package com.coupleapp.dto;

import com.coupleapp.entity.SharedActivity.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ActivityDTOs {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateActivityRequest {
        @NotBlank @Size(max = 100)
        private String name;

        @Size(max = 300)
        private String description;

        @NotNull private ActivityFrequency frequency;
        @NotNull private ActivityCategory category;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LogActivityRequest {
        @Size(max = 300)
        private String note;

        // Defaults to now if null — allows backdating if the couple forgot to log
        private LocalDateTime completedAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ActivityResponse {
        private Long id;
        private String name;
        private String description;
        private ActivityFrequency frequency;
        private ActivityCategory category;
        private Boolean isActive;
        private Integer completionCount;
        private LocalDateTime createdAt;
    }

    // Returned by the score dashboard — ranks activities by completions in a period
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ActivityScoreResponse {
        private List<ActivityRank> mostDone;   // Top activities completed
        private List<ActivityRank> leastDone;  // Bottom activities — the neglected ones
        private Integer totalCompletions;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ActivityRank {
        private Long activityId;
        private String activityName;
        private ActivityCategory category;
        private Long completionCount;
    }
}
