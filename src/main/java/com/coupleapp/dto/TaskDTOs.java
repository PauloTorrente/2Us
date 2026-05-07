package com.coupleapp.dto;

import com.coupleapp.entity.Task;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class TaskDTOs {

    public record CreateTaskRequest(
            @NotBlank(message = "Description is required")
            String description,

            Task.TaskAssignment assignment
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
