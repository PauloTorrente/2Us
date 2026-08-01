package com.coupleapp.dto;

import com.coupleapp.entity.Task;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class TaskDTOs {

    public record CreateTaskRequest(
            @NotBlank(message = "Description is required")
            String description,

            Task.TaskAssignment assignment,

            // Required when assignment == AGREED: the partner the couple agreed on.
            // Ignored for RANDOM (the app picks) and optional for MANUAL.
            Long assignedToUserId
    ) {}

    public record TaskResponse(
            Long id,
            String description,
            Task.TaskAssignment assignment,
            Task.TaskStatus status,
            String assignedTo,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {}
}
