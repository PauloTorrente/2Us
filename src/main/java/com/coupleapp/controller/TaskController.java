package com.coupleapp.controller;

import com.coupleapp.dto.TaskDTOs.*;
import com.coupleapp.entity.User;
import com.coupleapp.service.impl.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Domestic task management")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(request, user));
    }

    @GetMapping
    @Operation(summary = "Get all tasks for the couple")
    public ResponseEntity<List<TaskResponse>> getTasks(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(taskService.getTasksByCouple(user));
    }
}
